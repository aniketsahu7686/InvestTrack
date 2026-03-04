package com.investtrack.analytics.repository;

import com.investtrack.analytics.entity.TraderStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TraderStats entities.
 * Provides methods for leaderboard queries and individual trader lookups.
 */
@Repository
public interface TraderStatsRepository extends JpaRepository<TraderStats, UUID> {

    /**
     * Find stats for a specific user.
     */
    Optional<TraderStats> findByUserId(UUID userId);

    /**
     * Get the leaderboard: all traders sorted by ranking score descending.
     */
    List<TraderStats> findAllByOrderByOverallRankingScoreDesc();

    /**
     * Get the paginated leaderboard with minimum trades requirement.
     */
    @Query("SELECT ts FROM TraderStats ts WHERE ts.totalTrades >= :minTrades ORDER BY ts.overallRankingScore DESC")
    Page<TraderStats> findLeaderboard(int minTrades, Pageable pageable);

    /**
     * Check if stats exist for a user.
     */
    boolean existsByUserId(UUID userId);

    /**
     * Get all stats records (for batch recalculation).
     */
    @Query("SELECT ts FROM TraderStats ts WHERE ts.totalTrades > 0")
    List<TraderStats> findAllWithTrades();
}
