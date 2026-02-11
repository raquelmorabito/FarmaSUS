package br.gov.farmasus.auth.dto;

public record MeResponse(
    String login,
    String tipoUsuario
) {}
