package com.investtrack.analytics.controller;

import com.investtrack.analytics.service.AnalyticsService;
import com.investtrack.common.dto.TraderRankingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    private TraderRankingResponse sampleRanking;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleRanking = TraderRankingResponse.builder()
                .userId(userId)
                .username("toptrader")
                .totalTrades(50)
                .wins(35)
                .losses(12)
                .winRate(new BigDecimal("0.7000"))
                .avgRiskReward(new BigDecimal("2.50"))
                .consistencyScore(new BigDecimal("75.00"))
                .riskControlScore(new BigDecimal("85.00"))
                .overallRankingScore(new BigDecimal("72.5000"))
                .currentStreak(5)
                .bestStreak(8)
                .rank(1)
                .build();
    }

    @Test
    @DisplayName("GET /analytics/leaderboard — should return paginated leaderboard")
    void shouldReturnLeaderboard() throws Exception {
        Page<TraderRankingResponse> page = new PageImpl<>(List.of(sampleRanking));
        when(analyticsService.getLeaderboard(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/analytics/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").value("toptrader"))
                .andExpect(jsonPath("$.content[0].totalTrades").value(50))
                .andExpect(jsonPath("$.content[0].winRate").value(0.7000));
    }

    @Test
    @DisplayName("GET /analytics/trader/{userId} — should return trader stats")
    void shouldReturnTraderStats() throws Exception {
        when(analyticsService.getTraderStats(userId)).thenReturn(sampleRanking);

        mockMvc.perform(get("/analytics/trader/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("toptrader"))
                .andExpect(jsonPath("$.totalTrades").value(50))
                .andExpect(jsonPath("$.wins").value(35))
                .andExpect(jsonPath("$.currentStreak").value(5))
                .andExpect(jsonPath("$.bestStreak").value(8));
    }

    @Test
    @DisplayName("GET /analytics/leaderboard — should support pagination params")
    void shouldSupportPagination() throws Exception {
        Page<TraderRankingResponse> page = new PageImpl<>(List.of(sampleRanking));
        when(analyticsService.getLeaderboard(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/analytics/leaderboard")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
