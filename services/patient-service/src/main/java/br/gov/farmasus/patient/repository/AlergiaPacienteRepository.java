package br.gov.farmasus.patient.repository;

import br.gov.farmasus.patient.domain.AlergiaPaciente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlergiaPacienteRepository extends JpaRepository<AlergiaPaciente, Long> {
  List<AlergiaPaciente> findAllByPacienteIdOrderByDescricaoAsc(Long pacienteId);

  Optional<AlergiaPaciente> findByIdAndPacienteId(Long id, Long pacienteId);
}

