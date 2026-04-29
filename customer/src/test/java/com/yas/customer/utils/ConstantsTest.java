package com.yas.customer.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void testConstantsConstructor_whenInstantiated_thenObjectCreated() {
        Constants constants = new Constants();

        assertNotNull(constants);
    }

    @Test
    void testErrorCodeConstructorAndValues_whenAccessed_thenConstantsAvailable() {
        Constants.ErrorCode errorCode = new Constants.ErrorCode();

        assertNotNull(errorCode);
        assertEquals("USER_NOT_FOUND", Constants.ErrorCode.USER_NOT_FOUND);
        assertEquals("ACTION FAILED, PLEASE LOGIN", Constants.ErrorCode.UNAUTHENTICATED);
    }
}
