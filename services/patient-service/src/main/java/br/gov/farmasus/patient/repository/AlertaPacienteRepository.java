package br.gov.farmasus.patient.repository;

import br.gov.farmasus.patient.domain.AlertaPaciente;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaPacienteRepository extends JpaRepository<AlertaPaciente, Long> {
  boolean existsByEventId(UUID eventId);

  List<AlertaPaciente> findAllByPacienteIdOrderByCriadoEmDesc(Long pacienteId);
}
