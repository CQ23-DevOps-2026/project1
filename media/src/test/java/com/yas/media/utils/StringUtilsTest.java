package com.yas.media.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @Test
    void hasText_whenInputIsNull_thenReturnFalse() {
        assertFalse(StringUtils.hasText(null));
    }

    @Test
    void hasText_whenInputIsEmpty_thenReturnFalse() {
        assertFalse(StringUtils.hasText(""));
    }

    @Test
    void hasText_whenInputIsBlankSpaces_thenReturnFalse() {
        assertFalse(StringUtils.hasText("   "));
    }

    @Test
    void hasText_whenInputHasText_thenReturnTrue() {
        assertTrue(StringUtils.hasText("hello"));
    }

    @Test
    void hasText_whenInputHasTextWithLeadingAndTrailingSpaces_thenReturnTrue() {
        assertTrue(StringUtils.hasText("  hello  "));
    }
}
