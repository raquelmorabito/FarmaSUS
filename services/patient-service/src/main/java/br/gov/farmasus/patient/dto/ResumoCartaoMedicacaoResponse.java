package br.gov.farmasus.patient.dto;

public record ResumoCartaoMedicacaoResponse(
    int totalPrescricoes,
    int totalAlertas,
    int totalAlertasCriticos,
    int medicamentosAtivosHoje
) {}
