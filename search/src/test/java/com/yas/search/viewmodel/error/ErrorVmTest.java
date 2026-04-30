package com.yas.search.viewmodel.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void constructor_WithAllFields_ShouldSetAllValues() {
        List<String> fieldErrors = List.of("name is required", "price must be positive");
        ErrorVm vm = new ErrorVm("400", "Bad Request", "Validation failed", fieldErrors);

        assertEquals("400", vm.statusCode());
        assertEquals("Bad Request", vm.title());
        assertEquals("Validation failed", vm.detail());
        assertEquals(2, vm.fieldErrors().size());
    }

    @Test
    void constructor_WithoutFieldErrors_ShouldHaveEmptyList() {
        ErrorVm vm = new ErrorVm("404", "Not Found", "Product not found");

        assertEquals("404", vm.statusCode());
        assertEquals("Not Found", vm.title());
        assertEquals("Product not found", vm.detail());
        assertNotNull(vm.fieldErrors());
        assertTrue(vm.fieldErrors().isEmpty());
    }
}
