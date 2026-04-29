package com.yas.cart.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AbstractCircuitBreakFallbackHandlerTest {

    private static class TestHandler extends AbstractCircuitBreakFallbackHandler {
        void callBodiless(Throwable throwable) throws Throwable {
            handleBodilessFallback(throwable);
        }

        Object callTyped(Throwable throwable) throws Throwable {
            return handleTypedFallback(throwable);
        }
    }

    @Test
    void handleBodilessFallback_shouldRethrowException() {
        TestHandler handler = new TestHandler();
        RuntimeException exception = new RuntimeException("circuit open");

        assertThatThrownBy(() -> handler.callBodiless(exception))
            .isSameAs(exception);
    }

    @Test
    void handleTypedFallback_shouldRethrowException() {
        TestHandler handler = new TestHandler();
        RuntimeException exception = new RuntimeException("circuit open");

        assertThatThrownBy(() -> handler.callTyped(exception))
            .isSameAs(exception);
    }
}
