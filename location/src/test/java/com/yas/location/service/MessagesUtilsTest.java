package com.yas.location.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void testConstructor_whenInstantiated_thenObjectCreated() {
        MessagesUtils messagesUtils = new MessagesUtils();

        assertNotNull(messagesUtils);
    }

    @Test
    void testGetMessage_whenCodeExists_thenReturnFormattedMessage() {
        String result = MessagesUtils.getMessage("COUNTRY_NOT_FOUND", 10);

        assertEquals("The country 10 is not found", result);
    }

    @Test
    void testGetMessage_whenCodeDoesNotExist_thenReturnCode() {
        String result = MessagesUtils.getMessage("missing.code");

        assertEquals("missing.code", result);
    }
}
