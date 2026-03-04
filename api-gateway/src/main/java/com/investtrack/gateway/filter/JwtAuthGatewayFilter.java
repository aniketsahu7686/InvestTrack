package com.investtrack.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.List;

/**
 * Global gateway filter that validates JWT tokens for secured endpoints.
 * <p>
 * - Public endpoints (GET /trades, /analytics, /auth, /actuator, /kite) pass through
 * - POST /trades requires a valid JWT
 * - Valid tokens result in X-User-Id and X-Username headers forwarded downstream
 */
@Component
@Slf4j
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/", "/actuator/", "/v3/api-docs", "/swagger-ui", "/kite/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        HttpMethod method = request.getMethod();

        // Allow all public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Allow GET requests to public read endpoints
        if (method == HttpMethod.GET && isPublicReadPath(path)) {
            return chain.filter(exchange);
        }

        // OPTIONS requests always pass through (CORS preflight)
        if (method == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // All other requests require JWT
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for {} {}", method, path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = validateToken(token);
            String userId = claims.get("userId", String.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            // Forward user info as headers to downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Username", username)
                    .header("X-User-Role", role)
                    .build();

            log.debug("JWT validated for user: {} ({}), role: {}", username, userId, role);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception ex) {
            log.warn("JWT validation failed for {} {}: {}", method, path, ex.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100; // Run before other filters
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isPublicReadPath(String path) {
        return path.startsWith("/trades") || path.startsWith("/analytics") || path.startsWith("/prices");
    }

    private Claims validateToken(String token) {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
