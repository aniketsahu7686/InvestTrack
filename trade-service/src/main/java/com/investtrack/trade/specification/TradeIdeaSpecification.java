package com.investtrack.trade.specification;

import com.investtrack.common.enums.Timeframe;
import com.investtrack.common.enums.TradeStatus;
import com.investtrack.trade.entity.TradeIdea;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA Specification builder for dynamic trade filtering.
 * Supports filtering by timeframe, status, risk/reward ratio, stock symbol, and userId.
 * Used with pageable endpoints for flexible querying.
 *
 * <p>Usage example:</p>
 * <pre>
 *   Specification&lt;TradeIdea&gt; spec = Specification.where(
 *       TradeIdeaSpecification.hasTimeframe(Timeframe.INTRADAY))
 *       .and(TradeIdeaSpecification.hasMinRiskReward(BigDecimal.valueOf(2)))
 *       .and(TradeIdeaSpecification.hasStatus(TradeStatus.OPEN));
 * </pre>
 */
public final class TradeIdeaSpecification {

    private TradeIdeaSpecification() {
        // Utility class — prevent instantiation
    }

    /**
     * Filter by trade timeframe (INTRADAY, SWING, POSITIONAL).
     */
    public static Specification<TradeIdea> hasTimeframe(Timeframe timeframe) {
        return (root, query, criteriaBuilder) -> {
            if (timeframe == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("timeframe"), timeframe);
        };
    }

    /**
     * Filter by trade status (OPEN, TARGET_HIT, SL_HIT, EXPIRED).
     */
    public static Specification<TradeIdea> hasStatus(TradeStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Filter trades with risk/reward ratio >= the given minimum value.
     */
    public static Specification<TradeIdea> hasMinRiskReward(BigDecimal minRiskReward) {
        return (root, query, criteriaBuilder) -> {
            if (minRiskReward == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("riskRewardRatio"), minRiskReward);
        };
    }

    /**
     * Filter by stock symbol (case-insensitive).
     */
    public static Specification<TradeIdea> hasStockSymbol(String stockSymbol) {
        return (root, query, criteriaBuilder) -> {
            if (stockSymbol == null || stockSymbol.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("stockSymbol")),
                    stockSymbol.toUpperCase()
            );
        };
    }

    /**
     * Filter by user ID.
     */
    public static Specification<TradeIdea> hasUserId(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("userId"), userId);
        };
    }

    /**
     * Filter by trade reason (TECHNICAL, FUNDAMENTAL).
     */
    public static Specification<TradeIdea> hasReason(String reason) {
        return (root, query, criteriaBuilder) -> {
            if (reason == null || reason.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("reason"), com.investtrack.common.enums.TradeReason.valueOf(reason.toUpperCase()));
        };
    }
}
