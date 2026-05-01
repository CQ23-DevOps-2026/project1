package com.yas.customer.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yas.customer.viewmodel.ErrorVm;
import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void testCompactConstructor_whenCalled_thenFieldErrorsInitializedAsEmptyList() {
        ErrorVm errorVm = new ErrorVm("400", "Bad Request", "Validation failed");

        assertEquals("400", errorVm.statusCode());
        assertEquals("Bad Request", errorVm.title());
        assertEquals("Validation failed", errorVm.detail());
        assertNotNull(errorVm.fieldErrors());
        assertTrue(errorVm.fieldErrors().isEmpty());
    }

    @Test
    void testCanonicalConstructor_whenCalled_thenFieldErrorsPreserved() {
        ErrorVm errorVm = new ErrorVm("404", "Not Found", "Missing", List.of("email"));

        assertEquals(List.of("email"), errorVm.fieldErrors());
    }
}
