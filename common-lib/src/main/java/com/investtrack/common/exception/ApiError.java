package com.investtrack.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized API error response returned by all microservices.
 * Provides consistent error structure for frontend consumption.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    private int status;
    private String message;
    private List<String> errors;
    private String path;
    private LocalDateTime timestamp;
}
