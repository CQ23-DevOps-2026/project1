package com.yas.customer.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AbstractCircuitBreakFallbackHandlerTest {

    private final TestFallbackHandler fallbackHandler = new TestFallbackHandler();

    @Test
    void testHandleBodilessFallback_whenErrorOccurs_thenRethrowOriginalThrowable() {
        RuntimeException exception = new RuntimeException("boom");

        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> fallbackHandler.callHandleBodilessFallback(exception));

        assertSame(exception, thrown);
    }

    @Test
    void testHandleTypedFallback_whenErrorOccurs_thenRethrowOriginalThrowable() {
        IllegalStateException exception = new IllegalStateException("typed boom");

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> fallbackHandler.callHandleTypedFallback(exception));

        assertSame(exception, thrown);
    }

    private static class TestFallbackHandler extends AbstractCircuitBreakFallbackHandler {

        void callHandleBodilessFallback(Throwable throwable) throws Throwable {
            handleBodilessFallback(throwable);
        }

        String callHandleTypedFallback(Throwable throwable) throws Throwable {
            return handleTypedFallback(throwable);
        }
    }
}
