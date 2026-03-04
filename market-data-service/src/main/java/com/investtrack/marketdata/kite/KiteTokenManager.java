package com.investtrack.marketdata.kite;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages the Kite Connect access token.
 * <p>
 * Kite access tokens are valid for one trading day. This manager stores
 * the current token in memory and tracks when it was issued, so it can
 * be refreshed daily via the OAuth flow.
 */
@Component
@Slf4j
public class KiteTokenManager {

    private final AtomicReference<String> accessToken = new AtomicReference<>();
    private volatile LocalDate tokenDate;

    /**
     * Stores a new access token, updating the issue date.
     *
     * @param token the access token from Kite
     */
    public void storeAccessToken(String token) {
        this.accessToken.set(token);
        this.tokenDate = LocalDate.now();
        log.info("Kite access token stored. Valid for: {}", tokenDate);
    }

    /**
     * Returns the current access token.
     *
     * @return the access token, or null if not set
     */
    public String getAccessToken() {
        return accessToken.get();
    }

    /**
     * Checks if a valid (non-expired) access token is available.
     * Kite tokens expire at midnight, so we check against today's date.
     *
     * @return true if a valid token is available
     */
    public boolean hasValidToken() {
        String token = accessToken.get();
        if (token == null || tokenDate == null) {
            return false;
        }
        // Token is valid if it was issued today
        return tokenDate.equals(LocalDate.now());
    }

    /**
     * Clears the stored token (e.g., on logout or error).
     */
    public void clearToken() {
        accessToken.set(null);
        tokenDate = null;
        log.info("Kite access token cleared");
    }

    /**
     * Returns the date when the token was issued.
     */
    public LocalDate getTokenDate() {
        return tokenDate;
    }
}
