package br.gov.farmasus.patient.dto;

public record AlergiaPacienteResponse(
    Long alergiaId,
    Long pacienteId,
    String descricao,
    String origem
) {}

