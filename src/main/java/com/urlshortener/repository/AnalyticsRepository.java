package com.urlshortener.repository;

import com.urlshortener.domain.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List; /**
 * Repository for ClickAnalytics persistence operations
 */
@Repository
public interface AnalyticsRepository extends JpaRepository<ClickAnalytics, Long> {
    
    /**
     * Find all analytics records for a specific URL
     */
    List<ClickAnalytics> findByUrlId(Long urlId);
    
    /**
     * Count clicks for a URL within a date range
     */
    @Query("SELECT COUNT(a) FROM ClickAnalytics a WHERE a.urlId = :urlId " +
           "AND a.clickedAt BETWEEN :startDate AND :endDate")
    Long countClicksByUrlAndDateRange(@Param("urlId") Long urlId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get most common referrers for a URL
     */
    @Query(value = "SELECT referrer, COUNT(*) as count FROM analytics WHERE url_id = :urlId " +
           "AND referrer IS NOT NULL GROUP BY referrer ORDER BY count DESC LIMIT :limit",
           nativeQuery = true)
    List<Object[]> findTopReferrersByUrl(@Param("urlId") Long urlId, @Param("limit") int limit);
    
    /**
     * Get geographic distribution
     */
    @Query(value = "SELECT country, city, COUNT(*) as count FROM analytics WHERE url_id = :urlId " +
           "GROUP BY country, city ORDER BY count DESC",
           nativeQuery = true)
    List<Object[]> findGeoDistribution(@Param("urlId") Long urlId);
}
