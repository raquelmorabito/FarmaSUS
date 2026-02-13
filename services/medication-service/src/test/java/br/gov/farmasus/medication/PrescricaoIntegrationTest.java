package br.gov.farmasus.medication;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.farmasus.medication.dto.PrescricaoRequest;
import br.gov.farmasus.medication.dto.PrescricaoResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.awaitility.Awaitility;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
@Import(PrescricaoIntegrationTest.TestRabbitConfig.class)
@SuppressWarnings("resource")
class PrescricaoIntegrationTest {
  private static final String JWT_SECRET = "jwt-secret-integration-tests-32chars";
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
    registry.add("jwt.secret", () -> JWT_SECRET);
    registry.add("app.jwt.secret", () -> JWT_SECRET);
  }

  @Autowired
  private org.springframework.boot.test.web.client.TestRestTemplate restTemplate;

  @Autowired
  private RabbitAdmin rabbitAdmin;

  @Test
  void deveCriarPrescricoesEPublicarEventos() {
    LocalDate inicio = LocalDate.now(ZONA_BR);
    LocalDate fim = inicio.plusDays(7);

    PrescricaoRequest varfarina = new PrescricaoRequest(
        "Varfarina",
        "5mg",
        1,
        List.of(LocalTime.of(8, 0)),
        "Tomar apos cafe da manha.",
        null,
        inicio,
        fim
    );

    PrescricaoRequest ibuprofeno = new PrescricaoRequest(
        "Ibuprofeno",
        "400mg",
        2,
        List.of(LocalTime.of(10, 0), LocalTime.of(22, 0)),
        "Tomar apos refeicoes.",
        null,
        inicio,
        fim
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(gerarTokenProfissional("prof1"));

    ResponseEntity<PrescricaoResponse> resposta1 = restTemplate.exchange(
        "/pacientes/1/medicamentos",
        HttpMethod.POST,
        new HttpEntity<>(varfarina, headers),
        PrescricaoResponse.class
    );

    ResponseEntity<PrescricaoResponse> resposta2 = restTemplate.exchange(
        "/pacientes/1/medicamentos",
        HttpMethod.POST,
        new HttpEntity<>(ibuprofeno, headers),
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

  private String gerarTokenProfissional(String login) {
    SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    OffsetDateTime agora = OffsetDateTime.now(ZONA_BR);
    OffsetDateTime expiraEm = agora.plusMinutes(30);
    return Jwts.builder()
        .setSubject(login)
        .claim("tipoUsuario", "PROFISSIONAL")
        .setIssuedAt(Date.from(agora.toInstant()))
        .setExpiration(Date.from(expiraEm.toInstant()))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
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
