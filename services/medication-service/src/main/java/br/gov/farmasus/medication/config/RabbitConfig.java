package br.gov.farmasus.medication.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
  public org.springframework.amqp.rabbit.core.RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
  org.springframework.amqp.rabbit.core.RabbitAdmin admin = new org.springframework.amqp.rabbit.core.RabbitAdmin(connectionFactory);
  admin.setAutoStartup(true);
      return admin;
    }
}
