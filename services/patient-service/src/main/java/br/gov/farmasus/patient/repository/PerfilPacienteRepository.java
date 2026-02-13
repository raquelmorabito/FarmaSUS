package br.gov.farmasus.patient.repository;

import br.gov.farmasus.patient.domain.PerfilPaciente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilPacienteRepository extends JpaRepository<PerfilPaciente, Long> {
  Optional<PerfilPaciente> findByPacienteId(Long pacienteId);
}

