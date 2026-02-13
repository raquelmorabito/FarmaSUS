package br.gov.farmasus.medication;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.farmasus.medication.domain.Prescricao;
import br.gov.farmasus.medication.repository.PrescricaoRepository;
import br.gov.farmasus.medication.service.ReceitaPertoVencerScheduler;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(ReceitaPertoVencerSchedulerIntegrationTest.TestRabbitConfig.class)
@SuppressWarnings("resource")
class ReceitaPertoVencerSchedulerIntegrationTest {
  private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");
  private static final String TEST_QUEUE = "test.medication.prescription.expiring";

  @Container
  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("medicationdb")
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
    registry.add("jwt.secret", () -> "jwt-secret-integration-tests-32chars");
    registry.add("app.jwt.secret", () -> "jwt-secret-integration-tests-32chars");
    registry.add("jobs.receita-perto-vencer.dias-antecedencia", () -> "7");
  }

  @Autowired
  private PrescricaoRepository prescricaoRepository;

  @Autowired
  private ReceitaPertoVencerScheduler scheduler;

  @Autowired
  private RabbitAdmin rabbitAdmin;

  @BeforeEach
  void prepararDados() {
    prescricaoRepository.deleteAll();

    LocalDate hoje = LocalDate.now(ZONA_BR);
    Prescricao prescricao = new Prescricao();
    prescricao.setPacienteId(1L);
    prescricao.setNomeMedicamento("Dipirona");
    prescricao.setDosagem("500mg");
    prescricao.setFrequenciaDiaria(2);
    prescricao.setHorarios(List.of(java.time.LocalTime.of(8, 0), java.time.LocalTime.of(20, 0)));
    prescricao.setInicio(hoje.minusDays(3));
    prescricao.setFim(hoje.plusDays(7));
    prescricaoRepository.save(prescricao);
  }

  @Test
  void devePublicarEventoDeReceitaPertoDeVencerComSeteDias() {
    scheduler.processarReceitasPertoDeVencer();

    Awaitility.await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> {
          QueueInformation info = rabbitAdmin.getQueueInfo(TEST_QUEUE);
          assertThat(info).isNotNull();
          assertThat(info.getMessageCount()).isGreaterThanOrEqualTo(1);
        });
  }

  @TestConfiguration
  static class TestRabbitConfig {
    @Bean
    public Queue testPrescriptionExpiringQueue() {
      return QueueBuilder.nonDurable(TEST_QUEUE).build();
    }

    @Bean
    public Binding testPrescriptionExpiringBinding(TopicExchange eventsExchange, Queue testPrescriptionExpiringQueue) {
      return BindingBuilder.bind(testPrescriptionExpiringQueue)
          .to(eventsExchange)
          .with("medication.prescription.expiring");
    }
  }
}
