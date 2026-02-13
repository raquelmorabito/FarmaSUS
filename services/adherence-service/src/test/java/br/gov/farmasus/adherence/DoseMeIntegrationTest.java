package br.gov.farmasus.adherence;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.farmasus.adherence.domain.Dose;
import br.gov.farmasus.adherence.domain.DoseStatus;
import br.gov.farmasus.adherence.dto.DoseResponse;
import br.gov.farmasus.adherence.repository.DoseRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@SuppressWarnings("resource")
class DoseMeIntegrationTest {
  private static final String JWT_SECRET = "jwt-secret-integration-tests-32chars";
  private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

  @Container
  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("adherencedb")
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
    registry.add("jwt.secret", () -> JWT_SECRET);
    registry.add("app.jwt.secret", () -> JWT_SECRET);
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private DoseRepository doseRepository;

  @BeforeEach
  void prepararDados() {
    doseRepository.deleteAll();

    Dose dose = new Dose();
    dose.setPrescricaoId(101L);
    dose.setPacienteId(1L);
    dose.setNomeMedicamento("Dipirona");
    dose.setData(LocalDate.now(ZONA_BR));
    dose.setHorario(LocalTime.of(8, 0));
    dose.setStatus(DoseStatus.PENDENTE);
    doseRepository.save(dose);
  }

  @Test
  void deveConsultarDosesHojePeloEndpointMe() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(gerarToken("paciente1", "PACIENTE"));

    ResponseEntity<DoseResponse[]> response = restTemplate.exchange(
        "/pacientes/me/doses-hoje",
        HttpMethod.GET,
        new HttpEntity<>(null, headers),
        DoseResponse[].class);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody()[0].nomeMedicamento()).isEqualTo("Dipirona");
    assertThat(response.getBody()[0].status()).isEqualTo("PENDENTE");
  }

  private String gerarToken(String login, String tipoUsuario) {
    SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    Date agora = new Date();
    Date expiracao = new Date(agora.getTime() + 30L * 60L * 1000L);
    return Jwts.builder()
        .setSubject(login)
        .claim("tipoUsuario", tipoUsuario)
        .setIssuedAt(agora)
        .setExpiration(expiracao)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }
}
