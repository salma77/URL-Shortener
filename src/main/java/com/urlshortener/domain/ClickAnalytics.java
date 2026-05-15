package com.urlshortener.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks analytics for each click on a shortened URL.
 * 
 * Captures:
 * - User IP address
 * - User agent / browser info
 * - Referrer
 * - Timestamp of click
 * - Geographic location (optional)
 */
@Entity
@Table(name = "analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long urlId;
    
    @Column(nullable = false, length = 45)
    private String ipAddress;
    
    @Column(length = 500)
    private String userAgent;
    
    @Column(length = 500)
    private String referrer;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime clickedAt;
    
    @Column(length = 100)
    private String country;
    
    @Column(length = 100)
    private String city;
    
    @PrePersist
    protected void onCreate() {
        clickedAt = LocalDateTime.now();
    }
}
