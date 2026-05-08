package com.tarique.bankstatement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Bank Statement Service — Production-ready API
 * Designed for 10K req/sec, 50M Indian customers
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class BankStatementApplication {

    public static void main(String[] args) {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(BankStatementApplication.class, args);
        //Test
    }
}
