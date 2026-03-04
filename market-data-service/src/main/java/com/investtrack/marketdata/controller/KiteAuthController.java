package com.investtrack.marketdata.controller;

import com.investtrack.marketdata.kite.KiteAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for Kite Connect OAuth flow.
 * <p>
 * - GET /kite/login → returns the Kite login URL
 * - GET /kite/callback → handles the OAuth callback with request_token
 */
@RestController
@RequestMapping("/kite")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Kite Auth", description = "Kite Connect OAuth endpoints")
public class KiteAuthController {

    private final KiteAuthService kiteAuthService;

    @GetMapping("/login")
    @Operation(summary = "Get Kite login URL", description = "Returns the Kite Connect login URL for OAuth flow")
    public ResponseEntity<Map<String, String>> getLoginUrl() {
        String loginUrl = kiteAuthService.getLoginUrl();
        return ResponseEntity.ok(Map.of("loginUrl", loginUrl));
    }

    @GetMapping("/callback")
    @Operation(summary = "Kite OAuth callback", description = "Handles the Kite Connect OAuth callback with request_token")
    public ResponseEntity<Map<String, String>> handleCallback(
            @RequestParam("request_token") String requestToken,
            @RequestParam(value = "status", required = false) String status) {

        log.info("Kite callback received: status={}", status);

        if ("success".equals(status)) {
            String accessToken = kiteAuthService.exchangeToken(requestToken);
            return ResponseEntity.ok(Map.of(
                    "message", "Kite authentication successful",
                    "status", "connected"
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "message", "Kite authentication failed",
                "status", "error"
        ));
    }
}
