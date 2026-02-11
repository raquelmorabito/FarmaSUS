package br.gov.farmasus.medication.service;

import br.gov.farmasus.medication.repository.PacienteLoginRepository;
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
