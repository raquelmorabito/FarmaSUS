package br.gov.farmasus.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlergiaPacienteRequest(
    @NotBlank(message = "descricao e obrigatoria")
    @Size(max = 200, message = "descricao deve ter no maximo 200 caracteres")
    String descricao
) {}

