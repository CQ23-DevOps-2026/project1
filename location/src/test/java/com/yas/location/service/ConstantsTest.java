package com.yas.location.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void testConstantsAndNestedClasses_whenInstantiated_thenAvailable() {
        Constants constants = new Constants();
        Constants.ErrorCode errorCode = constants.new ErrorCode();
        Constants.PageableConstant pageableConstant = constants.new PageableConstant();
        Constants.ApiConstant apiConstant = constants.new ApiConstant();

        assertNotNull(constants);
        assertNotNull(errorCode);
        assertNotNull(pageableConstant);
        assertNotNull(apiConstant);
        assertEquals("/storefront/countries", Constants.ApiConstant.COUNTRIES_STOREFRONT_URL);
        assertEquals("10", Constants.PageableConstant.DEFAULT_PAGE_SIZE);
        assertEquals("COUNTRY_NOT_FOUND", Constants.ErrorCode.COUNTRY_NOT_FOUND);
    }
}
