package br.gov.farmasus.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public record DoseHojeResponse(
    Long id,
    Long prescricaoId,
    String nomeMedicamento,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate data,
    @JsonFormat(pattern = "HH:mm")
    LocalTime horario,
    String status
) {}

