package com.urlshortener.service;

import com.urlshortener.domain.ShortenedUrl;
import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UrlShortenerService
 * 
 * Tests cover:
 * - URL shortening with validation
 * - URL resolution and redirection
 * - Error handling
 * - Analytics integration
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("URL Shortener Service Tests")
class UrlShortenerServiceTest {
    
    @Mock
    private UrlRepository urlRepository;
    
    @Mock
    private ShortCodeGenerator shortCodeGenerator;
    
    @Mock
    private AnalyticsService analyticsService;
    
    @InjectMocks
    private UrlShortenerService service;
    
    private ShortenUrlRequest validRequest;
    private ShortenedUrl testUrl;
    
    @BeforeEach
    void setUp() {
        validRequest = ShortenUrlRequest.builder()
            .originalUrl("https://www.example.com/very/long/url")
            .userId("user123")
            .description("Test URL")
            .build();
        
        testUrl = ShortenedUrl.builder()
            .id(1L)
            .originalUrl("https://www.example.com/very/long/url")
            .shortCode("abc123")
            .userId("user123")
            .isActive(true)
            .clickCount(0L)
            .build();
    }
    
    @Test
    @DisplayName("Should shorten URL successfully with valid input")
    void testShortenUrlSuccess() {
        // Arrange
        when(shortCodeGenerator.generate()).thenReturn("abc123");
        when(urlRepository.existsByShortCode("abc123")).thenReturn(false);
        when(urlRepository.save(any(ShortenedUrl.class))).thenReturn(testUrl);
        
        // Act
        ShortenedUrl result = service.shortenUrl(validRequest);
        
        // Assert
        assertThat(result)
            .isNotNull()
            .extracting("shortCode", "originalUrl")
            .containsExactly("abc123", "https://www.example.com/very/long/url");
        
        verify(urlRepository, times(1)).save(any(ShortenedUrl.class));
    }
    
    @Test
    @DisplayName("Should throw InvalidUrlException for malformed URL")
    void testShortenUrlWithInvalidUrl() {
        // Arrange
        validRequest.setOriginalUrl("not-a-valid-url");
        
        // Act & Assert
        assertThatThrownBy(() -> service.shortenUrl(validRequest))
            .isInstanceOf(InvalidUrlException.class)
            .hasMessageContaining("Invalid URL format");
    }
    
    @Test
    @DisplayName("Should resolve URL by short code")
    void testResolveUrlSuccess() {
        // Arrange
        when(urlRepository.findByShortCode("abc123"))
            .thenReturn(Optional.of(testUrl));
        
        // Act
        String result = service.resolveUrl("abc123", 
            new UrlShortenerService.ClientInfo("192.168.1.1", "Mozilla", null));
        
        // Assert
        assertThat(result).isEqualTo("https://www.example.com/very/long/url");
        verify(analyticsService, times(1)).recordClick(eq(1L), any());
    }
    
    @Test
    @DisplayName("Should throw UrlNotFoundException for non-existent short code")
    void testResolveUrlNotFound() {
        // Arrange
        when(urlRepository.findByShortCode("invalid")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> service.resolveUrl("invalid", 
            new UrlShortenerService.ClientInfo("192.168.1.1", "Mozilla", null)))
            .isInstanceOf(UrlNotFoundException.class);
    }
    
    @Test
    @DisplayName("Should not resolve expired URL")
    void testResolveExpiredUrl() {
        // Arrange
        ShortenedUrl expiredUrl = ShortenedUrl.builder()
            .shortCode("expired")
            .originalUrl("https://example.com")
            .isActive(true)
            .expiresAt(LocalDateTime.now().minusDays(1))
            .build();
        
        when(urlRepository.findByShortCode("expired")).thenReturn(Optional.of(expiredUrl));
        
        // Act & Assert
        assertThatThrownBy(() -> service.resolveUrl("expired",
            new UrlShortenerService.ClientInfo("192.168.1.1", "Mozilla", null)))
            .isInstanceOf(UrlNotFoundException.class)
            .hasMessageContaining("no longer available");
    }
    
    @Test
    @DisplayName("Should delete URL successfully")
    void testDeleteUrlSuccess() {
        // Arrange
        when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(testUrl));
        
        // Act
        service.deleteUrl("abc123");
        
        // Assert
        verify(urlRepository, times(1)).save(any(ShortenedUrl.class));
    }
    
    @Test
    @DisplayName("Should throw UrlNotFoundException when deleting non-existent URL")
    void testDeleteUrlNotFound() {
        // Arrange
        when(urlRepository.findByShortCode("invalid")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> service.deleteUrl("invalid"))
            .isInstanceOf(UrlNotFoundException.class);
    }
}
