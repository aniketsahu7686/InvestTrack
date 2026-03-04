package com.investtrack.analytics.config;

import com.investtrack.common.config.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the Analytics Service.
 * Declares the trade exchange, trade status queue, and bindings.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange tradeExchange() {
        return new TopicExchange(RabbitMQConstants.TRADE_EXCHANGE);
    }

    @Bean
    public Queue tradeStatusQueue() {
        return QueueBuilder.durable(RabbitMQConstants.TRADE_STATUS_QUEUE).build();
    }

    @Bean
    public Binding tradeStatusBinding(Queue tradeStatusQueue, TopicExchange tradeExchange) {
        return BindingBuilder.bind(tradeStatusQueue)
                .to(tradeExchange)
                .with(RabbitMQConstants.TRADE_STATUS_ROUTING_KEY + ".#");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
