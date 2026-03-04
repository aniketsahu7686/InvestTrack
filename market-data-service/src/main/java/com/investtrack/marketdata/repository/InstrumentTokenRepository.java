package com.investtrack.marketdata.repository;

import com.investtrack.marketdata.entity.InstrumentToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for InstrumentToken entities.
 * Provides methods to look up instrument tokens by stock symbol or token number.
 */
@Repository
public interface InstrumentTokenRepository extends JpaRepository<InstrumentToken, UUID> {

    /**
     * Find an instrument token by stock symbol.
     */
    Optional<InstrumentToken> findByStockSymbol(String stockSymbol);

    /**
     * Find an instrument token by its numeric Kite token.
     */
    Optional<InstrumentToken> findByInstrumentToken(Long instrumentToken);

    /**
     * Find all active instruments (subscribed for price updates).
     */
    List<InstrumentToken> findByActiveTrue();

    /**
     * Check if a stock symbol exists in the mapping.
     */
    boolean existsByStockSymbol(String stockSymbol);
}
