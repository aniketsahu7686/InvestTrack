package com.investtrack.common.exception;

/**
 * Thrown when a business rule validation fails.
 * Examples: Risk/Reward ratio < 1, duplicate username, etc.
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
