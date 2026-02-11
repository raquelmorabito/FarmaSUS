package br.gov.farmasus.adherence.repository;

import br.gov.farmasus.adherence.domain.Dose;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoseRepository extends JpaRepository<Dose, Long> {
  boolean existsByPrescricaoId(Long prescricaoId);

  List<Dose> findAllByPacienteIdAndDataOrderByHorario(Long pacienteId, LocalDate data);

  Optional<Dose> findByIdAndPacienteId(Long id, Long pacienteId);
}
