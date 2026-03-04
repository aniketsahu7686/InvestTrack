package com.investtrack.analytics.scheduler;

import com.investtrack.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that periodically recalculates all trader rankings.
 * <p>
 * Ensures that rankings stay consistent even if events were missed
 * or if the scoring algorithm has been updated.
 * <p>
 * Default schedule: every 6 hours.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RankingRecalculationJob {

    private final AnalyticsService analyticsService;

    @Scheduled(cron = "${ranking.recalculation-cron:0 0 */6 * * *}")
    public void recalculateRankings() {
        log.info("Starting scheduled ranking recalculation...");

        try {
            analyticsService.recalculateAllRankings();
            log.info("Scheduled ranking recalculation completed successfully.");
        } catch (Exception ex) {
            log.error("Error during scheduled ranking recalculation: {}", ex.getMessage(), ex);
        }
    }
}
