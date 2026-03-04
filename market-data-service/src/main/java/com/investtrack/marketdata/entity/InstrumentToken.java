package com.investtrack.marketdata.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Maps stock symbols to Kite instrument tokens.
 * <p>
 * Kite Connect uses numeric instrument tokens for WebSocket subscriptions.
 * This entity stores the mapping so we can translate between human-readable
 * symbols (e.g., "RELIANCE") and Kite's numeric identifiers.
 */
@Entity
@Table(name = "instrument_tokens", indexes = {
        @Index(name = "idx_instrument_symbol", columnList = "stock_symbol", unique = true),
        @Index(name = "idx_instrument_token", columnList = "instrument_token", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The stock symbol (e.g., "RELIANCE", "TCS", "INFY").
     */
    @Column(name = "stock_symbol", nullable = false, unique = true, length = 20)
    private String stockSymbol;

    /**
     * The Kite instrument token (numeric identifier).
     */
    @Column(name = "instrument_token", nullable = false, unique = true)
    private Long instrumentToken;

    /**
     * The exchange (e.g., "NSE", "BSE").
     */
    @Column(name = "exchange", length = 10)
    private String exchange;

    /**
     * The full trading symbol used by Kite (e.g., "NSE:RELIANCE").
     */
    @Column(name = "trading_symbol", length = 50)
    private String tradingSymbol;

    /**
     * Whether this instrument is actively subscribed for price updates.
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
