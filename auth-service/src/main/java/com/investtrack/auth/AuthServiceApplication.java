package com.investtrack.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Entry point for the Auth Service microservice.
 * Handles user registration, login, JWT issuance, and role management.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.investtrack.auth", "com.investtrack.common"})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
