package com.yas.cart.service;

import static org.assertj.core.api.Assertions.assertThat;

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

        Throwable thrown = null;
        try {
            handler.callBodiless(exception);
        } catch (Throwable ex) {
            thrown = ex;
        }

        assertThat(thrown).isSameAs(exception);
    }

    @Test
    void handleTypedFallback_shouldRethrowException() {
        TestHandler handler = new TestHandler();
        RuntimeException exception = new RuntimeException("circuit open");

        Throwable thrown = null;
        try {
            handler.callTyped(exception);
        } catch (Throwable ex) {
            thrown = ex;
        }

        assertThat(thrown).isSameAs(exception);
    }

    @Test
    void handleBodilessFallback_withCheckedException_shouldRethrow() {
        TestHandler handler = new TestHandler();
        Exception checkedException = new Exception("checked error");

        Throwable thrown = null;
        try {
            handler.callBodiless(checkedException);
        } catch (Throwable ex) {
            thrown = ex;
        }

        assertThat(thrown).isSameAs(checkedException);
    }

    @Test
    void handleTypedFallback_withCheckedException_shouldRethrow() {
        TestHandler handler = new TestHandler();
        Exception checkedException = new Exception("checked error");

        Throwable thrown = null;
        try {
            handler.callTyped(checkedException);
        } catch (Throwable ex) {
            thrown = ex;
        }

        assertThat(thrown).isSameAs(checkedException);
    }
}

class RegularTestHandler extends AbstractCircuitBreakFallbackHandler {
    public void callBodiless(Throwable throwable) throws Throwable {
        handleBodilessFallback(throwable);
    }

    public <T> T callTyped(Throwable throwable) throws Throwable {
        return handleTypedFallback(throwable);
    }
}
