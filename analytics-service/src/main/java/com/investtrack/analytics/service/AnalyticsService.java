package com.investtrack.analytics.service;

import com.investtrack.analytics.entity.TraderStats;
import com.investtrack.analytics.repository.TraderStatsRepository;
import com.investtrack.common.dto.TraderRankingResponse;
import com.investtrack.common.enums.TradeStatus;
import com.investtrack.common.event.TradeStatusEvent;
import com.investtrack.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for managing trader statistics and leaderboard.
 * <p>
 * Handles:
 * - Processing trade status events (incrementing win/loss/expired counts)
 * - Recalculating trader rankings
 * - Serving leaderboard and individual trader stats
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TraderStatsRepository traderStatsRepository;
    private final RankingService rankingService;

    @Value("${ranking.min-trades-for-ranking:5}")
    private int minTradesForRanking;

    /**
     * Processes a trade status event and updates the trader's stats.
     *
     * @param event the trade status change event
     */
    @Transactional
    public void processTradeStatusEvent(TradeStatusEvent event) {
        log.info("Processing trade status event: tradeId={}, userId={}, status={}",
                event.getTradeId(), event.getUserId(), event.getNewStatus());

        TraderStats stats = traderStatsRepository.findByUserId(event.getUserId())
                .orElseGet(() -> createNewStats(event.getUserId()));

        // Increment appropriate counter
        stats.setTotalTrades(stats.getTotalTrades() + 1);
        stats.setTotalRiskRewardSum(stats.getTotalRiskRewardSum().add(event.getRiskRewardRatio()));

        switch (event.getNewStatus()) {
            case TARGET_HIT -> {
                stats.setWins(stats.getWins() + 1);
                updateStreak(stats, true);
            }
            case SL_HIT -> {
                stats.setLosses(stats.getLosses() + 1);
                updateStreak(stats, false);
            }
            case EXPIRED -> {
                stats.setExpired(stats.getExpired() + 1);
                // Expired trades break any active streak
                stats.setCurrentStreak(0);
            }
            default -> log.warn("Unexpected trade status: {}", event.getNewStatus());
        }

        // Recalculate all derived metrics
        rankingService.recalculateStats(stats);

        traderStatsRepository.save(stats);
        log.info("Updated stats for userId={}: totalTrades={}, winRate={}, rankingScore={}",
                event.getUserId(), stats.getTotalTrades(), stats.getWinRate(), stats.getOverallRankingScore());
    }

    /**
     * Gets the leaderboard with pagination.
     * Only traders with at least minTradesForRanking trades are included.
     */
    @Transactional(readOnly = true)
    public Page<TraderRankingResponse> getLeaderboard(Pageable pageable) {
        return traderStatsRepository.findLeaderboard(minTradesForRanking, pageable)
                .map(this::toRankingResponse);
    }

    /**
     * Gets stats for a specific trader.
     */
    @Transactional(readOnly = true)
    public TraderRankingResponse getTraderStats(UUID userId) {
        TraderStats stats = traderStatsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("TraderStats", "userId", userId));
        return toRankingResponse(stats);
    }

    /**
     * Recalculates rankings for all traders. Called by the scheduled job.
     */
    @Transactional
    public void recalculateAllRankings() {
        log.info("Starting full ranking recalculation...");
        var allStats = traderStatsRepository.findAllWithTrades();

        int count = 0;
        for (TraderStats stats : allStats) {
            rankingService.recalculateStats(stats);
            traderStatsRepository.save(stats);
            count++;
        }

        log.info("Ranking recalculation complete. Updated {} traders.", count);
    }

    /**
     * Creates a new TraderStats record for a first-time trader.
     */
    private TraderStats createNewStats(UUID userId) {
        log.info("Creating new TraderStats for userId={}", userId);
        return TraderStats.builder()
                .userId(userId)
                .build();
    }

    /**
     * Updates the win/loss streak.
     */
    private void updateStreak(TraderStats stats, boolean isWin) {
        if (isWin) {
            if (stats.getCurrentStreak() >= 0) {
                stats.setCurrentStreak(stats.getCurrentStreak() + 1);
            } else {
                stats.setCurrentStreak(1); // Reset from losing streak
            }
            // Update best streak
            if (stats.getCurrentStreak() > stats.getBestStreak()) {
                stats.setBestStreak(stats.getCurrentStreak());
            }
        } else {
            if (stats.getCurrentStreak() <= 0) {
                stats.setCurrentStreak(stats.getCurrentStreak() - 1);
            } else {
                stats.setCurrentStreak(-1); // Reset from winning streak
            }
        }
    }

    /**
     * Maps TraderStats entity to TraderRankingResponse DTO.
     */
    private TraderRankingResponse toRankingResponse(TraderStats stats) {
        return TraderRankingResponse.builder()
                .userId(stats.getUserId())
                .username(stats.getUsername())
                .totalTrades(stats.getTotalTrades())
                .wins(stats.getWins())
                .losses(stats.getLosses())
                .winRate(stats.getWinRate())
                .avgRiskReward(stats.getAvgRiskReward())
                .consistencyScore(stats.getConsistencyScore())
                .riskControlScore(stats.getRiskControlScore())
                .overallRankingScore(stats.getOverallRankingScore())
                .currentStreak(stats.getCurrentStreak())
                .bestStreak(stats.getBestStreak())
                .build();
    }
}
