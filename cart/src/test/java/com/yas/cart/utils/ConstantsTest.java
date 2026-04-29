package com.yas.cart.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void errorCodes_shouldBeDefined() {
        assertThat(Constants.ErrorCode.NOT_FOUND_PRODUCT).isEqualTo("NOT_FOUND_PRODUCT");
        assertThat(Constants.ErrorCode.NOT_EXISTING_ITEM_IN_CART).isEqualTo("NOT_EXISTING_ITEM_IN_CART");
        assertThat(Constants.ErrorCode.NOT_EXISTING_PRODUCT_IN_CART).isEqualTo("NOT_EXISTING_PRODUCT_IN_CART");
        assertThat(Constants.ErrorCode.NON_EXISTING_CART_ITEM).isEqualTo("NON_EXISTING_CART_ITEM");
        assertThat(Constants.ErrorCode.ADD_CART_ITEM_FAILED).isEqualTo("ADD_CART_ITEM_FAILED");
        assertThat(Constants.ErrorCode.DUPLICATED_CART_ITEMS_TO_DELETE)
            .isEqualTo("DUPLICATED_CART_ITEMS_TO_DELETE");
    }

    @Test
    void constants_constructors_shouldBeCallable_forCoverage() {
        assertThatCode(Constants::new).doesNotThrowAnyException();
        assertThatCode(Constants.ErrorCode::new).doesNotThrowAnyException();
    }
}
