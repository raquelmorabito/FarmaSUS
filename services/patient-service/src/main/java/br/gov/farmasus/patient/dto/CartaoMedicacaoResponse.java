package br.gov.farmasus.patient.dto;

import java.util.List;

public record CartaoMedicacaoResponse(
    Long pacienteId,
    List<PrescricaoPacienteResponse> prescricoes,
    List<AlertaPacienteResponse> alertas,
    ResumoCartaoMedicacaoResponse resumo
) {}
