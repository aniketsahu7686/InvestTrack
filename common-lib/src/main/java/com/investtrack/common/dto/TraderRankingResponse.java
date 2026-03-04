package com.investtrack.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for trader ranking on the leaderboard.
 * Contains all computed performance metrics and overall rank.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraderRankingResponse {

    private UUID userId;
    private String username;
    private int totalTrades;
    private int wins;
    private int losses;
    private BigDecimal winRate;
    private BigDecimal avgRiskReward;
    private BigDecimal consistencyScore;
    private BigDecimal riskControlScore;
    private BigDecimal overallRankingScore;
    private int currentStreak;
    private int bestStreak;
    private int rank;
}
