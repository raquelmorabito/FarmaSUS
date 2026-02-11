package br.gov.farmasus.adherence.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DoseRegistradaEvent(
    UUID eventId,
    UUID correlationId,
    Long doseId,
    Long prescricaoId,
    Long pacienteId,
    String status,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime registradoEm
) {}
