package com.urlshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; /**
 * Response DTO for URL statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlStatsResponse {
    
    private String shortCode;
    
    private String originalUrl;
    
    private Long totalClicks;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;
    
    private Boolean isActive;
    
    private String description;
}
