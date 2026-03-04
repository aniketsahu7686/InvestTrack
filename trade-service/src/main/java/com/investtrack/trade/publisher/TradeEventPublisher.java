package com.investtrack.trade.publisher;

import com.investtrack.common.config.RabbitMQConstants;
import com.investtrack.common.event.TradeStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes TradeStatusEvent messages to RabbitMQ when a trade's status changes.
 * Events are consumed by the analytics-service to update trader rankings.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradeEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publishes a TradeStatusEvent to the trade exchange with status-based routing.
     *
     * @param event the trade status event to publish
     */
    public void publishTradeStatusEvent(TradeStatusEvent event) {
        String routingKey = RabbitMQConstants.TRADE_STATUS_ROUTING_KEY + "." + event.getNewStatus().name().toLowerCase();

        log.info("Publishing TradeStatusEvent: tradeId={}, status={}, routingKey={}",
                event.getTradeId(), event.getNewStatus(), routingKey);

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.TRADE_EXCHANGE,
                routingKey,
                event
        );

        log.debug("TradeStatusEvent published successfully for tradeId={}", event.getTradeId());
    }
}
