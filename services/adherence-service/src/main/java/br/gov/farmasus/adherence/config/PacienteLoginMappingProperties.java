package br.gov.farmasus.adherence.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.paciente-login")
public record PacienteLoginMappingProperties(Map<String, Long> mappings) {
}
