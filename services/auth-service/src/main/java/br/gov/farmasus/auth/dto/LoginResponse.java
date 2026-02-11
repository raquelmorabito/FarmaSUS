package br.gov.farmasus.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

public record LoginResponse(
    String token,
    String tipoUsuario,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    OffsetDateTime expiraEm
) {}
