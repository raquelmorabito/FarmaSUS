package br.gov.farmasus.medication.repository;

import br.gov.farmasus.medication.domain.PacienteLogin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteLoginRepository extends JpaRepository<PacienteLogin, Long> {
  Optional<PacienteLogin> findByLogin(String login);
}
