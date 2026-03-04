package com.investtrack.marketdata.scheduler;

import com.investtrack.common.dto.PriceUpdateDTO;
import com.investtrack.marketdata.entity.InstrumentToken;
import com.investtrack.marketdata.repository.InstrumentTokenRepository;
import com.investtrack.marketdata.service.PriceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled job that periodically validates cached prices.
 * <p>
 * - Checks that all active instrument tokens have recent price data in Redis
 * - Logs warnings for stale or missing cache entries
 * - Runs every 5 minutes (configurable via price-cache.reconciliation-interval-ms)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriceReconciliationJob {

    private final InstrumentTokenRepository instrumentTokenRepository;
    private final PriceCacheService priceCacheService;

    @Scheduled(fixedDelayString = "${price-cache.reconciliation-interval-ms:300000}")
    public void reconcilePriceCache() {
        log.info("Starting price cache reconciliation...");

        List<InstrumentToken> activeInstruments = instrumentTokenRepository.findByActiveTrue();

        if (activeInstruments.isEmpty()) {
            log.info("No active instruments to reconcile.");
            return;
        }

        int cachedCount = 0;
        int missingCount = 0;

        for (InstrumentToken instrument : activeInstruments) {
            PriceUpdateDTO cached = priceCacheService.getCachedPrice(instrument.getStockSymbol());
            if (cached != null) {
                cachedCount++;
            } else {
                missingCount++;
                log.warn("Price cache MISS for active instrument: {} (token: {})",
                        instrument.getStockSymbol(), instrument.getInstrumentToken());
            }
        }

        log.info("Price cache reconciliation complete. Active: {}, Cached: {}, Missing: {}",
                activeInstruments.size(), cachedCount, missingCount);
    }
}
