package com.yas.product.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PriceValidatorTest {

    private PriceValidator priceValidator;

    @BeforeEach
    void setUp() {
        priceValidator = new PriceValidator();
    }

    @Test
    void isValid_PositivePrice_ShouldReturnTrue() {
        assertTrue(priceValidator.isValid(100.0, null));
    }

    @Test
    void isValid_ZeroPrice_ShouldReturnTrue() {
        assertTrue(priceValidator.isValid(0.0, null));
    }

    @Test
    void isValid_NegativePrice_ShouldReturnFalse() {
        assertFalse(priceValidator.isValid(-1.0, null));
    }

    @Test
    void isValid_VerySmallPositive_ShouldReturnTrue() {
        assertTrue(priceValidator.isValid(0.01, null));
    }

    @Test
    void isValid_LargePrice_ShouldReturnTrue() {
        assertTrue(priceValidator.isValid(999999.99, null));
    }
}
