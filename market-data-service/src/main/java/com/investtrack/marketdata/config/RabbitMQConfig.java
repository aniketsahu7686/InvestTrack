package com.investtrack.marketdata.config;

import com.investtrack.common.config.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the Market Data Service.
 * Declares the price exchange and bindings for publishing price update events.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange priceExchange() {
        return new TopicExchange(RabbitMQConstants.PRICE_EXCHANGE);
    }

    @Bean
    public Queue priceUpdateQueue() {
        return QueueBuilder.durable(RabbitMQConstants.PRICE_UPDATE_QUEUE).build();
    }

    @Bean
    public Binding priceUpdateBinding(Queue priceUpdateQueue, TopicExchange priceExchange) {
        return BindingBuilder.bind(priceUpdateQueue)
                .to(priceExchange)
                .with(RabbitMQConstants.PRICE_UPDATE_ROUTING_KEY + ".#");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
