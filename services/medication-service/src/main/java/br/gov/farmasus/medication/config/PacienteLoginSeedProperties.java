package br.gov.farmasus.medication.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seed.paciente-login")
public record PacienteLoginSeedProperties(Map<String, Long> mappings) {
}
