package com.urlshortener.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ShortCodeGenerator
 */
@DisplayName("Short Code Generator Tests")
class ShortCodeGeneratorTest {
    
    private final ShortCodeGenerator generator = new ShortCodeGenerator();
    
    @Test
    @DisplayName("Should generate code with default length")
    void testGenerateDefaultLength() {
        String code = generator.generate();
        
        assertThat(code)
            .isNotNull()
            .hasSize(7)
            .matches("[0-9a-zA-Z]+");
    }
    
    @Test
    @DisplayName("Should generate code with custom length")
    void testGenerateCustomLength() {
        String code = generator.generate(5);
        
        assertThat(code)
            .isNotNull()
            .hasSize(5)
            .matches("[0-9a-zA-Z]+");
    }
    
    @Test
    @DisplayName("Should generate unique codes")
    void testGenerateUnique() {
        String code1 = generator.generate();
        String code2 = generator.generate();
        String code3 = generator.generate();
        
        assertThat(code1)
            .isNotEqualTo(code2)
            .isNotEqualTo(code3);
        
        assertThat(code2).isNotEqualTo(code3);
    }

    @Test
    @DisplayName("Should handle encoding/decoding from ID")
    void testEncodeDecodeFromId() {
        long originalId = 12345L;

        String encoded = generator.generateFromId(originalId);
        long decoded = generator.decodeToId(encoded);

        assertThat(decoded).isEqualTo(originalId);
    }
    
    @Test
    @DisplayName("Should throw exception for invalid length")
    void testGenerateInvalidLength() {
        assertThatThrownBy(() -> generator.generate(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Length must be positive");
    }
    
    @Test
    @DisplayName("Should throw exception for negative ID")
    void testGenerateFromNegativeId() {
        assertThatThrownBy(() -> generator.generateFromId(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("Should handle zero ID")
    void testGenerateFromZeroId() {
        String code = generator.generateFromId(0);
        assertThat(code).isEqualTo("0");
    }
}
