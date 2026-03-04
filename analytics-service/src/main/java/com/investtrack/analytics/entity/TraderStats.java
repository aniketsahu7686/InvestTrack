package com.investtrack.analytics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores aggregated performance statistics for each trader.
 * <p>
 * Updated whenever a TradeStatusEvent is received (trade closed).
 * Used to calculate the overall ranking score for the leaderboard.
 * <p>
 * Ranking formula:
 * Score = WinRate × 0.4 + NormalizedAvgRR × 0.3 + ConsistencyScore × 0.2 + RiskControlScore × 0.1
 */
@Entity
@Table(name = "trader_stats", indexes = {
        @Index(name = "idx_trader_stats_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_trader_stats_ranking", columnList = "overall_ranking_score DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraderStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The user ID this stats record belongs to (from auth-service).
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    /**
     * Username for display purposes (denormalized for performance).
     */
    @Column(name = "username", length = 50)
    private String username;

    /**
     * Total number of closed trades (TARGET_HIT + SL_HIT + EXPIRED).
     */
    @Column(name = "total_trades", nullable = false)
    @Builder.Default
    private int totalTrades = 0;

    /**
     * Number of trades that hit target (TARGET_HIT).
     */
    @Column(name = "wins", nullable = false)
    @Builder.Default
    private int wins = 0;

    /**
     * Number of trades that hit stop loss (SL_HIT).
     */
    @Column(name = "losses", nullable = false)
    @Builder.Default
    private int losses = 0;

    /**
     * Number of expired trades (EXPIRED).
     */
    @Column(name = "expired", nullable = false)
    @Builder.Default
    private int expired = 0;

    /**
     * Win rate: wins / totalTrades (0.0 to 1.0).
     */
    @Column(name = "win_rate", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal winRate = BigDecimal.ZERO;

    /**
     * Average risk/reward ratio across all closed trades.
     */
    @Column(name = "avg_risk_reward", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal avgRiskReward = BigDecimal.ZERO;

    /**
     * Sum of all R:R ratios (used for incremental average calculation).
     */
    @Column(name = "total_risk_reward_sum", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalRiskRewardSum = BigDecimal.ZERO;

    /**
     * Consistency score (0 to 100): measures how consistently the trader performs.
     * Based on variance in outcomes — lower variance = higher consistency.
     */
    @Column(name = "consistency_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal consistencyScore = BigDecimal.ZERO;

    /**
     * Risk control score (0 to 100): based on average risk percentage and SL discipline.
     */
    @Column(name = "risk_control_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal riskControlScore = BigDecimal.ZERO;

    /**
     * The overall ranking score, calculated from the weighted formula.
     */
    @Column(name = "overall_ranking_score", nullable = false, precision = 7, scale = 4)
    @Builder.Default
    private BigDecimal overallRankingScore = BigDecimal.ZERO;

    /**
     * Current streak: positive for consecutive wins, negative for consecutive losses.
     */
    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private int currentStreak = 0;

    /**
     * Best win streak ever achieved.
     */
    @Column(name = "best_streak", nullable = false)
    @Builder.Default
    private int bestStreak = 0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
