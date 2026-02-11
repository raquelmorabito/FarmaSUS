package br.gov.farmasus.safety.repository;

import br.gov.farmasus.safety.domain.AlertaInteracao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaInteracaoRepository extends JpaRepository<AlertaInteracao, Long> {
  List<AlertaInteracao> findAllByPacienteIdOrderByCriadoEmDesc(Long pacienteId);
  boolean existsByPrescricaoId(Long prescricaoId);
}
