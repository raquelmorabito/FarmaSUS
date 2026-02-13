package br.gov.farmasus.safety;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SafetyServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(SafetyServiceApplication.class, args);
  }
}
