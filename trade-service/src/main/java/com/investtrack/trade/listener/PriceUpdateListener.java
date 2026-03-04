package com.investtrack.trade.listener;

import com.investtrack.common.enums.TradeStatus;
import com.investtrack.common.event.PriceUpdateEvent;
import com.investtrack.trade.entity.TradeIdea;
import com.investtrack.trade.repository.TradeIdeaRepository;
import com.investtrack.trade.service.TradeIdeaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Listens for PriceUpdateEvent messages from RabbitMQ (published by market-data-service).
 * <p>
 * When a price update arrives:
 * - Fetches all OPEN trades for that stock symbol
 * - If LTP >= targetPrice → auto-close as TARGET_HIT
 * - If LTP <= stopLoss → auto-close as SL_HIT
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriceUpdateListener {

    private final TradeIdeaRepository tradeIdeaRepository;
    private final TradeIdeaService tradeIdeaService;

    @RabbitListener(queues = "${investtrack.rabbitmq.price-update-queue:investtrack.price.update.queue}")
    public void handlePriceUpdate(PriceUpdateEvent event) {
        String symbol = event.getStockSymbol().toUpperCase();
        BigDecimal ltp = event.getLtp();

        log.info("Received price update: symbol={}, LTP={}", symbol, ltp);

        List<TradeIdea> openTrades = tradeIdeaRepository.findByStockSymbolAndStatus(symbol, TradeStatus.OPEN);

        if (openTrades.isEmpty()) {
            log.debug("No open trades found for symbol: {}", symbol);
            return;
        }

        log.info("Found {} open trades for symbol: {}", openTrades.size(), symbol);

        for (TradeIdea trade : openTrades) {
            try {
                evaluateTradeAgainstPrice(trade, ltp);
            } catch (Exception ex) {
                log.error("Error evaluating trade {} against price {}: {}",
                        trade.getId(), ltp, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Evaluates a single open trade against the current LTP.
     * - If LTP >= target → TARGET_HIT
     * - If LTP <= stopLoss → SL_HIT
     */
    private void evaluateTradeAgainstPrice(TradeIdea trade, BigDecimal ltp) {
        if (ltp.compareTo(trade.getTargetPrice()) >= 0) {
            log.info("TARGET_HIT for trade {}: LTP {} >= target {}",
                    trade.getId(), ltp, trade.getTargetPrice());
            tradeIdeaService.autoCloseTrade(trade.getId(), TradeStatus.TARGET_HIT, ltp);

        } else if (ltp.compareTo(trade.getStopLoss()) <= 0) {
            log.info("SL_HIT for trade {}: LTP {} <= stopLoss {}",
                    trade.getId(), ltp, trade.getStopLoss());
            tradeIdeaService.autoCloseTrade(trade.getId(), TradeStatus.SL_HIT, ltp);
        }
    }
}
