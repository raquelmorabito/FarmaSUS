package br.gov.farmasus.auth.model;

public record Usuario(String login, String senha, TipoUsuario tipoUsuario) {}
