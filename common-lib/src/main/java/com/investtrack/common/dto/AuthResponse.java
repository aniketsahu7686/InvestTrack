package com.investtrack.common.dto;

import com.investtrack.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authentication. Contains the JWT token and user metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String username;
    private String email;
    private UserRole role;
    private long expiresIn;
}
