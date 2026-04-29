package com.yas.cart.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CartItemIdTest {

    @Test
    void gettersSettersAndEquals_shouldWork() {
        CartItemId first = new CartItemId();
        first.setCustomerId("user-1");
        first.setProductId(10L);

        CartItemId second = new CartItemId("user-1", 10L);

        assertThat(first.getCustomerId()).isEqualTo("user-1");
        assertThat(first.getProductId()).isEqualTo(10L);
        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }
}
