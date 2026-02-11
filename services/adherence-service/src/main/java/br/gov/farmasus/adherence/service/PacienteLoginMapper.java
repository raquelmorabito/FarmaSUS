package br.gov.farmasus.adherence.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PacienteLoginMapper {
  private final Map<String, Long> loginParaPacienteId = Map.of(
      "paciente1", 1L,
      "paciente2", 2L
  );

  public Optional<Long> obterPacienteId(String login) {
    return Optional.ofNullable(loginParaPacienteId.get(login));
  }
}
