package com.investtrack.common.event;

import com.investtrack.common.enums.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a trade's status changes (e.g., TARGET_HIT, SL_HIT, EXPIRED).
 * Produced by Trade Service. Consumed by Analytics Service for ranking recalculation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeStatusEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID tradeId;
    private UUID userId;
    private String stockSymbol;
    private TradeStatus previousStatus;
    private TradeStatus newStatus;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal stopLoss;
    private BigDecimal targetPrice;
    private BigDecimal riskRewardRatio;
    private LocalDateTime closedAt;
}
