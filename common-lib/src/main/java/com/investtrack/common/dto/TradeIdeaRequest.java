package com.investtrack.common.dto;

import com.investtrack.common.enums.Timeframe;
import com.investtrack.common.enums.TradeReason;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new trade idea.
 * All fields are validated — SL and Target are mandatory,
 * and business rules (R:R >= 1) are enforced at the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeIdeaRequest {

    @NotBlank(message = "Stock symbol is required")
    @Size(max = 20, message = "Stock symbol must not exceed 20 characters")
    private String stockSymbol;

    @NotNull(message = "Entry price is required")
    @DecimalMin(value = "0.01", message = "Entry price must be greater than 0")
    private BigDecimal entryPrice;

    @NotNull(message = "Stop loss is required")
    @DecimalMin(value = "0.01", message = "Stop loss must be greater than 0")
    private BigDecimal stopLoss;

    @NotNull(message = "Target price is required")
    @DecimalMin(value = "0.01", message = "Target price must be greater than 0")
    private BigDecimal targetPrice;

    @NotNull(message = "Timeframe is required")
    private Timeframe timeframe;

    @NotNull(message = "Trade reason is required")
    private TradeReason reason;

    @DecimalMin(value = "0.1", message = "Risk percentage must be at least 0.1%")
    @DecimalMax(value = "100.0", message = "Risk percentage must not exceed 100%")
    private BigDecimal riskPercentage;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
