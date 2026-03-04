package com.investtrack.analytics.service;

import com.investtrack.analytics.entity.TraderStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RankingService.class)
@TestPropertySource(properties = {
        "ranking.weights.win-rate=0.4",
        "ranking.weights.avg-risk-reward=0.3",
        "ranking.weights.consistency=0.2",
        "ranking.weights.risk-control=0.1"
})
class RankingServiceTest {

    @Autowired
    private RankingService rankingService;

    @Test
    @DisplayName("Should calculate ranking score for trader with good stats")
    void shouldCalculateRankingForGoodTrader() {
        TraderStats stats = TraderStats.builder()
                .userId(UUID.randomUUID())
                .totalTrades(20)
                .wins(14)
                .losses(4)
                .expired(2)
                .winRate(new BigDecimal("0.7000"))
                .avgRiskReward(new BigDecimal("2.50"))
                .totalRiskRewardSum(new BigDecimal("50.00"))
                .consistencyScore(new BigDecimal("60.00"))
                .riskControlScore(new BigDecimal("80.00"))
                .currentStreak(3)
                .bestStreak(5)
                .build();

        BigDecimal score = rankingService.calculateOverallScore(stats);

        assertThat(score).isNotNull();
        assertThat(score).isGreaterThan(BigDecimal.ZERO);
        // Win rate: 70 * 0.4 = 28
        // Avg RR: (2.5/5.0)*100 = 50 * 0.3 = 15
        // Consistency: 60 * 0.2 = 12
        // Risk control: 80 * 0.1 = 8
        // Total ≈ 63
        assertThat(score).isGreaterThan(new BigDecimal("50"));
    }

    @Test
    @DisplayName("Should calculate low ranking for poor trader")
    void shouldCalculateLowRankingForPoorTrader() {
        TraderStats stats = TraderStats.builder()
                .userId(UUID.randomUUID())
                .totalTrades(10)
                .wins(2)
                .losses(6)
                .expired(2)
                .winRate(new BigDecimal("0.2000"))
                .avgRiskReward(new BigDecimal("1.00"))
                .totalRiskRewardSum(new BigDecimal("10.00"))
                .consistencyScore(new BigDecimal("20.00"))
                .riskControlScore(new BigDecimal("30.00"))
                .currentStreak(-3)
                .bestStreak(1)
                .build();

        BigDecimal score = rankingService.calculateOverallScore(stats);

        assertThat(score).isNotNull();
        assertThat(score).isLessThan(new BigDecimal("30"));
    }

    @Test
    @DisplayName("Should handle zero trades gracefully")
    void shouldHandleZeroTrades() {
        TraderStats stats = TraderStats.builder()
                .userId(UUID.randomUUID())
                .totalTrades(0)
                .wins(0)
                .losses(0)
                .expired(0)
                .winRate(BigDecimal.ZERO)
                .avgRiskReward(BigDecimal.ZERO)
                .totalRiskRewardSum(BigDecimal.ZERO)
                .consistencyScore(BigDecimal.ZERO)
                .riskControlScore(BigDecimal.ZERO)
                .build();

        BigDecimal score = rankingService.calculateOverallScore(stats);

        assertThat(score).isNotNull();
        assertThat(score.compareTo(BigDecimal.ZERO)).isEqualTo(0);
    }

    @Test
    @DisplayName("Should recalculate all stats correctly")
    void shouldRecalculateStats() {
        TraderStats stats = TraderStats.builder()
                .userId(UUID.randomUUID())
                .totalTrades(10)
                .wins(7)
                .losses(2)
                .expired(1)
                .totalRiskRewardSum(new BigDecimal("22.50"))
                .currentStreak(3)
                .bestStreak(4)
                .build();

        rankingService.recalculateStats(stats);

        assertThat(stats.getWinRate()).isEqualByComparingTo(new BigDecimal("0.7000"));
        assertThat(stats.getAvgRiskReward()).isEqualByComparingTo(new BigDecimal("2.25"));
        assertThat(stats.getConsistencyScore()).isGreaterThan(BigDecimal.ZERO);
        assertThat(stats.getRiskControlScore()).isGreaterThan(BigDecimal.ZERO);
        assertThat(stats.getOverallRankingScore()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should cap normalized avg RR at 100")
    void shouldCapNormalizedAvgRR() {
        TraderStats stats = TraderStats.builder()
                .userId(UUID.randomUUID())
                .totalTrades(5)
                .wins(5)
                .losses(0)
                .expired(0)
                .winRate(new BigDecimal("1.0000"))
                .avgRiskReward(new BigDecimal("10.00")) // Above max (5.0)
                .totalRiskRewardSum(new BigDecimal("50.00"))
                .consistencyScore(new BigDecimal("100.00"))
                .riskControlScore(new BigDecimal("100.00"))
                .currentStreak(5)
                .bestStreak(5)
                .build();

        BigDecimal score = rankingService.calculateOverallScore(stats);

        // All components maxed: 100*0.4 + 100*0.3 + 100*0.2 + 100*0.1 = 100
        assertThat(score).isEqualByComparingTo(new BigDecimal("100.0000"));
    }
}
