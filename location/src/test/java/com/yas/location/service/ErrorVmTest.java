package com.yas.location.viewmodel.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void testCompactConstructor_whenCalled_thenFieldErrorsInitialized() {
        ErrorVm errorVm = new ErrorVm("400", "Bad Request", "Invalid request");

        assertEquals("400", errorVm.statusCode());
        assertEquals("Bad Request", errorVm.title());
        assertEquals("Invalid request", errorVm.detail());
        assertNotNull(errorVm.fieldErrors());
        assertTrue(errorVm.fieldErrors().isEmpty());
    }

    @Test
    void testCanonicalConstructor_whenCalled_thenFieldErrorsPreserved() {
        ErrorVm errorVm = new ErrorVm("404", "Not Found", "Missing", List.of("field"));

        assertEquals(List.of("field"), errorVm.fieldErrors());
    }
}
