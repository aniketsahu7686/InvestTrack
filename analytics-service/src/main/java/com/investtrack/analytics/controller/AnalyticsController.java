package com.investtrack.analytics.controller;

import com.investtrack.analytics.service.AnalyticsService;
import com.investtrack.common.dto.TraderRankingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for analytics and leaderboard endpoints.
 * <p>
 * - GET /analytics/leaderboard — paginated leaderboard sorted by ranking score
 * - GET /analytics/trader/{userId} — individual trader statistics
 */
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Trader analytics and leaderboard endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/leaderboard")
    @Operation(summary = "Get leaderboard", description = "Returns the paginated trader leaderboard sorted by overall ranking score")
    public ResponseEntity<Page<TraderRankingResponse>> getLeaderboard(
            @PageableDefault(size = 20, sort = "overallRankingScore", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Fetching leaderboard: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<TraderRankingResponse> leaderboard = analyticsService.getLeaderboard(pageable);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/trader/{userId}")
    @Operation(summary = "Get trader stats", description = "Returns detailed statistics for a specific trader")
    public ResponseEntity<TraderRankingResponse> getTraderStats(
            @Parameter(description = "Trader's user ID") @PathVariable UUID userId) {

        log.info("Fetching stats for trader: {}", userId);
        TraderRankingResponse stats = analyticsService.getTraderStats(userId);
        return ResponseEntity.ok(stats);
    }
}
