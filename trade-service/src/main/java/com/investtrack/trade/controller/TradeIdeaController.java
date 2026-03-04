package com.investtrack.trade.controller;

import com.investtrack.common.dto.TradeIdeaRequest;
import com.investtrack.common.dto.TradeIdeaResponse;
import com.investtrack.common.enums.Timeframe;
import com.investtrack.common.enums.TradeReason;
import com.investtrack.common.enums.TradeStatus;
import com.investtrack.trade.service.TradeIdeaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * REST controller for Trade Idea operations.
 * <p>
 * - POST /trades — create a new trade idea (authenticated)
 * - GET /trades — list trade ideas with filters + pagination (public)
 * - GET /trades/{id} — get a specific trade idea by ID (public)
 * - GET /trades/user/{userId} — get all trade ideas by a user (public)
 */
@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trade Ideas", description = "Trade idea management endpoints")
public class TradeIdeaController {

    private final TradeIdeaService tradeIdeaService;

    @PostMapping
    @Operation(summary = "Create a new trade idea", description = "Submit a new trade idea. Requires authentication. R:R must be >= 1.")
    public ResponseEntity<TradeIdeaResponse> createTrade(
            @Valid @RequestBody TradeIdeaRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        UUID userId = UUID.fromString(authentication.getCredentials().toString());

        log.info("Creating trade idea for user: {} symbol: {}", username, request.getStockSymbol());
        TradeIdeaResponse response = tradeIdeaService.createTrade(request, userId, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List trade ideas", description = "Retrieve trade ideas with optional filters and pagination. Publicly accessible.")
    public ResponseEntity<Page<TradeIdeaResponse>> getTrades(
            @Parameter(description = "Filter by timeframe") @RequestParam(required = false) Timeframe timeframe,
            @Parameter(description = "Filter by trade status") @RequestParam(required = false) TradeStatus status,
            @Parameter(description = "Filter by trade reason") @RequestParam(required = false) TradeReason reason,
            @Parameter(description = "Filter by stock symbol") @RequestParam(required = false) String stockSymbol,
            @Parameter(description = "Filter by minimum risk-reward ratio") @RequestParam(required = false) BigDecimal minRiskReward,
            @Parameter(description = "Filter by user ID") @RequestParam(required = false) UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<TradeIdeaResponse> trades = tradeIdeaService.getTrades(
                timeframe, status, reason, stockSymbol, minRiskReward, userId, pageable);
        return ResponseEntity.ok(trades);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trade idea by ID", description = "Retrieve a specific trade idea by its ID. Publicly accessible.")
    public ResponseEntity<TradeIdeaResponse> getTradeById(
            @Parameter(description = "Trade idea ID") @PathVariable UUID id) {

        TradeIdeaResponse response = tradeIdeaService.getTradeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get trade ideas by user", description = "Retrieve all trade ideas submitted by a specific user. Publicly accessible.")
    public ResponseEntity<Page<TradeIdeaResponse>> getTradesByUser(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<TradeIdeaResponse> trades = tradeIdeaService.getTradesByUserId(userId, pageable);
        return ResponseEntity.ok(trades);
    }
}
