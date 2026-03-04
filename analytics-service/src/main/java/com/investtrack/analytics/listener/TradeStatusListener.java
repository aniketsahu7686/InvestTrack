package com.investtrack.analytics.listener;

import com.investtrack.analytics.service.AnalyticsService;
import com.investtrack.common.config.RabbitMQConstants;
import com.investtrack.common.event.TradeStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for TradeStatusEvent messages from RabbitMQ.
 * <p>
 * When a trade is closed (TARGET_HIT, SL_HIT, or EXPIRED), this listener
 * receives the event and delegates to AnalyticsService to update the
 * trader's statistics and recalculate their ranking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradeStatusListener {

    private final AnalyticsService analyticsService;

    @RabbitListener(queues = RabbitMQConstants.TRADE_STATUS_QUEUE)
    public void handleTradeStatusEvent(TradeStatusEvent event) {
        log.info("Received TradeStatusEvent: tradeId={}, userId={}, status={}",
                event.getTradeId(), event.getUserId(), event.getNewStatus());

        try {
            analyticsService.processTradeStatusEvent(event);
            log.info("Successfully processed TradeStatusEvent for tradeId={}", event.getTradeId());
        } catch (Exception ex) {
            log.error("Error processing TradeStatusEvent for tradeId={}: {}",
                    event.getTradeId(), ex.getMessage(), ex);
            // In production, consider DLQ or retry mechanism
            throw ex;
        }
    }
}
