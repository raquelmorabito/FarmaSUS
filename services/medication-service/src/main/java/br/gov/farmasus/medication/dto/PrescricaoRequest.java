package br.gov.farmasus.medication.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PrescricaoRequest(
    @NotBlank(message = "nomeMedicamento e obrigatorio")
    String nomeMedicamento,

    @NotBlank(message = "dosagem e obrigatoria")
    String dosagem,

    @NotNull(message = "frequenciaDiaria e obrigatoria")
    @Positive(message = "frequenciaDiaria deve ser positiva")
    Integer frequenciaDiaria,

    @NotNull(message = "horarios e obrigatorio")
    @Size(min = 1, message = "horarios deve ter ao menos um horario")
    @JsonFormat(pattern = "HH:mm")
    List<LocalTime> horarios,

    @Size(max = 1000, message = "orientacoesUso deve ter no maximo 1000 caracteres")
    String orientacoesUso,

    Boolean confirmarRiscoInteracao,

    @NotNull(message = "inicio e obrigatorio")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate inicio,

    @NotNull(message = "fim e obrigatorio")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fim
) {}
