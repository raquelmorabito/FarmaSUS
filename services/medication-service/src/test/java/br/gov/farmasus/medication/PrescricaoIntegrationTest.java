package br.gov.farmasus.medication;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.farmasus.medication.dto.PrescricaoRequest;
import br.gov.farmasus.medication.dto.PrescricaoResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(PrescricaoIntegrationTest.TestRabbitConfig.class)
@SuppressWarnings("resource")
class PrescricaoIntegrationTest {
  private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");
  private static final String TEST_QUEUE = "test.medication.created";

  @Container
  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("medicationdb")
      .withUsername("farma")
      .withPassword("farma");

  @Container
  private static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    String jdbcUrl = POSTGRES.getJdbcUrl();
    String dbUser = POSTGRES.getUsername();
    String dbPass = POSTGRES.getPassword();

    String rabbitHost = RABBIT.getHost();
    Integer rabbitPort = RABBIT.getAmqpPort();

    registry.add("spring.datasource.url", () -> jdbcUrl);
    registry.add("spring.datasource.username", () -> dbUser);
    registry.add("spring.datasource.password", () -> dbPass);
    registry.add("spring.rabbitmq.host", () -> rabbitHost);
    registry.add("spring.rabbitmq.port", () -> rabbitPort);
    registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
    registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
  }

  @Autowired
  private org.springframework.boot.test.web.client.TestRestTemplate restTemplate;

  @Autowired
  private RabbitAdmin rabbitAdmin;

  @BeforeEach
  void configurarHttpSemStreaming() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setOutputStreaming(false);
    restTemplate.getRestTemplate().setRequestFactory(requestFactory);
  }

  @Test
  void deveCriarPrescricoesEPublicarEventos() {
    LocalDate inicio = LocalDate.now(ZONA_BR);
    LocalDate fim = inicio.plusDays(7);

    PrescricaoRequest varfarina = new PrescricaoRequest(
        "Varfarina",
        "5mg",
        1,
        List.of(LocalTime.of(8, 0)),
        inicio,
        fim
    );

    PrescricaoRequest ibuprofeno = new PrescricaoRequest(
        "Ibuprofeno",
        "400mg",
        2,
        List.of(LocalTime.of(10, 0), LocalTime.of(22, 0)),
        inicio,
        fim
    );

    ResponseEntity<PrescricaoResponse> resposta1 = restTemplate.postForEntity(
        "/pacientes/1/medicamentos",
        varfarina,
        PrescricaoResponse.class
    );

    ResponseEntity<PrescricaoResponse> resposta2 = restTemplate.postForEntity(
        "/pacientes/1/medicamentos",
        ibuprofeno,
        PrescricaoResponse.class
    );

    assertThat(resposta1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(resposta2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    Awaitility.await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> {
          QueueInformation info = rabbitAdmin.getQueueInfo(TEST_QUEUE);
          assertThat(info).isNotNull();
          assertThat(info.getMessageCount()).isGreaterThanOrEqualTo(2);
        });
  }

  @TestConfiguration
  static class TestRabbitConfig {
    @Bean
    public Queue testMedicationCreatedQueue() {
      return QueueBuilder.nonDurable(TEST_QUEUE).build();
    }

    @Bean
    public Binding testMedicationCreatedBinding(TopicExchange eventsExchange, Queue testMedicationCreatedQueue) {
      return BindingBuilder.bind(testMedicationCreatedQueue)
          .to(eventsExchange)
          .with("medication.created");
    }
  }
}
