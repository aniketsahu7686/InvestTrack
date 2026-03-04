package com.investtrack.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Redis-based rate limiting filter for the API Gateway.
 * <p>
 * Uses a sliding window counter stored in Redis to limit
 * the number of requests per user/IP within a time window.
 * <p>
 * Applied to sensitive endpoints like POST /trades.
 */
@Component
@Slf4j
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Value("${rate-limiting.trade-post-replenish-rate:5}")
    private int replenishRate;

    @Value("${rate-limiting.trade-post-burst-capacity:10}")
    private int burstCapacity;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    public RateLimitingFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientId = resolveClientId(exchange);
            String key = RATE_LIMIT_KEY_PREFIX + clientId;

            return redisTemplate.opsForValue().increment(key)
                    .flatMap(count -> {
                        if (count == 1) {
                            // First request in window — set expiry
                            return redisTemplate.expire(key, WINDOW_DURATION)
                                    .then(chain.filter(exchange));
                        }

                        if (count > burstCapacity) {
                            log.warn("Rate limit exceeded for client: {} (count: {})", clientId, count);
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(burstCapacity));
                            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
                            return exchange.getResponse().setComplete();
                        }

                        long remaining = burstCapacity - count;
                        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(burstCapacity));
                        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));
                        return chain.filter(exchange);
                    });
        };
    }

    /**
     * Resolves the client identifier for rate limiting.
     * Uses X-User-Id header if available (authenticated), otherwise falls back to IP.
     */
    private String resolveClientId(org.springframework.web.server.ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }

        // Fall back to client IP
        if (exchange.getRequest().getRemoteAddress() != null) {
            return "ip:" + exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    public static class Config {
        // Configuration properties can be added here
    }
}
