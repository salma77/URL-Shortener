package com.urlshortener.service;

import com.urlshortener.domain.ClickAnalytics;
import com.urlshortener.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling click analytics and statistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {
    
    private final AnalyticsRepository analyticsRepository;

    /**
     * Record a click event asynchronously
     *
     * This is non-blocking to ensure fast redirects
     */
    @Async
    @Transactional
    public void recordClick(Long urlId, UrlShortenerService.ClientInfo clientInfo) {
        try {
            ClickAnalytics analytics = ClickAnalytics.builder()
                .urlId(urlId)
                .ipAddress(clientInfo.ipAddress())
                .userAgent(clientInfo.userAgent())
                .referrer(clientInfo.referrer())
                .build();

            analyticsRepository.save(analytics);
            log.debug("Recorded click for URL ID: {}", urlId);
        } catch (Exception e) {
            log.error("Failed to record analytics for URL ID: {}", urlId, e);
            // Don't throw - analytics failure shouldn't affect main operation
        }
    }
}
