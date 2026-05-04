package com.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main entry point for URLShortener Pro application
 * 
 * Features:
 * - Spring Boot auto-configuration
 * - Redis caching
 * - Async processing for analytics
 * - Transaction management with JPA
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class UrlShortenerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }
}
