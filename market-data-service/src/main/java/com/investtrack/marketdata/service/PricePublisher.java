package com.investtrack.marketdata.service;

import com.investtrack.common.config.RabbitMQConstants;
import com.investtrack.common.dto.PriceUpdateDTO;
import com.investtrack.common.event.PriceUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes PriceUpdateEvent messages to RabbitMQ.
 * <p>
 * The trade-service listens for these events to auto-close trades
 * when LTP hits target or stop-loss levels.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PricePublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publishes a price update event for a stock symbol.
     *
     * @param priceUpdate the price data to publish
     */
    public void publishPriceUpdate(PriceUpdateDTO priceUpdate) {
        String routingKey = RabbitMQConstants.PRICE_UPDATE_ROUTING_KEY + "." + priceUpdate.getStockSymbol().toLowerCase();

        PriceUpdateEvent event = PriceUpdateEvent.builder()
                .stockSymbol(priceUpdate.getStockSymbol())
                .ltp(priceUpdate.getLtp())
                .timestamp(priceUpdate.getTimestamp())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.PRICE_EXCHANGE,
                routingKey,
                event
        );

        log.debug("Published price update: symbol={}, LTP={}", priceUpdate.getStockSymbol(), priceUpdate.getLtp());
    }
}
