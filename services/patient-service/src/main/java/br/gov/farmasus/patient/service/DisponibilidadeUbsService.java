package br.gov.farmasus.patient.service;

import br.gov.farmasus.patient.dto.DisponibilidadeMedicamentoResponse;
import br.gov.farmasus.patient.dto.UbsDisponibilidadeResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DisponibilidadeUbsService {
  private final Map<String, List<UbsDisponibilidadeResponse>> disponibilidadePorMedicamento;

  public DisponibilidadeUbsService() {
    this.disponibilidadePorMedicamento = new HashMap<>();
    disponibilidadePorMedicamento.put("dipirona", List.of(
        new UbsDisponibilidadeResponse(101L, "UBS Central", "Rua das Flores, 120", 240),
        new UbsDisponibilidadeResponse(102L, "UBS Vila Nova", "Av. Brasil, 845", 95)
    ));
    disponibilidadePorMedicamento.put("metformina", List.of(
        new UbsDisponibilidadeResponse(101L, "UBS Central", "Rua das Flores, 120", 180),
        new UbsDisponibilidadeResponse(103L, "UBS Jardim Esperanca", "Rua Sao Jorge, 56", 70)
    ));
    disponibilidadePorMedicamento.put("losartana", List.of(
        new UbsDisponibilidadeResponse(102L, "UBS Vila Nova", "Av. Brasil, 845", 130),
        new UbsDisponibilidadeResponse(103L, "UBS Jardim Esperanca", "Rua Sao Jorge, 56", 40)
    ));
    disponibilidadePorMedicamento.put("sinvastatina", List.of(
        new UbsDisponibilidadeResponse(101L, "UBS Central", "Rua das Flores, 120", 65),
        new UbsDisponibilidadeResponse(104L, "UBS Parque Sul", "Rua Um, 300", 25)
    ));
  }

  public DisponibilidadeMedicamentoResponse buscar(String medicamento) {
    String chave = normalizar(medicamento);
    List<UbsDisponibilidadeResponse> unidades = disponibilidadePorMedicamento.getOrDefault(chave, List.of());
    return new DisponibilidadeMedicamentoResponse(medicamento, unidades);
  }

  private String normalizar(String texto) {
    if (texto == null) {
      return "";
    }
    return texto.trim().toLowerCase(Locale.ROOT);
  }
}

