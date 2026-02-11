package br.gov.farmasus.adherence.config;

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
  public static final String EXCHANGE_EVENTS = "farma.events";
  public static final String PRESCRICAO_CRIADA_QUEUE = "adherence.medication.prescription.created";
  public static final String PRESCRICAO_CRIADA_ROUTING_KEY = "medication.prescription.created";
  public static final String PRESCRICAO_CRIADA_ROUTING_KEY_COMPAT = "medication.created";
  public static final String DOSE_REGISTRADA_ROUTING_KEY = "adherence.dose.registered";

  @Bean
  public TopicExchange eventsExchange() {
    return new TopicExchange(EXCHANGE_EVENTS, true, false);
  }

  @Bean
  public Queue prescricaoCriadaQueue() {
    return QueueBuilder.durable(PRESCRICAO_CRIADA_QUEUE).build();
  }

  @Bean
  public Binding prescricaoCriadaBinding(TopicExchange eventsExchange, Queue prescricaoCriadaQueue) {
    return BindingBuilder.bind(prescricaoCriadaQueue)
        .to(eventsExchange)
        .with(PRESCRICAO_CRIADA_ROUTING_KEY);
  }

  @Bean
  public Binding prescricaoCriadaBindingCompat(TopicExchange eventsExchange, Queue prescricaoCriadaQueue) {
    return BindingBuilder.bind(prescricaoCriadaQueue)
        .to(eventsExchange)
        .with(PRESCRICAO_CRIADA_ROUTING_KEY_COMPAT);
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
