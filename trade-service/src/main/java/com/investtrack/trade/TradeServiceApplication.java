package com.investtrack.trade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Trade Service microservice.
 * Handles trade idea creation, status tracking, and event publishing.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.investtrack.trade", "com.investtrack.common"})
@EnableScheduling
public class TradeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeServiceApplication.class, args);
    }
}
