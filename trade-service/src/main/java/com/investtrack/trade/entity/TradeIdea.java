package com.investtrack.trade.entity;

import com.investtrack.common.enums.Timeframe;
import com.investtrack.common.enums.TradeReason;
import com.investtrack.common.enums.TradeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a structured trade idea.
 * Immutable after creation — no updates allowed to core fields.
 * Only status and closedAt can be changed by the system (auto-close).
 */
@Entity
@Table(name = "trade_ideas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeIdea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "trader_username", nullable = false, length = 30)
    private String traderUsername;

    @Column(name = "stock_symbol", nullable = false, length = 20)
    private String stockSymbol;

    @Column(name = "entry_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal entryPrice;

    @Column(name = "stop_loss", nullable = false, precision = 12, scale = 2)
    private BigDecimal stopLoss;

    @Column(name = "target_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetPrice;

    @Column(name = "risk_reward_ratio", nullable = false, precision = 6, scale = 2)
    private BigDecimal riskRewardRatio;

    @Column(name = "risk_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal riskPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "timeframe", nullable = false, length = 15)
    private Timeframe timeframe;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 15)
    private TradeReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    @Builder.Default
    private TradeStatus status = TradeStatus.OPEN;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
