package br.gov.farmasus.safety.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  public static final String EXCHANGE_NAME = "farma.events";
  public static final String MEDICATION_CREATED_QUEUE = "safety.medication.created";
  public static final String MEDICATION_CREATED_DLQ = "safety.medication.created.dlq";
  public static final String MEDICATION_CREATED_ROUTING_KEY = "medication.created";
  public static final String MEDICATION_CREATED_DLQ_ROUTING_KEY = "safety.medication.created.dlq";

  @Bean
  public TopicExchange farmaEventsExchange() {
    return new TopicExchange(EXCHANGE_NAME, true, false);
  }

  @Bean
  public Queue medicationCreatedDlq() {
    return QueueBuilder.durable(MEDICATION_CREATED_DLQ).build();
  }

  @Bean
  public Queue medicationCreatedQueue() {
    return QueueBuilder.durable(MEDICATION_CREATED_QUEUE)
        .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
        .withArgument("x-dead-letter-routing-key", MEDICATION_CREATED_DLQ_ROUTING_KEY)
        .build();
  }

  @Bean
  public Binding medicationCreatedBinding(TopicExchange farmaEventsExchange, Queue medicationCreatedQueue) {
    return BindingBuilder.bind(medicationCreatedQueue)
        .to(farmaEventsExchange)
        .with(MEDICATION_CREATED_ROUTING_KEY);
  }

  @Bean
  public Binding medicationCreatedDlqBinding(TopicExchange farmaEventsExchange, Queue medicationCreatedDlq) {
    return BindingBuilder.bind(medicationCreatedDlq)
        .to(farmaEventsExchange)
        .with(MEDICATION_CREATED_DLQ_ROUTING_KEY);
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
    factory.setAdviceChain(
        RetryInterceptorBuilder.stateless()
            .maxAttempts(3)
            .backOffOptions(1000, 2.0, 10000)
            .recoverer(new RejectAndDontRequeueRecoverer())
            .build());
    return factory;
  }
}
