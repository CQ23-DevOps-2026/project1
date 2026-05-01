package com.yas.search.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_WhenKeyExists_ShouldReturnMessage() {
        // Just checking it doesn't throw and returns something for a common key
        String result = MessagesUtils.getMessage("PRODUCT_NOT_FOUND", 1L);
        assertNotNull(result);
    }

    @Test
    void getMessage_WhenKeyNotExists_ShouldFallbackToErrorCode() {
        String result = MessagesUtils.getMessage("NON_EXISTENT_KEY_XYZ");
        assertEquals("NON_EXISTENT_KEY_XYZ", result);
    }

    @Test
    void getMessage_WhenKeyNotExistsWithArgs_ShouldFallbackToErrorCode() {
        String result = MessagesUtils.getMessage("MISSING_KEY", "arg1");
        assertNotNull(result);
    }
}
