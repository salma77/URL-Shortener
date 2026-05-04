package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a shortened URL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenUrlRequest {

    @NotBlank(message = "Original URL is required")
    @URL(message = "Invalid URL format")
    private String originalUrl;

    private String userId;

    private String description;

    private String tags;

    private LocalDateTime expiresAt;
}