package br.gov.farmasus.adherence.service;

import br.gov.farmasus.adherence.config.PacienteLoginMappingProperties;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PacienteLoginMapper {
  private final Map<String, Long> loginParaPacienteId;

  public PacienteLoginMapper(PacienteLoginMappingProperties properties) {
    this.loginParaPacienteId = properties.mappings() == null ? Map.of() : Map.copyOf(properties.mappings());
  }

  public Optional<Long> obterPacienteId(String login) {
    return Optional.ofNullable(loginParaPacienteId.get(login));
  }
}
