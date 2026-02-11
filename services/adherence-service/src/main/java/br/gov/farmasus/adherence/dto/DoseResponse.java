package br.gov.farmasus.adherence.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public record DoseResponse(
    Long id,
    Long prescricaoId,
    String nomeMedicamento,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate data,
    @JsonFormat(pattern = "HH:mm")
    LocalTime horario,
    String status
) {}
