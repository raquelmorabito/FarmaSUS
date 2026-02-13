package br.gov.farmasus.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TipoSanguineoRequest(
    @NotBlank(message = "tipoSanguineo e obrigatorio")
    @Pattern(
        regexp = "^(A|B|AB|O)[+-]$",
        message = "tipoSanguineo deve seguir o formato A+, A-, B+, B-, AB+, AB-, O+ ou O-")
    String tipoSanguineo
) {}

