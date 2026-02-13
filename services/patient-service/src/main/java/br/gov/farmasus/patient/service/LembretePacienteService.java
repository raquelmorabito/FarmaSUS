package br.gov.farmasus.patient.service;

import br.gov.farmasus.patient.dto.LembretePacienteResponse;
import br.gov.farmasus.patient.dto.PrescricaoPacienteResponse;
import br.gov.farmasus.patient.dto.DoseHojeResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LembretePacienteService {
  private final MedicationServiceClient medicationServiceClient;
  private final AdherenceServiceClient adherenceServiceClient;

  public LembretePacienteService(
      MedicationServiceClient medicationServiceClient,
      AdherenceServiceClient adherenceServiceClient) {
    this.medicationServiceClient = medicationServiceClient;
    this.adherenceServiceClient = adherenceServiceClient;
  }

  public List<LembretePacienteResponse> listarLembretes(Long pacienteId, String authorizationHeader) {
    List<PrescricaoPacienteResponse> prescricoes = buscarPrescricoes(pacienteId, authorizationHeader);
    List<LembretePacienteResponse> lembretes = new ArrayList<>();

    LocalDate hoje = LocalDate.now();
    LocalDate limite = hoje.plusDays(7);

    for (PrescricaoPacienteResponse prescricao : prescricoes) {
      if (!prescricao.fim().isBefore(hoje) && !prescricao.fim().isAfter(limite)) {
        lembretes.add(new LembretePacienteResponse(
            "RECEITA_VENCIMENTO",
            "Receita proxima do vencimento",
            "A receita de " + prescricao.nomeMedicamento() + " vence em " + prescricao.fim() + ". Procure sua UBS.",
            prescricao.fim(),
            null,
            "ALTA"
        ));
      }
    }

    List<DoseHojeResponse> dosesHoje = buscarDosesHoje(authorizationHeader);
    for (DoseHojeResponse dose : dosesHoje) {
      if (!"PENDENTE".equalsIgnoreCase(dose.status())) {
        continue;
      }
      lembretes.add(new LembretePacienteResponse(
          "DOSE_HOJE",
          "Horario de medicacao",
          "Tomar " + dose.nomeMedicamento() + " as " + dose.horario() + ".",
          dose.data(),
          dose.horario(),
          "MEDIA"
      ));
    }

    lembretes.sort(Comparator
        .comparing(LembretePacienteResponse::data)
        .thenComparing(l -> l.horario() == null ? java.time.LocalTime.MAX : l.horario()));
    return lembretes;
  }

  private List<PrescricaoPacienteResponse> buscarPrescricoes(Long pacienteId, String authorizationHeader) {
    return medicationServiceClient.buscarPrescricoes(pacienteId, authorizationHeader);
  }

  private List<DoseHojeResponse> buscarDosesHoje(String authorizationHeader) {
    return adherenceServiceClient.buscarDosesHoje(authorizationHeader);
  }
}
