package com.investtrack.marketdata.kite;

import com.investtrack.common.dto.PriceUpdateDTO;
import com.investtrack.marketdata.service.PriceCacheService;
import com.investtrack.marketdata.service.PricePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket client that connects to the Kite Connect streaming API.
 * <p>
 * Receives real-time price ticks and:
 * 1. Updates the Redis cache via PriceCacheService
 * 2. Publishes PriceUpdateEvent via PricePublisher (to RabbitMQ)
 * <p>
 * NOTE: This is a simplified implementation. In production, you would use
 * the actual Kite Connect Java SDK's KiteTicker for binary WebSocket messages.
 * This implementation handles JSON-formatted text messages for demonstration.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KiteWebSocketClient extends TextWebSocketHandler {

    private final KiteConfig kiteConfig;
    private final KiteTokenManager kiteTokenManager;
    private final PriceCacheService priceCacheService;
    private final PricePublisher pricePublisher;

    private WebSocketSession session;
    private final Set<Long> subscribedTokens = ConcurrentHashMap.newKeySet();
    private final Map<Long, String> tokenToSymbolMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();

    private static final String KITE_WS_URL = "wss://ws.kite.trade";
    private static final int RECONNECT_DELAY_SECONDS = 5;

    /**
     * Initiates the WebSocket connection to Kite streaming API.
     */
    public void connect() {
        if (!kiteTokenManager.hasValidToken()) {
            log.warn("Cannot connect to Kite WebSocket: no valid access token");
            return;
        }

        String wsUrl = KITE_WS_URL + "?api_key=" + kiteConfig.getApiKey()
                + "&access_token=" + kiteTokenManager.getAccessToken();

        try {
            StandardWebSocketClient client = new StandardWebSocketClient();
            this.session = client.execute(this, wsUrl).get(10, TimeUnit.SECONDS);
            log.info("Connected to Kite WebSocket successfully");
        } catch (Exception ex) {
            log.error("Failed to connect to Kite WebSocket: {}", ex.getMessage(), ex);
            scheduleReconnect();
        }
    }

    /**
     * Subscribes to price updates for the given instrument tokens.
     *
     * @param instrumentTokens   the instrument tokens to subscribe to
     * @param tokenSymbolMapping mapping of instrument token → stock symbol
     */
    public void subscribe(Set<Long> instrumentTokens, Map<Long, String> tokenSymbolMapping) {
        subscribedTokens.addAll(instrumentTokens);
        tokenToSymbolMap.putAll(tokenSymbolMapping);

        if (session != null && session.isOpen()) {
            try {
                // Send subscription message (simplified JSON format)
                String subscribeMsg = "{\"a\":\"subscribe\",\"v\":" + instrumentTokens + "}";
                session.sendMessage(new TextMessage(subscribeMsg));
                log.info("Subscribed to {} instrument tokens", instrumentTokens.size());
            } catch (Exception ex) {
                log.error("Failed to send subscribe message: {}", ex.getMessage(), ex);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // Parse tick data and process
            // NOTE: Real Kite ticks are binary; this handles demo JSON format
            String payload = message.getPayload();
            log.debug("Received tick: {}", payload);
            processTickData(payload);
        } catch (Exception ex) {
            log.error("Error processing tick message: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Kite WebSocket connection established");
        this.session = session;

        // Re-subscribe to previously subscribed tokens
        if (!subscribedTokens.isEmpty()) {
            subscribe(subscribedTokens, tokenToSymbolMap);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.warn("Kite WebSocket connection closed: {}", status);
        this.session = null;
        scheduleReconnect();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Kite WebSocket transport error: {}", exception.getMessage(), exception);
    }

    /**
     * Processes a single tick data payload.
     * Extracts price information and updates cache + publishes event.
     */
    private void processTickData(String tickPayload) {
        // Simplified: in production, parse binary Kite tick format
        // For demonstration, we assume a JSON-like format
        // Actual implementation would use KiteTicker SDK

        // This is a placeholder — real implementation would parse binary data
        // and extract instrumentToken, LTP, OHLCV from the tick
        log.debug("Processing tick data: {}", tickPayload);
    }

    /**
     * Called when a parsed tick is received (from actual Kite SDK integration).
     * Updates Redis cache and publishes to RabbitMQ.
     */
    public void onTickReceived(Long instrumentToken, BigDecimal ltp, BigDecimal open,
                                BigDecimal high, BigDecimal low, BigDecimal close, Long volume) {
        String symbol = tokenToSymbolMap.get(instrumentToken);
        if (symbol == null) {
            log.warn("Received tick for unknown instrument token: {}", instrumentToken);
            return;
        }

        PriceUpdateDTO priceUpdate = PriceUpdateDTO.builder()
                .stockSymbol(symbol)
                .ltp(ltp)
                .open(open)
                .high(high)
                .low(low)
                .close(close)
                .volume(volume)
                .timestamp(LocalDateTime.now())
                .build();

        // Update Redis cache
        priceCacheService.cachePrice(priceUpdate);

        // Publish to RabbitMQ for trade-service consumption
        pricePublisher.publishPriceUpdate(priceUpdate);

        log.debug("Tick processed: symbol={}, ltp={}", symbol, ltp);
    }

    /**
     * Schedules a reconnection attempt after a delay.
     */
    private void scheduleReconnect() {
        if (kiteTokenManager.hasValidToken()) {
            log.info("Scheduling reconnect in {} seconds...", RECONNECT_DELAY_SECONDS);
            reconnectExecutor.schedule(this::connect, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * Checks if the WebSocket connection is active.
     */
    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
            reconnectExecutor.shutdownNow();
            log.info("Kite WebSocket client shut down");
        } catch (Exception ex) {
            log.error("Error during WebSocket disconnect: {}", ex.getMessage(), ex);
        }
    }
}
