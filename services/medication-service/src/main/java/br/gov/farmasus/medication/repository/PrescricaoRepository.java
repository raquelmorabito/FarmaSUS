package br.gov.farmasus.medication.repository;

import br.gov.farmasus.medication.domain.Prescricao;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescricaoRepository extends JpaRepository<Prescricao, Long> {
  List<Prescricao> findAllByPacienteId(Long pacienteId);
  List<Prescricao> findAllByFim(LocalDate fim);
}
