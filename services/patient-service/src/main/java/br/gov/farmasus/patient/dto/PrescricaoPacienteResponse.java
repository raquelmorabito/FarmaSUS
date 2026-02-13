package br.gov.farmasus.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PrescricaoPacienteResponse(
    Long prescricaoId,
    Long pacienteId,
    String nomeMedicamento,
    String dosagem,
    Integer frequenciaDiaria,
    @JsonFormat(pattern = "HH:mm")
    List<LocalTime> horarios,
    String orientacoesUso,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate inicio,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fim
) {}
