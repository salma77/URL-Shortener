package com.urlshortener.util;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Generates short, unique codes for URL shortening.
 * 
 * Uses base62 encoding (0-9, a-z, A-Z) to create human-friendly, 
 * collision-resistant codes. Default length is 6-8 characters.
 * 
 * Examples: "abc123", "xYz9Qw", "a1b2c3d4"
 */
@Component
public class ShortCodeGenerator {
    
    private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int DEFAULT_LENGTH = 7;
    private final Random random = new Random();
    
    /**
     * Generate a random short code
     * 
     * @return short code of DEFAULT_LENGTH characters
     */
    public String generate() {
        return generate(DEFAULT_LENGTH);
    }
    
    /**
     * Generate a random short code of specified length
     * 
     * @param length desired length of short code
     * @return randomly generated short code
     */
    public String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
    
    /**
     * Generate short code from a numeric ID (deterministic alternative)
     * 
     * Useful for generating consistent short codes from IDs.
     * This is the base62 encoding of the numeric ID.
     * 
     * @param id numeric identifier
     * @return base62-encoded short code
     */
    public String generateFromId(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID must be non-negative");
        }
        
        if (id == 0) {
            return "0";
        }
        
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(CHARACTERS.charAt((int) (id % CHARACTERS.length())));
            id /= CHARACTERS.length();
        }
        
        return sb.reverse().toString();
    }

    /**
     * Decode base62 short code back to numeric ID
     *
     * @param code base62-encoded short code
     * @return original numeric ID
     */
    public long decodeToId(String code) {
        long id = 0;
        for (char c : code.toCharArray()) {
            int digit = CHARACTERS.indexOf(c);
            if (digit == -1) {
                throw new IllegalArgumentException("Invalid character in code: " + c);
            }
            id = id * CHARACTERS.length() + digit;
        }
        return id;
    }
}
