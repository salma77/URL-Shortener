package com.urlshortener.exception;

/**
 * Thrown when a requested URL is not found
 */
public class UrlNotFoundException extends RuntimeException {
    
    public UrlNotFoundException(String message) {
        super(message);
    }
    
    public UrlNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
