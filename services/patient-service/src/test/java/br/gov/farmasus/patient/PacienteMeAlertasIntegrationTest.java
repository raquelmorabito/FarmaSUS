package br.gov.farmasus.patient;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.farmasus.patient.domain.AlertaPaciente;
import br.gov.farmasus.patient.domain.PacienteLogin;
import br.gov.farmasus.patient.domain.Severidade;
import br.gov.farmasus.patient.dto.AlertaPacienteResponse;
import br.gov.farmasus.patient.repository.AlertaPacienteRepository;
import br.gov.farmasus.patient.repository.PacienteLoginRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
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
class PacienteMeAlertasIntegrationTest {
  private static final String JWT_SECRET = "jwt-secret-integration-tests-32chars";

  @Container
  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("patientdb")
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
  private PacienteLoginRepository pacienteLoginRepository;

  @Autowired
  private AlertaPacienteRepository alertaPacienteRepository;

  @BeforeEach
  void prepararDados() {
    alertaPacienteRepository.deleteAll();
    pacienteLoginRepository.deleteAll();

    PacienteLogin pacienteLogin = new PacienteLogin();
    pacienteLogin.setLogin("paciente1");
    pacienteLogin.setPacienteId(1L);
    pacienteLoginRepository.save(pacienteLogin);

    AlertaPaciente alerta = new AlertaPaciente();
    alerta.setEventId(UUID.randomUUID());
    alerta.setCorrelationId(UUID.randomUUID());
    alerta.setCriadoEm(OffsetDateTime.now(ZoneOffset.ofHours(-3)));
    alerta.setAlertaId(100L);
    alerta.setPacienteId(1L);
    alerta.setPrescricaoId(10L);
    alerta.setSeveridade(Severidade.ALTA);
    alerta.setMensagem("Risco de interacao");
    alerta.setRecomendacao("Avaliar substituicao");
    alertaPacienteRepository.save(alerta);
  }

  @Test
  void deveConsultarAlertasPeloEndpointMe() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(gerarToken("paciente1", "PACIENTE"));

    ResponseEntity<AlertaPacienteResponse[]> response = restTemplate.exchange(
        "/pacientes/me/alertas",
        HttpMethod.GET,
        new HttpEntity<>(null, headers),
        AlertaPacienteResponse[].class);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody()[0].pacienteId()).isEqualTo(1L);
    assertThat(response.getBody()[0].mensagem()).isEqualTo("Risco de interacao");
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
