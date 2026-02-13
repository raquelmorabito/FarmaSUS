package br.gov.farmasus.patient.dto;

import java.util.List;

public record PerfilClinicoPacienteResponse(
    Long pacienteId,
    String tipoSanguineo,
    List<AlergiaPacienteResponse> alergias
) {}

