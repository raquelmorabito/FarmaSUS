package br.gov.farmasus.patient.service;

import br.gov.farmasus.patient.domain.Severidade;
import br.gov.farmasus.patient.dto.AlertaPacienteResponse;
import br.gov.farmasus.patient.dto.AlergiaPacienteResponse;
import br.gov.farmasus.patient.dto.CartaoMedicacaoResponse;
import br.gov.farmasus.patient.dto.PerfilClinicoPacienteResponse;
import br.gov.farmasus.patient.dto.PrescricaoPacienteResponse;
import br.gov.farmasus.patient.dto.ResumoCartaoMedicacaoResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CartaoMedicacaoService {
  private final AlertaPacienteService alertaPacienteService;
  private final PerfilClinicoPacienteService perfilClinicoPacienteService;
  private final MedicationServiceClient medicationServiceClient;

  public CartaoMedicacaoService(
      AlertaPacienteService alertaPacienteService,
      PerfilClinicoPacienteService perfilClinicoPacienteService,
      MedicationServiceClient medicationServiceClient) {
    this.alertaPacienteService = alertaPacienteService;
    this.perfilClinicoPacienteService = perfilClinicoPacienteService;
    this.medicationServiceClient = medicationServiceClient;
  }

  public CartaoMedicacaoResponse obterCartao(Long pacienteId, String authorizationHeader) {
    List<PrescricaoPacienteResponse> prescricoes = buscarPrescricoes(pacienteId, authorizationHeader);
    List<AlertaPacienteResponse> alertas = alertaPacienteService.listarAlertas(pacienteId);
    PerfilClinicoPacienteResponse perfil = perfilClinicoPacienteService.obterPerfil(pacienteId);
    List<AlergiaPacienteResponse> alergias = perfil.alergias();
    ResumoCartaoMedicacaoResponse resumo = montarResumo(prescricoes, alertas);

    return new CartaoMedicacaoResponse(
        pacienteId,
        prescricoes,
        alertas,
        alergias,
        perfil.tipoSanguineo(),
        resumo
    );
  }

  private List<PrescricaoPacienteResponse> buscarPrescricoes(Long pacienteId, String authorizationHeader) {
    return medicationServiceClient.buscarPrescricoes(pacienteId, authorizationHeader);
  }

  private ResumoCartaoMedicacaoResponse montarResumo(
      List<PrescricaoPacienteResponse> prescricoes,
      List<AlertaPacienteResponse> alertas) {
    LocalDate hoje = LocalDate.now();
    int ativosHoje = (int) prescricoes.stream()
        .filter(p -> !p.inicio().isAfter(hoje) && !p.fim().isBefore(hoje))
        .count();

    int alertasCriticos = (int) alertas.stream()
        .filter(a -> a.severidade() == Severidade.CONTRAINDICADA || a.severidade() == Severidade.ALTA)
        .count();

    return new ResumoCartaoMedicacaoResponse(
        prescricoes.size(),
        alertas.size(),
        alertasCriticos,
        ativosHoje
    );
  }
}
