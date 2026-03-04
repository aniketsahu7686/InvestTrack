package com.investtrack.trade.repository;

import com.investtrack.common.enums.TradeStatus;
import com.investtrack.trade.entity.TradeIdea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for TradeIdea entity.
 * Extends JpaSpecificationExecutor for dynamic filtering with Specifications.
 */
@Repository
public interface TradeIdeaRepository extends JpaRepository<TradeIdea, UUID>,
        JpaSpecificationExecutor<TradeIdea> {

    /**
     * Find all trade ideas by a specific user.
     */
    List<TradeIdea> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find all open trades for a specific stock symbol.
     * Used by the price update listener to auto-close trades.
     */
    List<TradeIdea> findByStockSymbolAndStatus(String stockSymbol, TradeStatus status);

    /**
     * Find all open trades across all symbols.
     * Used by the reconciliation scheduler.
     */
    List<TradeIdea> findByStatus(TradeStatus status);

    /**
     * Count trades by user and status.
     */
    long countByUserIdAndStatus(UUID userId, TradeStatus status);
}
