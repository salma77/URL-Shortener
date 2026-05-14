package com.urlshortener.exception;

/**
 * Thrown when a short code generation fails
 */
public class ShortCodeGenerationException extends RuntimeException {
    
    public ShortCodeGenerationException(String message) {
        super(message);
    }
    
    public ShortCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
