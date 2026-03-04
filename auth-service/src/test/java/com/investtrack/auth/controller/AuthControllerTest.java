package com.investtrack.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investtrack.auth.security.JwtAuthenticationFilter;
import com.investtrack.auth.security.SecurityConfig;
import com.investtrack.auth.security.UserDetailsServiceImpl;
import com.investtrack.auth.service.AuthService;
import com.investtrack.common.dto.AuthResponse;
import com.investtrack.common.dto.LoginRequest;
import com.investtrack.common.dto.RegisterRequest;
import com.investtrack.common.enums.UserRole;
import com.investtrack.common.security.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests for AuthController — verifies HTTP layer behavior.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ─── Registration Tests ─────────────────────────────────

    @Test
    @DisplayName("POST /auth/register — should return 201 with token")
    void register_ReturnsCreated() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .fullName("New User")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .username("newuser")
                .email("new@example.com")
                .role(UserRole.USER)
                .expiresIn(86400000L)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /auth/register — should return 400 for invalid input")
    void register_InvalidInput_ReturnsBadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("")  // blank — violates @NotBlank
                .email("invalid-email")
                .password("short")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ─── Login Tests ────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login — should return 200 with token")
    void login_ReturnsOk() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .expiresIn(86400000L)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /auth/login — should return 400 for blank fields")
    void login_BlankFields_ReturnsBadRequest() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("")
                .password("")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
