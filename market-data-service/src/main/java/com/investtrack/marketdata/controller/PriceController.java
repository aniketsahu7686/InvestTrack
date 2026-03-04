package com.investtrack.marketdata.controller;

import com.investtrack.common.dto.PriceUpdateDTO;
import com.investtrack.marketdata.entity.InstrumentToken;
import com.investtrack.marketdata.kite.KiteWebSocketClient;
import com.investtrack.marketdata.repository.InstrumentTokenRepository;
import com.investtrack.marketdata.service.PriceCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for price data endpoints.
 * <p>
 * - GET /prices/{symbol} → get the latest cached price for a symbol
 * - POST /prices/subscribe → subscribe to live price updates for symbols
 */
@RestController
@RequestMapping("/prices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Prices", description = "Live price data endpoints")
public class PriceController {

    private final PriceCacheService priceCacheService;
    private final InstrumentTokenRepository instrumentTokenRepository;
    private final KiteWebSocketClient kiteWebSocketClient;

    @GetMapping("/{symbol}")
    @Operation(summary = "Get latest price", description = "Returns the latest cached price for a stock symbol")
    public ResponseEntity<PriceUpdateDTO> getPrice(@PathVariable String symbol) {
        PriceUpdateDTO price = priceCacheService.getCachedPrice(symbol.toUpperCase());

        if (price == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(price);
    }

    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to price updates", description = "Subscribe to live price updates for given stock symbols")
    public ResponseEntity<Map<String, Object>> subscribe(@RequestBody List<String> symbols) {
        log.info("Subscribing to price updates for: {}", symbols);

        List<String> upperSymbols = symbols.stream()
                .map(String::toUpperCase)
                .toList();

        // Find instrument tokens for the requested symbols
        List<InstrumentToken> instruments = upperSymbols.stream()
                .map(instrumentTokenRepository::findByStockSymbol)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();

        if (instruments.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "No instrument tokens found for given symbols",
                    "requestedSymbols", upperSymbols
            ));
        }

        Set<Long> tokens = instruments.stream()
                .map(InstrumentToken::getInstrumentToken)
                .collect(Collectors.toSet());

        Map<Long, String> tokenSymbolMap = instruments.stream()
                .collect(Collectors.toMap(
                        InstrumentToken::getInstrumentToken,
                        InstrumentToken::getStockSymbol
                ));

        kiteWebSocketClient.subscribe(tokens, tokenSymbolMap);

        List<String> subscribedSymbols = instruments.stream()
                .map(InstrumentToken::getStockSymbol)
                .toList();

        return ResponseEntity.ok(Map.of(
                "message", "Subscribed to price updates",
                "subscribedSymbols", subscribedSymbols,
                "tokenCount", tokens.size()
        ));
    }

    @GetMapping("/status")
    @Operation(summary = "WebSocket status", description = "Check the status of the Kite WebSocket connection")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "connected", kiteWebSocketClient.isConnected(),
                "service", "market-data-service"
        ));
    }
}
