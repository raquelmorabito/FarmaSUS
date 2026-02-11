package br.gov.farmasus.auth.dto;

import br.gov.farmasus.auth.model.TipoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
    @NotBlank String login,
    @NotBlank String senha,
    @NotNull TipoUsuario tipoUsuario
) {}
