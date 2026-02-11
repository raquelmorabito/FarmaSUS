package br.gov.farmasus.adherence.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PrescricaoCriadaEvent(
    UUID eventId,
    UUID correlationId,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime criadoEm,
    Long prescricaoId,
    Long pacienteId,
    String nomeMedicamento,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate inicio,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fim,
    List<String> horarios
) {}
