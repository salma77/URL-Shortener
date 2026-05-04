package com.urlshortener.controller;

import org.springframework.web.bind.annotation.RestController;

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
public class UrlController {
    

}
