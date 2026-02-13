package br.gov.farmasus.medication;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
class JwtFlowIntegrationTest {
  private static final String JWT_SECRET = "jwt-secret-integration-tests-32chars";
  private static final ZoneId ZONA_BR = ZoneId.of("America/Sao_Paulo");

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
    registry.add("jwt.secret", () -> JWT_SECRET);
    registry.add("app.jwt.secret", () -> JWT_SECRET);
  }

  @Autowired
  private org.springframework.boot.test.web.client.TestRestTemplate restTemplate;

  @BeforeEach
  void configurarHttpSemStreaming() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setOutputStreaming(false);
    restTemplate.getRestTemplate().setRequestFactory(requestFactory);
  }

  @Test
  void deveRetornar401SemToken() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response = restTemplate.exchange(
        "/pacientes/1/medicamentos",
        HttpMethod.POST,
        new HttpEntity<>(prescricaoPayload(), headers),
        String.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void deveAceitarJwtValidoNoEndpointProtegido() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(gerarTokenProfissional("prof1"));

    ResponseEntity<String> response = restTemplate.exchange(
        "/pacientes/1/medicamentos",
        HttpMethod.POST,
        new HttpEntity<>(prescricaoPayload(), headers),
        String.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).contains("\"nomeMedicamento\":\"Dipirona\"");
  }

  private Map<String, Object> prescricaoPayload() {
    LocalDate inicio = LocalDate.now(ZONA_BR);
    return Map.of(
        "nomeMedicamento", "Dipirona",
        "dosagem", "500mg",
        "frequenciaDiaria", 2,
        "horarios", List.of("08:00", "20:00"),
        "inicio", inicio.toString(),
        "fim", inicio.plusDays(7).toString()
    );
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
}
