package com.investtrack.common.dto;

import com.investtrack.common.enums.Timeframe;
import com.investtrack.common.enums.TradeReason;
import com.investtrack.common.enums.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for returning trade idea details to clients.
 * Includes the auto-calculated riskRewardRatio and trader information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeIdeaResponse {

    private UUID id;
    private UUID userId;
    private String traderUsername;
    private String stockSymbol;
    private BigDecimal entryPrice;
    private BigDecimal stopLoss;
    private BigDecimal targetPrice;
    private BigDecimal riskRewardRatio;
    private BigDecimal riskPercentage;
    private Timeframe timeframe;
    private TradeReason reason;
    private TradeStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
}
