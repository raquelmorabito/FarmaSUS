package br.gov.farmasus.safety;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.farmasus.safety.domain.PrescricaoPaciente;
import br.gov.farmasus.safety.dto.PrescricaoCriadaEvent;
import br.gov.farmasus.safety.dto.VerificacaoInteracaoResponse;
import br.gov.farmasus.safety.repository.AlertaInteracaoRepository;
import br.gov.farmasus.safety.repository.PrescricaoPacienteRepository;
import br.gov.farmasus.safety.service.AlertaService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(SafetyIntegrationTest.TestRabbitConfig.class)
@SuppressWarnings("resource")
class SafetyIntegrationTest {
  private static final String TEST_ALERT_QUEUE = "test.safety.alert.created";
  private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

  @Container
  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("safetydb")
      .withUsername("farma")
      .withPassword("farma");

  @Container
  private static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.rabbitmq.host", RABBIT::getHost);
    registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
    registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
    registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
  }

  @Autowired
  private org.springframework.boot.test.web.client.TestRestTemplate restTemplate;

  @Autowired
  private PrescricaoPacienteRepository prescricaoPacienteRepository;

  @Autowired
  private AlertaInteracaoRepository alertaInteracaoRepository;

  @Autowired
  private AlertaService alertaService;

  @Autowired
  private RabbitAdmin rabbitAdmin;

  @BeforeEach
  void setup() {
    alertaInteracaoRepository.deleteAll();
    prescricaoPacienteRepository.deleteAll();
  }

  @Test
  void deveRetornarRiscoQuandoExisteInteracaoComPrescricaoAtiva() {
    LocalDate hoje = LocalDate.now(ZONA_BR);
    prescricaoPacienteRepository.save(prescricao(1L, 10L, "ibuprofeno", hoje.minusDays(1), hoje.plusDays(7)));

    ResponseEntity<VerificacaoInteracaoResponse> response = restTemplate.exchange(
        "/alertas/paciente/verificacao-interacao?pacienteId=1&medicamento=varfarina",
        HttpMethod.GET,
        null,
        VerificacaoInteracaoResponse.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().possuiRisco()).isTrue();
    assertThat(response.getBody().medicamentoConflitante()).isEqualTo("ibuprofeno");
  }

  @Test
  void devePublicarEventoDeAlertaAoProcessarPrescricaoComInteracao() {
    LocalDate hoje = LocalDate.now(ZONA_BR);
    prescricaoPacienteRepository.save(prescricao(1L, 10L, "ibuprofeno", hoje.minusDays(1), hoje.plusDays(7)));

    PrescricaoCriadaEvent event = new PrescricaoCriadaEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        OffsetDateTime.now(ZONA_BR),
        1L,
        20L,
        "varfarina",
        "5mg",
        1,
        List.of(LocalTime.of(8, 0)),
        hoje,
        hoje.plusDays(7)
    );

    alertaService.processarPrescricao(event);

    assertThat(alertaInteracaoRepository.findAllByPacienteIdOrderByCriadoEmDesc(1L)).isNotEmpty();
    Awaitility.await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> {
          QueueInformation info = rabbitAdmin.getQueueInfo(TEST_ALERT_QUEUE);
          assertThat(info).isNotNull();
          assertThat(info.getMessageCount()).isGreaterThanOrEqualTo(1);
        });
  }

  private PrescricaoPaciente prescricao(
      Long pacienteId,
      Long prescricaoId,
      String nomeMedicamento,
      LocalDate inicio,
      LocalDate fim) {
    PrescricaoPaciente prescricao = new PrescricaoPaciente();
    prescricao.setPacienteId(pacienteId);
    prescricao.setPrescricaoId(prescricaoId);
    prescricao.setNomeMedicamento(nomeMedicamento);
    prescricao.setInicio(inicio);
    prescricao.setFim(fim);
    return prescricao;
  }

  @TestConfiguration
  static class TestRabbitConfig {
    @Bean
    Queue testSafetyAlertQueue() {
      return QueueBuilder.nonDurable(TEST_ALERT_QUEUE).build();
    }

    @Bean
    Binding testSafetyAlertBinding(TopicExchange farmaEventsExchange, Queue testSafetyAlertQueue) {
      return BindingBuilder.bind(testSafetyAlertQueue)
          .to(farmaEventsExchange)
          .with("safety.alert.created");
    }
  }
}
