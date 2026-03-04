package com.investtrack.marketdata.kite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Handles the Kite Connect OAuth flow.
 * <p>
 * Flow:
 * 1. User visits the Kite login URL → redirected to Zerodha login
 * 2. After login, Zerodha redirects back with a 'request_token'
 * 3. This service exchanges the request_token for an access_token
 * 4. The access_token is stored via KiteTokenManager for API calls
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KiteAuthService {

    private static final String KITE_API_BASE = "https://api.kite.trade";
    private static final String TOKEN_ENDPOINT = KITE_API_BASE + "/session/token";

    private final KiteConfig kiteConfig;
    private final KiteTokenManager kiteTokenManager;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Returns the full Kite login URL for the user to initiate OAuth.
     */
    public String getLoginUrl() {
        String url = kiteConfig.getFullLoginUrl();
        log.info("Generated Kite login URL: {}", url);
        return url;
    }

    /**
     * Exchanges a request_token for an access_token via Kite API.
     * The checksum is SHA-256 of (api_key + request_token + api_secret).
     *
     * @param requestToken the request token received from Kite callback
     * @return the access token
     */
    @SuppressWarnings("unchecked")
    public String exchangeToken(String requestToken) {
        log.info("Exchanging request token for access token...");

        String checksum = generateChecksum(requestToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("X-Kite-Version", "3");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("api_key", kiteConfig.getApiKey());
        body.add("request_token", requestToken);
        body.add("checksum", checksum);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    TOKEN_ENDPOINT, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    String accessToken = (String) data.get("access_token");
                    kiteTokenManager.storeAccessToken(accessToken);
                    log.info("Kite access token obtained and stored successfully");
                    return accessToken;
                }
            }

            throw new RuntimeException("Failed to exchange Kite token: unexpected response");
        } catch (Exception ex) {
            log.error("Kite token exchange failed: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to exchange Kite token", ex);
        }
    }

    /**
     * Generates SHA-256 checksum: SHA256(api_key + request_token + api_secret).
     */
    private String generateChecksum(String requestToken) {
        try {
            String raw = kiteConfig.getApiKey() + requestToken + kiteConfig.getApiSecret();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
