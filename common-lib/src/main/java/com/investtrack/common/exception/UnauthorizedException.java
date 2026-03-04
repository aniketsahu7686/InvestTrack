package com.investtrack.common.exception;

/**
 * Thrown when authentication or authorization fails.
 * Examples: invalid JWT, expired token, insufficient role.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
