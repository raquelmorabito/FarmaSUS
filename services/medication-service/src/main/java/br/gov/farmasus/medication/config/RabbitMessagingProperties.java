package br.gov.farmasus.medication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq.messaging")
public record RabbitMessagingProperties(
    Exchange exchange,
    Routing routing) {

  public record Exchange(String events) {
  }

  public record Routing(
      String medicationCreated,
      String medicationPrescriptionExpiring) {
  }
}
