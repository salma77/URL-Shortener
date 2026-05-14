package com.urlshortener.exception;

/**
 * Thrown when a URL fails validation
 */
public class InvalidUrlException extends RuntimeException {
    
    public InvalidUrlException(String message) {
        super(message);
    }
    
    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
