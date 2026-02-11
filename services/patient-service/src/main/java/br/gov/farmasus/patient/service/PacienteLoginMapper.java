package br.gov.farmasus.patient.service;

import br.gov.farmasus.patient.repository.PacienteLoginRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PacienteLoginMapper {
  private final PacienteLoginRepository repository;

  public PacienteLoginMapper(PacienteLoginRepository repository) {
    this.repository = repository;
  }

  public Optional<Long> obterPacienteId(String login) {
    return repository.findByLogin(login).map(pacienteLogin -> pacienteLogin.getPacienteId());
  }
}
