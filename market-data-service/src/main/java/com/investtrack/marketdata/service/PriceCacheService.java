package com.investtrack.marketdata.service;

import com.investtrack.common.dto.PriceUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for caching live price data in Redis.
 * <p>
 * Each stock symbol's latest price is stored with a configurable TTL.
 * Key format: "price:{SYMBOL}" (e.g., "price:RELIANCE")
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceCacheService {

    private static final String PRICE_KEY_PREFIX = "price:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${price-cache.ttl-seconds:60}")
    private long cacheTtlSeconds;

    /**
     * Caches the latest price data for a stock symbol.
     *
     * @param priceUpdate the price data to cache
     */
    public void cachePrice(PriceUpdateDTO priceUpdate) {
        String key = PRICE_KEY_PREFIX + priceUpdate.getStockSymbol().toUpperCase();

        redisTemplate.opsForValue().set(key, priceUpdate, cacheTtlSeconds, TimeUnit.SECONDS);
        log.debug("Cached price for {}: LTP={}", priceUpdate.getStockSymbol(), priceUpdate.getLastTradedPrice());
    }

    /**
     * Retrieves the cached price data for a stock symbol.
     *
     * @param stockSymbol the stock symbol to look up
     * @return the cached price data, or null if not found/expired
     */
    public PriceUpdateDTO getCachedPrice(String stockSymbol) {
        String key = PRICE_KEY_PREFIX + stockSymbol.toUpperCase();
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached instanceof PriceUpdateDTO priceUpdate) {
            log.debug("Cache hit for {}: LTP={}", stockSymbol, priceUpdate.getLastTradedPrice());
            return priceUpdate;
        }

        log.debug("Cache miss for {}", stockSymbol);
        return null;
    }

    /**
     * Removes the cached price for a symbol.
     *
     * @param stockSymbol the stock symbol to evict from cache
     */
    public void evictPrice(String stockSymbol) {
        String key = PRICE_KEY_PREFIX + stockSymbol.toUpperCase();
        redisTemplate.delete(key);
        log.debug("Evicted cached price for {}", stockSymbol);
    }

    /**
     * Checks if a price is currently cached for the given symbol.
     */
    public boolean hasCachedPrice(String stockSymbol) {
        String key = PRICE_KEY_PREFIX + stockSymbol.toUpperCase();
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
