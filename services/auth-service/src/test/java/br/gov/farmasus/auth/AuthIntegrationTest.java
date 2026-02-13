package br.gov.farmasus.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.gov.farmasus.auth.dto.LoginRequest;
import br.gov.farmasus.auth.model.TipoUsuario;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {
  private static final String JWT_SECRET = "jwt-secret-integration-tests-32chars";
  private static final String PACIENTE_LOGIN = "pac_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  private static final String PACIENTE_SENHA = "pw_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  private static final String PROF_LOGIN = "prof_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  private static final String PROF_SENHA = "pw_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("jwt.secret", () -> JWT_SECRET);
    registry.add("spring.datasource.url", () -> "jdbc:h2:mem:authdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
    registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
    registry.add("spring.datasource.username", () -> "sa");
    registry.add("spring.datasource.password", () -> "");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.flyway.enabled", () -> "true");
    registry.add("spring.flyway.placeholders.seed_paciente_login", () -> PACIENTE_LOGIN);
    registry.add("spring.flyway.placeholders.seed_paciente_senha", () -> PACIENTE_SENHA);
    registry.add("spring.flyway.placeholders.seed_paciente_tipo", () -> "PACIENTE");
    registry.add("spring.flyway.placeholders.seed_prof_login", () -> PROF_LOGIN);
    registry.add("spring.flyway.placeholders.seed_prof_senha", () -> PROF_SENHA);
    registry.add("spring.flyway.placeholders.seed_prof_tipo", () -> "PROFISSIONAL");
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void deveFazerLoginComCredenciaisValidas() throws Exception {
    LoginRequest request = new LoginRequest(PROF_LOGIN, PROF_SENHA, TipoUsuario.PROFISSIONAL);

    mockMvc.perform(post("/auth/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.tipoUsuario").value("PROFISSIONAL"))
        .andExpect(jsonPath("$.expiraEm").isNotEmpty());
  }

  @Test
  void deveRetornar401QuandoCredenciaisForemInvalidas() throws Exception {
    LoginRequest request = new LoginRequest(PROF_LOGIN, "senha-invalida", TipoUsuario.PROFISSIONAL);

    mockMvc.perform(post("/auth/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveConsultarEndpointMeComTokenValido() throws Exception {
    LoginRequest login = new LoginRequest(PACIENTE_LOGIN, PACIENTE_SENHA, TipoUsuario.PACIENTE);
    MvcResult loginResult = mockMvc.perform(post("/auth/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(login)))
        .andExpect(status().isOk())
        .andReturn();

    String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    assertThat(token).isNotBlank();

    mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.login").value(PACIENTE_LOGIN))
        .andExpect(jsonPath("$.tipoUsuario").value("PACIENTE"));
  }

  @Test
  void deveRetornar401NoEndpointMeSemToken() throws Exception {
    mockMvc.perform(get("/auth/me"))
        .andExpect(status().isUnauthorized());
  }
}
