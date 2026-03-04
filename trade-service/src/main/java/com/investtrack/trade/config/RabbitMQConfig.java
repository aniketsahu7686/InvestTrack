package com.investtrack.trade.config;

import com.investtrack.common.config.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the Trade Service.
 * Declares exchanges, queues, and bindings for:
 * - Consuming price update events
 * - Publishing trade status events
 */
@Configuration
public class RabbitMQConfig {

    // ─── Exchanges ──────────────────────────────────────────

    @Bean
    public TopicExchange tradeExchange() {
        return new TopicExchange(RabbitMQConstants.TRADE_EXCHANGE);
    }

    @Bean
    public TopicExchange priceExchange() {
        return new TopicExchange(RabbitMQConstants.PRICE_EXCHANGE);
    }

    // ─── Queues ─────────────────────────────────────────────

    @Bean
    public Queue tradeStatusQueue() {
        return new Queue(RabbitMQConstants.TRADE_STATUS_QUEUE, true);
    }

    @Bean
    public Queue priceUpdateQueue() {
        return new Queue(RabbitMQConstants.PRICE_UPDATE_QUEUE, true);
    }

    // ─── Bindings ───────────────────────────────────────────

    @Bean
    public Binding tradeStatusBinding(Queue tradeStatusQueue, TopicExchange tradeExchange) {
        return BindingBuilder.bind(tradeStatusQueue)
                .to(tradeExchange)
                .with(RabbitMQConstants.TRADE_STATUS_ROUTING_KEY);
    }

    @Bean
    public Binding priceUpdateBinding(Queue priceUpdateQueue, TopicExchange priceExchange) {
        return BindingBuilder.bind(priceUpdateQueue)
                .to(priceExchange)
                .with(RabbitMQConstants.PRICE_UPDATE_ROUTING_KEY);
    }

    // ─── Message Converter ──────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
