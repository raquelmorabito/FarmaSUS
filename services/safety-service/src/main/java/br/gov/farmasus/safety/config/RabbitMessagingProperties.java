package br.gov.farmasus.safety.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq.messaging")
public record RabbitMessagingProperties(
    Exchange exchange,
    Queue queue,
    Routing routing) {

  public record Exchange(String events) {
  }

  public record Queue(
      String medicationCreated,
      String medicationCreatedDlq) {
  }

  public record Routing(
      String medicationCreated,
      String medicationCreatedDlq,
      String safetyAlertCreated) {
  }
}
