package com.investtrack.auth.service;

import com.investtrack.auth.entity.User;
import com.investtrack.auth.mapper.UserMapper;
import com.investtrack.auth.repository.UserRepository;
import com.investtrack.common.dto.AuthResponse;
import com.investtrack.common.dto.LoginRequest;
import com.investtrack.common.dto.RegisterRequest;
import com.investtrack.common.exception.BusinessValidationException;
import com.investtrack.common.exception.ResourceNotFoundException;
import com.investtrack.common.exception.UnauthorizedException;
import com.investtrack.common.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for authentication operations.
 * Handles user registration, login, and JWT token issuance.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    /**
     * Registers a new user with encrypted password.
     * Validates uniqueness of username and email before persisting.
     *
     * @param request the registration request DTO
     * @return authentication response with JWT token
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());

        // Validate uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessValidationException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessValidationException("Email '" + request.getEmail() + "' is already registered");
        }

        // Build and persist user entity
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        // Generate JWT token for immediate login
        String token = jwtUtils.generateToken(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .expiresIn(jwtUtils.getExpirationMs())
                .build();
    }

    /**
     * Authenticates a user and issues a JWT token.
     *
     * @param request the login request DTO
     * @return authentication response with JWT token
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getUsername()));

            String token = jwtUtils.generateToken(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().name()
            );

            log.info("User logged in successfully: {}", user.getUsername());

            return AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .expiresIn(jwtUtils.getExpirationMs())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {}", request.getUsername());
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    /**
     * Returns the currently authenticated user's profile.
     *
     * @param username the authenticated username from JWT
     * @return authentication response (without a new token)
     */
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
