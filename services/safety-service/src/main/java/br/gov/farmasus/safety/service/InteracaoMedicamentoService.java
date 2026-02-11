package br.gov.farmasus.safety.service;

import br.gov.farmasus.safety.domain.InteracaoMedicamento;
import br.gov.farmasus.safety.repository.InteracaoMedicamentoRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class InteracaoMedicamentoService {
  private final InteracaoMedicamentoRepository repository;

  public InteracaoMedicamentoService(InteracaoMedicamentoRepository repository) {
    this.repository = repository;
  }

  public Optional<InteracaoMedicamento> buscarInteracao(String medicamento1, String medicamento2) {
    String nome1 = normalizar(medicamento1);
    String nome2 = normalizar(medicamento2);
    if (nome1.isEmpty() || nome2.isEmpty()) {
      return Optional.empty();
    }
    String primeiro = nome1.compareTo(nome2) <= 0 ? nome1 : nome2;
    String segundo = nome1.compareTo(nome2) <= 0 ? nome2 : nome1;
    return repository.findByMedicamentoAAndMedicamentoB(primeiro, segundo);
  }

  public String normalizar(String nome) {
    if (nome == null) {
      return "";
    }
    return nome.trim().toLowerCase(Locale.ROOT);
  }
}
