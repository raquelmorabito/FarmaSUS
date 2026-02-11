package br.gov.farmasus.patient.dto;

import br.gov.farmasus.patient.domain.Severidade;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertaInteracaoCriadoEvent(
    UUID eventId,
    UUID correlationId,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime criadoEm,
    Long alertaId,
    Long pacienteId,
    Long prescricaoId,
    Severidade severidade,
    String mensagem,
    String recomendacao
) {}
