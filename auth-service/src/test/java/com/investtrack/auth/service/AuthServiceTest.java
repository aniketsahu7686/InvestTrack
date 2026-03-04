package com.investtrack.auth.service;

import com.investtrack.auth.entity.User;
import com.investtrack.auth.mapper.UserMapper;
import com.investtrack.auth.repository.UserRepository;
import com.investtrack.common.dto.AuthResponse;
import com.investtrack.common.dto.LoginRequest;
import com.investtrack.common.dto.RegisterRequest;
import com.investtrack.common.enums.UserRole;
import com.investtrack.common.exception.BusinessValidationException;
import com.investtrack.common.exception.UnauthorizedException;
import com.investtrack.common.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService — registration, login, and profile retrieval.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .fullName("Test User")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("$2a$10$encodedPassword")
                .fullName("Test User")
                .role(UserRole.USER)
                .enabled(true)
                .build();
    }

    // ─── Registration Tests ─────────────────────────────────

    @Test
    @DisplayName("Should register a new user successfully")
    void register_Success() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(testUser);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtils.generateToken(any(UUID.class), anyString(), anyString())).thenReturn("jwt-token");
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void register_DuplicateUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("already taken");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void register_DuplicateEmail() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    // ─── Login Tests ────────────────────────────────────────

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtils.generateToken(any(UUID.class), anyString(), anyString())).thenReturn("jwt-token");
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should throw exception for invalid credentials")
    void login_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid username or password");
    }

    // ─── Get Current User Tests ─────────────────────────────

    @Test
    @DisplayName("Should return current user profile")
    void getCurrentUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        AuthResponse response = authService.getCurrentUser("testuser");

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.USER);
    }
}
