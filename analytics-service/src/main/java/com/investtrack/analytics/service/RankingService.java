package com.investtrack.analytics.service;

import com.investtrack.analytics.entity.TraderStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates the overall ranking score for a trader.
 * <p>
 * Formula:
 * Score = WinRate × W1 + NormalizedAvgRR × W2 + ConsistencyScore × W3 + RiskControlScore × W4
 * <p>
 * Defaults: W1=0.4, W2=0.3, W3=0.2, W4=0.1
 * <p>
 * All component scores are normalized to 0-100 scale before weighting.
 */
@Service
@Slf4j
public class RankingService {

    @Value("${ranking.weights.win-rate:0.4}")
    private BigDecimal winRateWeight;

    @Value("${ranking.weights.avg-risk-reward:0.3}")
    private BigDecimal avgRRWeight;

    @Value("${ranking.weights.consistency:0.2}")
    private BigDecimal consistencyWeight;

    @Value("${ranking.weights.risk-control:0.1}")
    private BigDecimal riskControlWeight;

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal MAX_RR_FOR_NORMALIZATION = new BigDecimal("5.0");

    /**
     * Calculates the overall ranking score for a trader based on their stats.
     *
     * @param stats the trader's current stats
     * @return the overall ranking score (0 to 100)
     */
    public BigDecimal calculateOverallScore(TraderStats stats) {
        BigDecimal normalizedWinRate = normalizeWinRate(stats.getWinRate());
        BigDecimal normalizedAvgRR = normalizeAvgRiskReward(stats.getAvgRiskReward());
        BigDecimal normalizedConsistency = stats.getConsistencyScore();
        BigDecimal normalizedRiskControl = stats.getRiskControlScore();

        BigDecimal score = normalizedWinRate.multiply(winRateWeight)
                .add(normalizedAvgRR.multiply(avgRRWeight))
                .add(normalizedConsistency.multiply(consistencyWeight))
                .add(normalizedRiskControl.multiply(riskControlWeight))
                .setScale(4, RoundingMode.HALF_UP);

        log.debug("Ranking score for userId={}: winRate={}, avgRR={}, consistency={}, riskControl={} → score={}",
                stats.getUserId(), normalizedWinRate, normalizedAvgRR,
                normalizedConsistency, normalizedRiskControl, score);

        return score;
    }

    /**
     * Recalculates all derived metrics for a TraderStats record.
     * Call this after incrementing wins/losses/expired counts.
     *
     * @param stats the stats to recalculate
     */
    public void recalculateStats(TraderStats stats) {
        // Win rate
        if (stats.getTotalTrades() > 0) {
            stats.setWinRate(
                    new BigDecimal(stats.getWins())
                            .divide(new BigDecimal(stats.getTotalTrades()), 4, RoundingMode.HALF_UP)
            );
        }

        // Average R:R
        if (stats.getTotalTrades() > 0) {
            stats.setAvgRiskReward(
                    stats.getTotalRiskRewardSum()
                            .divide(new BigDecimal(stats.getTotalTrades()), 2, RoundingMode.HALF_UP)
            );
        }

        // Consistency score: based on win rate stability
        stats.setConsistencyScore(calculateConsistencyScore(stats));

        // Risk control score: based on SL discipline (losses vs expired)
        stats.setRiskControlScore(calculateRiskControlScore(stats));

        // Overall ranking score
        stats.setOverallRankingScore(calculateOverallScore(stats));
    }

    /**
     * Normalizes win rate (0.0 - 1.0) to 0 - 100 scale.
     */
    private BigDecimal normalizeWinRate(BigDecimal winRate) {
        return winRate.multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Normalizes average R:R to 0 - 100 scale.
     * R:R of 5.0+ is considered maximum (100).
     */
    private BigDecimal normalizeAvgRiskReward(BigDecimal avgRR) {
        if (avgRR.compareTo(MAX_RR_FOR_NORMALIZATION) >= 0) {
            return HUNDRED;
        }
        return avgRR.divide(MAX_RR_FOR_NORMALIZATION, 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates consistency score (0 - 100).
     * <p>
     * Higher consistency = the trader wins at a steady rate.
     * Uses a simple heuristic based on win rate and having enough trades.
     * A win rate near 50% gets boosted if the trader has many trades.
     */
    private BigDecimal calculateConsistencyScore(TraderStats stats) {
        if (stats.getTotalTrades() < 3) {
            return BigDecimal.ZERO;
        }

        // Base consistency from trade volume (more trades = more data = more reliable)
        BigDecimal volumeFactor = BigDecimal.valueOf(
                Math.min(stats.getTotalTrades(), 100) / 100.0 * 40
        );

        // Streak bonus: positive streaks indicate consistency
        BigDecimal streakBonus = BigDecimal.valueOf(
                Math.min(stats.getBestStreak(), 10) * 3.0
        );

        // Win rate stability: consistent traders maintain steady win rates
        // Penalty for extreme imbalance (too many losses or only wins on few trades)
        BigDecimal winRateFactor = stats.getWinRate().multiply(new BigDecimal("30"))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal score = volumeFactor.add(streakBonus).add(winRateFactor);

        // Clamp to 0-100
        if (score.compareTo(HUNDRED) > 0) {
            score = HUNDRED;
        }
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            score = BigDecimal.ZERO;
        }

        return score.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates risk control score (0 - 100).
     * <p>
     * Traders who respect SL (SL_HIT rather than EXPIRED) get higher scores.
     * Fewer expired trades relative to total = better risk management.
     */
    private BigDecimal calculateRiskControlScore(TraderStats stats) {
        if (stats.getTotalTrades() == 0) {
            return BigDecimal.ZERO;
        }

        // SL discipline: trades that expire are "unmanaged" risk
        int managedTrades = stats.getWins() + stats.getLosses(); // TARGET_HIT + SL_HIT
        BigDecimal managedRatio = new BigDecimal(managedTrades)
                .divide(new BigDecimal(stats.getTotalTrades()), 4, RoundingMode.HALF_UP);

        // Managed ratio contributes 70% of risk control score
        BigDecimal managedScore = managedRatio.multiply(new BigDecimal("70"))
                .setScale(2, RoundingMode.HALF_UP);

        // Average R:R acts as a bonus (good R:R = controlled risk)
        BigDecimal rrBonus = stats.getAvgRiskReward()
                .min(new BigDecimal("3"))
                .multiply(new BigDecimal("10"))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal score = managedScore.add(rrBonus);

        // Clamp to 0-100
        if (score.compareTo(HUNDRED) > 0) {
            score = HUNDRED;
        }
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            score = BigDecimal.ZERO;
        }

        return score.setScale(2, RoundingMode.HALF_UP);
    }
}
