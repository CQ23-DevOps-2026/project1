package com.yas.cart.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CartItemTest {

    @Test
    void builderAndAccessors_shouldWork() {
        CartItem cartItem = CartItem.builder()
            .customerId("user-1")
            .productId(5L)
            .quantity(3)
            .build();

        assertThat(cartItem.getCustomerId()).isEqualTo("user-1");
        assertThat(cartItem.getProductId()).isEqualTo(5L);
        assertThat(cartItem.getQuantity()).isEqualTo(3);

        cartItem.setQuantity(4);
        assertThat(cartItem.getQuantity()).isEqualTo(4);
    }
}
