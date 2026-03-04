package com.investtrack.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO representing a live price update from the Market Data Service.
 * Constructed from Kite WebSocket tick data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateDTO {

    private String stockSymbol;
    private BigDecimal lastTradedPrice;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private long volume;
    private long timestamp;
}
