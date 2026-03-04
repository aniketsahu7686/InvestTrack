package com.investtrack.auth.controller;

import com.investtrack.auth.service.AuthService;
import com.investtrack.common.dto.AuthResponse;
import com.investtrack.common.dto.LoginRequest;
import com.investtrack.common.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 * Handles user registration, login, and profile retrieval.
 *
 * <ul>
 *   <li>POST /auth/register — Register a new user</li>
 *   <li>POST /auth/login    — Authenticate and get JWT</li>
 *   <li>GET  /auth/me       — Get current user profile (requires JWT)</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * Registers a new user and returns a JWT token for immediate login.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and returns a JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the profile of the currently authenticated user.
     * Requires a valid JWT in the Authorization header.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        log.debug("Profile request for authenticated user: {}", authentication.getName());
        AuthResponse response = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
