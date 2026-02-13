package br.gov.farmasus.patient.dto;

import java.util.List;

public record DisponibilidadeMedicamentoResponse(
    String medicamento,
    List<UbsDisponibilidadeResponse> unidades
) {}

