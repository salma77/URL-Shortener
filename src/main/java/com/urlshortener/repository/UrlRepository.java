package com.urlshortener.repository;

import com.urlshortener.domain.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ShortenedUrl persistence operations
 */
@Repository
public interface UrlRepository extends JpaRepository<ShortenedUrl, Long> {

}

