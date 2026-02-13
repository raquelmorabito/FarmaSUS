package br.gov.farmasus.adherence.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record RegistroDoseHistoricoResponse(
    Long registroId,
    Long doseId,
    Long prescricaoId,
    String nomeMedicamento,
    String status,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dataDose,
    @JsonFormat(pattern = "HH:mm")
    LocalTime horarioDose,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime registradoEm,
    String comentario
) {}

