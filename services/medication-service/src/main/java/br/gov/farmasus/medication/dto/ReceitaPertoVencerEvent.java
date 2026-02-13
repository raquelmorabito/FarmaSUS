package br.gov.farmasus.medication.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReceitaPertoVencerEvent(
    UUID eventId,
    UUID correlationId,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime criadoEm,
    Long pacienteId,
    Long prescricaoId,
    String nomeMedicamento,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fim,
    Integer diasParaVencer
) {}
