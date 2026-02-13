package br.gov.farmasus.medication.config;

import br.gov.farmasus.medication.domain.PacienteLogin;
import br.gov.farmasus.medication.repository.PacienteLoginRepository;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("docker")
public class DockerSeedConfig {
  @Bean
  public CommandLineRunner seedPacienteLogin(
      PacienteLoginRepository repository,
      PacienteLoginSeedProperties seedProperties) {
    return args -> {
      Map<String, Long> mappings = seedProperties.mappings();
      if (mappings == null || mappings.isEmpty()) {
        return;
      }
      for (Map.Entry<String, Long> entry : mappings.entrySet()) {
        if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null) {
          continue;
        }
        if (repository.findByLogin(entry.getKey()).isPresent()) {
          continue;
        }
        PacienteLogin pacienteLogin = new PacienteLogin();
        pacienteLogin.setLogin(entry.getKey());
        pacienteLogin.setPacienteId(entry.getValue());
        repository.save(pacienteLogin);
      }
    };
  }
}
