package br.gov.farmasus.patient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq.messaging")
public record RabbitMessagingProperties(
    Exchange exchange,
    Queue queue,
    Routing routing) {

  public record Exchange(String events) {
  }

  public record Queue(
      String alertCreated,
      String alertCreatedDlq) {
  }

  public record Routing(
      String alertCreated,
      String alertCreatedDlq) {
  }
}
