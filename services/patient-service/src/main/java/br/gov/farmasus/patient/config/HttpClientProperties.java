package br.gov.farmasus.patient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "http.client")
public record HttpClientProperties(
    long connectTimeoutMs,
    long readTimeoutMs) {
}
