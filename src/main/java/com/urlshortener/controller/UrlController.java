package com.urlshortener.controller;

import com.urlshortener.domain.ShortenedUrl;
import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.ShortenUrlResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.service.UrlShortenerService;
import com.urlshortener.service.UrlShortenerService.ClientInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

/**
 * REST API endpoints for URL shortening service
 * 
 * Endpoints:
 * - POST /api/v1/shorten - Create shortened URL
 * - GET /{shortCode} - Redirect to original URL
 * - GET /api/v1/stats/{shortCode} - Get URL statistics
 * - DELETE /api/v1/urls/{shortCode} - Delete shortened URL
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UrlController {
    
    private final UrlShortenerService urlShortenerService;
    private static final String API_VERSION = "/api/v1";

    /**
     * Create a shortened URL
     *
     * @param request containing original URL and optional metadata
     * @return the created shortened URL with short code
     */
    @PostMapping(API_VERSION + "/shorten")
    public ResponseEntity<ShortenUrlResponse> shortenUrl(
            @Valid @RequestBody ShortenUrlRequest request,
            HttpServletRequest httpRequest) {

        log.info("Received shorten request for URL: {}", request.getOriginalUrl());

        ShortenedUrl created = urlShortenerService.shortenUrl(request);

        String baseUrl = getBaseUrl(httpRequest);
        ShortenUrlResponse response = ShortenUrlResponse.builder()
            .shortCode(created.getShortCode())
            .shortUrl(baseUrl + "/" + created.getShortCode())
            .originalUrl(created.getOriginalUrl())
            .createdAt(created.getCreatedAt())
            .expiresAt(created.getExpiresAt())
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Redirect to original URL by short code
     *
     * @param shortCode the shortened code
     * @return redirect to original URL
     */
    @GetMapping("/{shortCode}")
    public RedirectView redirectUrl(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        ClientInfo clientInfo = extractClientInfo(request);
        String originalUrl = urlShortenerService.resolveUrl(shortCode, clientInfo);

        return new RedirectView(originalUrl);
    }

    /**
     * Get statistics for a shortened URL
     *
     * @param shortCode the short code
     * @return detailed statistics including click count and metadata
     */
    @GetMapping(API_VERSION + "/stats/{shortCode}")
    public ResponseEntity<UrlStatsResponse> getUrlStats(
            @PathVariable String shortCode) {

        UrlStatsResponse stats = urlShortenerService.getUrlStats(shortCode);
        return ResponseEntity.ok(stats);
    }

    /**
     * Delete a shortened URL
     *
     * @param shortCode the short code to delete
     */
    @DeleteMapping(API_VERSION + "/urls/{shortCode}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable String shortCode) {

        urlShortenerService.deleteUrl(shortCode);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check endpoint
     */
    @GetMapping(API_VERSION + "/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("OK", System.currentTimeMillis()));
    }

    /**
     * Extract client information from HTTP request
     */
    private ClientInfo extractClientInfo(HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        return new ClientInfo(ipAddress, userAgent, referrer);
    }

    /**
     * Get client IP address, handling proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Get base URL for constructing short URLs
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();

        if ((scheme.equals("http") && port == 80) ||
            (scheme.equals("https") && port == 443)) {
            return String.format("%s://%s", scheme, serverName);
        }
        return String.format("%s://%s:%d", scheme, serverName, port);
    }

    /**
     * Simple health response DTO
     */
    public record HealthResponse(String status, long timestamp) {}
}
