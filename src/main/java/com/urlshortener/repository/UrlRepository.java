package com.urlshortener.repository;

import com.urlshortener.domain.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ShortenedUrl persistence operations
 */
@Repository
public interface UrlRepository extends JpaRepository<ShortenedUrl, Long> {

    /**
     * Find a shortened URL by its short code
     */
    Optional<ShortenedUrl> findByShortCode(String shortCode);

    /**
     * Find all URLs created by a specific user
     */
    List<ShortenedUrl> findByUserId(String userId);

    /**
     * Find all active, non-expired URLs for a user
     */
    @Query("SELECT u FROM ShortenedUrl u WHERE u.userId = :userId AND u.isActive = true " +
           "AND (u.expiresAt IS NULL OR u.expiresAt > CURRENT_TIMESTAMP)")
    List<ShortenedUrl> findActiveUrlsByUserId(@Param("userId") String userId);

    /**
     * Find expired URLs that should be cleaned up
     */
    @Query("SELECT u FROM ShortenedUrl u WHERE u.isActive = true " +
           "AND u.expiresAt IS NOT NULL AND u.expiresAt < CURRENT_TIMESTAMP")
    List<ShortenedUrl> findExpiredUrls();

    /**
     * Check if a short code is already taken
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Get top clicked URLs
     */
    @Query(value = "SELECT * FROM urls WHERE is_active = true ORDER BY click_count DESC LIMIT :limit",
           nativeQuery = true)
    List<ShortenedUrl> findTopClickedUrls(@Param("limit") int limit);
}

