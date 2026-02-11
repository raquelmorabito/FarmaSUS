package br.gov.farmasus.safety.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PrescricaoCriadaEvent(
    UUID eventId,
    UUID correlationId,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime criadoEm,
    Long pacienteId,
    Long prescricaoId,
    String nomeMedicamento,
    String dosagem,
    Integer frequenciaDiaria,
    @JsonFormat(pattern = "HH:mm")
    List<LocalTime> horarios,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate inicio,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fim
) {}
