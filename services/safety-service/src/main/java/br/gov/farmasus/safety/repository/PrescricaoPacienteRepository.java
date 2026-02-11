package br.gov.farmasus.safety.repository;

import br.gov.farmasus.safety.domain.PrescricaoPaciente;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescricaoPacienteRepository extends JpaRepository<PrescricaoPaciente, Long> {
  List<PrescricaoPaciente> findAllByPacienteIdAndInicioLessThanEqualAndFimGreaterThanEqual(
      Long pacienteId,
      LocalDate inicio,
      LocalDate fim
  );
}
