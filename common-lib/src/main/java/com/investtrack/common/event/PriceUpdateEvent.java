package com.investtrack.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Event published when a live price update is received from Kite WebSocket.
 * Produced by Market Data Service. Consumed by Trade Service to auto-close trades.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String stockSymbol;
    private BigDecimal lastTradedPrice;
    private long timestamp;
}
