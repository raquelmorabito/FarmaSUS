package br.gov.farmasus.safety.repository;

import br.gov.farmasus.safety.domain.InteracaoMedicamento;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteracaoMedicamentoRepository extends JpaRepository<InteracaoMedicamento, Long> {
  Optional<InteracaoMedicamento> findByMedicamentoAAndMedicamentoB(String medicamentoA, String medicamentoB);
}
