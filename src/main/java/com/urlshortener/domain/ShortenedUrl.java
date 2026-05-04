package com.urlshortener.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a shortened URL with associated metadata and analytics.
 * 
 * This entity tracks:
 * - Original long URL
 * - Generated short code
 * - Creation and expiration timestamps
 * - Click analytics
 * - User information (optional)
 */
@Entity
@Table(name = "urls", indexes = {
    @Index(name = "idx_short_code", columnList = "short_code", unique = true),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenedUrl {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 2048)
    private String originalUrl;
    
    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private Long clickCount = 0L;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    private String description;
    
    @Column(length = 50)
    private String tags;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * Check if this shortened URL has expired
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Increment click count (used for analytics)
     */
    public void incrementClickCount() {
        this.clickCount++;
    }
    
    /**
     * Check if URL is available for redirection
     */
    public boolean isAvailable() {
        return isActive && !isExpired();
    }
}
