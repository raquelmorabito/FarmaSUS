package br.gov.farmasus.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public record LembretePacienteResponse(
    String tipo,
    String titulo,
    String mensagem,
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate data,
    @JsonFormat(pattern = "HH:mm")
    LocalTime horario,
    String prioridade
) {}

