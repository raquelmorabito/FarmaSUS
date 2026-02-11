package br.gov.farmasus.patient.config;

import br.gov.farmasus.patient.domain.PacienteLogin;
import br.gov.farmasus.patient.repository.PacienteLoginRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("docker")
public class DockerSeedConfig {
  @Bean
  public CommandLineRunner seedPacienteLogin(PacienteLoginRepository repository) {
    return args -> {
      if (repository.findByLogin("paciente1").isEmpty()) {
        PacienteLogin paciente1 = new PacienteLogin();
        paciente1.setLogin("paciente1");
        paciente1.setPacienteId(1L);
        repository.save(paciente1);
      }
      if (repository.findByLogin("paciente2").isEmpty()) {
        PacienteLogin paciente2 = new PacienteLogin();
        paciente2.setLogin("paciente2");
        paciente2.setPacienteId(2L);
        repository.save(paciente2);
      }
    };
  }
}
