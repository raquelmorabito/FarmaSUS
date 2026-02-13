package br.gov.farmasus.adherence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq.messaging")
public record RabbitMessagingProperties(
    Exchange exchange,
    Queue queue,
    Routing routing) {

  public record Exchange(String events) {
  }

  public record Queue(
      String prescriptionCreated,
      String prescriptionCreatedDlq) {
  }

  public record Routing(
      String prescriptionCreated,
      String prescriptionCreatedCompat,
      String prescriptionCreatedDlq,
      String doseRegistered) {
  }
}
