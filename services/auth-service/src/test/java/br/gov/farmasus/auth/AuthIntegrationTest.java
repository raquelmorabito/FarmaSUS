package br.gov.farmasus.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.gov.farmasus.auth.dto.LoginRequest;
import br.gov.farmasus.auth.model.TipoUsuario;
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

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("jwt.secret", () -> JWT_SECRET);
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void deveFazerLoginComCredenciaisValidas() throws Exception {
    LoginRequest request = new LoginRequest("prof1", "senha123", TipoUsuario.PROFISSIONAL);

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
    LoginRequest request = new LoginRequest("prof1", "senha-invalida", TipoUsuario.PROFISSIONAL);

    mockMvc.perform(post("/auth/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveConsultarEndpointMeComTokenValido() throws Exception {
    LoginRequest login = new LoginRequest("paciente1", "senha123", TipoUsuario.PACIENTE);
    MvcResult loginResult = mockMvc.perform(post("/auth/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(login)))
        .andExpect(status().isOk())
        .andReturn();

    String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    assertThat(token).isNotBlank();

    mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.login").value("paciente1"))
        .andExpect(jsonPath("$.tipoUsuario").value("PACIENTE"));
  }

  @Test
  void deveRetornar401NoEndpointMeSemToken() throws Exception {
    mockMvc.perform(get("/auth/me"))
        .andExpect(status().isUnauthorized());
  }
}
