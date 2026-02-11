package br.gov.farmasus.patient.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  public static final String EXCHANGE_NAME = "farma.events";
  public static final String ALERTA_CRIADO_QUEUE = "patient.safety.alert.created";
  public static final String ALERTA_CRIADO_DLQ = "patient.safety.alert.created.dlq";
  public static final String ALERTA_CRIADO_ROUTING_KEY = "safety.alert.created";
  public static final String ALERTA_CRIADO_DLQ_ROUTING_KEY = "patient.safety.alert.created.dlq";

  @Bean
  public TopicExchange farmaEventsExchange() {
    return new TopicExchange(EXCHANGE_NAME, true, false);
  }

  @Bean
  public Queue alertaCriadoDlq() {
    return QueueBuilder.durable(ALERTA_CRIADO_DLQ).build();
  }

  @Bean
  public Queue alertaCriadoQueue() {
    return QueueBuilder.durable(ALERTA_CRIADO_QUEUE)
        .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
        .withArgument("x-dead-letter-routing-key", ALERTA_CRIADO_DLQ_ROUTING_KEY)
        .build();
  }

  @Bean
  public Binding alertaCriadoBinding(TopicExchange farmaEventsExchange, Queue alertaCriadoQueue) {
    return BindingBuilder.bind(alertaCriadoQueue)
        .to(farmaEventsExchange)
        .with(ALERTA_CRIADO_ROUTING_KEY);
  }

  @Bean
  public Binding alertaCriadoDlqBinding(TopicExchange farmaEventsExchange, Queue alertaCriadoDlq) {
    return BindingBuilder.bind(alertaCriadoDlq)
        .to(farmaEventsExchange)
        .with(ALERTA_CRIADO_DLQ_ROUTING_KEY);
  }

  @Bean
  public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(converter);
    return template;
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      Jackson2JsonMessageConverter converter) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(converter);
    factory.setDefaultRequeueRejected(false);
    return factory;
  }
}
