package br.gov.farmasus.adherence.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  private final RabbitMessagingProperties rabbitMessagingProperties;

  public RabbitConfig(RabbitMessagingProperties rabbitMessagingProperties) {
    this.rabbitMessagingProperties = rabbitMessagingProperties;
  }

  @Bean
  public TopicExchange eventsExchange() {
    return new TopicExchange(rabbitMessagingProperties.exchange().events(), true, false);
  }

  @Bean
  public Queue prescricaoCriadaDlq() {
    return QueueBuilder.durable(rabbitMessagingProperties.queue().prescriptionCreatedDlq()).build();
  }

  @Bean
  public Queue prescricaoCriadaQueue() {
    return QueueBuilder.durable(rabbitMessagingProperties.queue().prescriptionCreated())
        .withArgument("x-dead-letter-exchange", rabbitMessagingProperties.exchange().events())
        .withArgument("x-dead-letter-routing-key", rabbitMessagingProperties.routing().prescriptionCreatedDlq())
        .build();
  }

  @Bean
  public Binding prescricaoCriadaBinding(TopicExchange eventsExchange, Queue prescricaoCriadaQueue) {
    return BindingBuilder.bind(prescricaoCriadaQueue)
        .to(eventsExchange)
        .with(rabbitMessagingProperties.routing().prescriptionCreated());
  }

  @Bean
  public Binding prescricaoCriadaBindingCompat(TopicExchange eventsExchange, Queue prescricaoCriadaQueue) {
    return BindingBuilder.bind(prescricaoCriadaQueue)
        .to(eventsExchange)
        .with(rabbitMessagingProperties.routing().prescriptionCreatedCompat());
  }

  @Bean
  public Binding prescricaoCriadaDlqBinding(TopicExchange eventsExchange, Queue prescricaoCriadaDlq) {
    return BindingBuilder.bind(prescricaoCriadaDlq)
        .to(eventsExchange)
        .with(rabbitMessagingProperties.routing().prescriptionCreatedDlq());
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
