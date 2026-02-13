package br.gov.farmasus.adherence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, Integer expiracaoMinutos) {}
