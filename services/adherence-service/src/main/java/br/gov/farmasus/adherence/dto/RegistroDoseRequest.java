package br.gov.farmasus.adherence.dto;

import br.gov.farmasus.adherence.domain.DoseStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record RegistroDoseRequest(
    @NotNull DoseStatus status,
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime registradoEm,
    @Size(max = 300, message = "comentario deve ter no maximo 300 caracteres")
    String comentario
) {}
