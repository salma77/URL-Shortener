package com.urlshortener.service;

import com.urlshortener.domain.ShortenedUrl;
import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.repository.AnalyticsRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Service layer for URL shortening operations.
 * 
 * Handles:
 * - Creating shortened URLs with unique codes
 * - Retrieving original URLs
 * - Managing URL expiration and lifecycle
 * - Analytics tracking
 * - Caching for performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerService {

    private final UrlRepository urlRepository;
    private final AnalyticsRepository analyticsRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final AnalyticsService analyticsService;

    private static final int MAX_RETRIES = 3;

    /**
     * Create a shortened URL from a long URL
     *
     * @param request containing original URL and optional metadata
     * @return the created ShortenedUrl entity
     * @throws InvalidUrlException if URL is malformed
     */
    @Transactional
    public ShortenedUrl shortenUrl(ShortenUrlRequest request) {
        log.info("Creating shortened URL for: {}", request.getOriginalUrl());

        // Validate URL
        validateUrl(request.getOriginalUrl());

        // Generate unique short code
        String shortCode = generateUniqueShortCode();

        // Create entity
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
            .originalUrl(request.getOriginalUrl())
            .shortCode(shortCode)
            .userId(request.getUserId())
            .description(request.getDescription())
            .tags(request.getTags())
            .expiresAt(request.getExpiresAt())
            .build();

        // Save to database
        ShortenedUrl saved = urlRepository.save(shortenedUrl);
        log.info("Successfully created shortened URL: {} -> {}", shortCode, request.getOriginalUrl());

        return saved;
    }

    /**
     * Retrieve original URL by short code and record analytics
     *
     * @param shortCode the shortened code
     * @param clientInfo contains IP, user agent, referrer
     * @return the original URL
     * @throws UrlNotFoundException if short code doesn't exist
     */
    @Transactional
    @Cacheable(value = "shortUrl", key = "#shortCode")
    public String resolveUrl(String shortCode, ClientInfo clientInfo) {
        log.debug("Resolving short code: {}", shortCode);

        ShortenedUrl url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException("Short code not found: " + shortCode));

        // Check if expired or inactive
        if (!url.isAvailable()) {
            throw new UrlNotFoundException("This shortened URL is no longer available");
        }

        // Record analytics asynchronously
        analyticsService.recordClick(url.getId(), clientInfo);

        // Increment click count
        url.incrementClickCount();
        urlRepository.save(url);

        return url.getOriginalUrl();
    }

    /**
     * Get detailed statistics for a shortened URL
     *
     * @param shortCode the short code
     * @return analytics and metadata
     */
    @Transactional(readOnly = true)
    public UrlStatsResponse getUrlStats(String shortCode) {
        ShortenedUrl url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException("Short code not found: " + shortCode));

        return UrlStatsResponse.builder()
            .shortCode(shortCode)
            .originalUrl(url.getOriginalUrl())
            .totalClicks(url.getClickCount())
            .createdAt(url.getCreatedAt())
            .expiresAt(url.getExpiresAt())
            .isActive(url.isAvailable())
            .description(url.getDescription())
            .build();
    }

    /**
     * Delete a shortened URL
     *
     * @param shortCode the short code to delete
     */
    @Transactional
    @CacheEvict(value = "shortUrl", key = "#shortCode")
    public void deleteUrl(String shortCode) {
        ShortenedUrl url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new UrlNotFoundException("Short code not found: " + shortCode));

        url.setIsActive(false);
        urlRepository.save(url);
        log.info("Deleted shortened URL: {}", shortCode);
    }

    /**
     * Generate a unique short code (retry on collision)
     */
    private String generateUniqueShortCode() {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String code = shortCodeGenerator.generate();
            if (!urlRepository.existsByShortCode(code)) {
                return code;
            }
            log.warn("Short code collision, retrying... Attempt {}", i + 1);
        }
        throw new RuntimeException("Failed to generate unique short code after " + MAX_RETRIES + " attempts");
    }

    /**
     * Validate URL format
     */
    private void validateUrl(String urlString) {
        try {
            new URL(urlString);
        } catch (MalformedURLException e) {
            throw new InvalidUrlException("Invalid URL format: " + urlString, e);
        }
    }

    /**
     * Data transfer object for client information
     */
    public record ClientInfo(
        String ipAddress,
        String userAgent,
        String referrer
    ) {}
}
