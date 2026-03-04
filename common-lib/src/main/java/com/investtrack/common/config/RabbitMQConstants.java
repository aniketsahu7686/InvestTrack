package com.investtrack.common.config;

/**
 * Centralized RabbitMQ constants shared across all microservices.
 * Ensures consistent exchange, queue, and routing key naming.
 */
public final class RabbitMQConstants {

    private RabbitMQConstants() {
        // Utility class — prevent instantiation
    }

    // ─── Exchanges ──────────────────────────────────────────────
    public static final String TRADE_EXCHANGE = "investtrack.trade.exchange";
    public static final String PRICE_EXCHANGE = "investtrack.price.exchange";

    // ─── Queues ─────────────────────────────────────────────────
    public static final String TRADE_STATUS_QUEUE = "investtrack.trade.status.queue";
    public static final String PRICE_UPDATE_QUEUE = "investtrack.price.update.queue";
    public static final String ANALYTICS_TRADE_QUEUE = "investtrack.analytics.trade.queue";

    // ─── Routing Keys ───────────────────────────────────────────
    public static final String TRADE_STATUS_ROUTING_KEY = "trade.status.update";
    public static final String PRICE_UPDATE_ROUTING_KEY = "price.update";
    public static final String ANALYTICS_TRADE_ROUTING_KEY = "analytics.trade.update";
}
