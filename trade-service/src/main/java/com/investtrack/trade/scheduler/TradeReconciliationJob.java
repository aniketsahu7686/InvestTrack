package com.investtrack.trade.scheduler;

import com.investtrack.common.enums.TradeStatus;
import com.investtrack.trade.entity.TradeIdea;
import com.investtrack.trade.repository.TradeIdeaRepository;
import com.investtrack.trade.service.TradeIdeaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that periodically reconciles open trades.
 * <p>
 * - Expires INTRADAY trades that are still open after market close
 * - Expires SWING trades older than 14 days
 * - Expires POSITIONAL trades older than 90 days
 * <p>
 * Runs every hour to catch any trades that weren't auto-closed by price updates.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradeReconciliationJob {

    private final TradeIdeaRepository tradeIdeaRepository;
    private final TradeIdeaService tradeIdeaService;

    private static final int SWING_EXPIRY_DAYS = 14;
    private static final int POSITIONAL_EXPIRY_DAYS = 90;

    /**
     * Runs every hour to expire stale open trades based on their timeframe.
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void reconcileOpenTrades() {
        log.info("Starting trade reconciliation job...");

        List<TradeIdea> openTrades = tradeIdeaRepository.findByStatus(TradeStatus.OPEN);

        if (openTrades.isEmpty()) {
            log.info("No open trades to reconcile.");
            return;
        }

        log.info("Found {} open trades to evaluate for expiry", openTrades.size());

        int expiredCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (TradeIdea trade : openTrades) {
            try {
                if (shouldExpire(trade, now)) {
                    log.info("Expiring trade {}: symbol={}, timeframe={}, createdAt={}",
                            trade.getId(), trade.getStockSymbol(), trade.getTimeframe(), trade.getCreatedAt());
                    tradeIdeaService.autoCloseTrade(trade.getId(), TradeStatus.EXPIRED, trade.getEntryPrice());
                    expiredCount++;
                }
            } catch (Exception ex) {
                log.error("Error expiring trade {}: {}", trade.getId(), ex.getMessage(), ex);
            }
        }

        log.info("Trade reconciliation complete. Expired {} trades out of {} open.", expiredCount, openTrades.size());
    }

    /**
     * Determines if a trade should be expired based on its timeframe and age.
     */
    private boolean shouldExpire(TradeIdea trade, LocalDateTime now) {
        if (trade.getCreatedAt() == null) {
            return false;
        }

        return switch (trade.getTimeframe()) {
            case INTRADAY -> {
                // Expire intraday trades if created before today (past market close)
                yield trade.getCreatedAt().toLocalDate().isBefore(now.toLocalDate());
            }
            case SWING -> {
                // Expire swing trades older than 14 days
                yield trade.getCreatedAt().plusDays(SWING_EXPIRY_DAYS).isBefore(now);
            }
            case POSITIONAL -> {
                // Expire positional trades older than 90 days
                yield trade.getCreatedAt().plusDays(POSITIONAL_EXPIRY_DAYS).isBefore(now);
            }
        };
    }
}
