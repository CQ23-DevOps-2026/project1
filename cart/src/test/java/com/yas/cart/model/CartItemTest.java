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

    @Test
    void noArgsConstructor_and_allArgsConstructor_shouldWork() {
        CartItem item1 = new CartItem();
        item1.setCustomerId("c1");
        item1.setProductId(1L);
        item1.setQuantity(2);
        
        assertThat(item1.getCustomerId()).isEqualTo("c1");
        assertThat(item1.getProductId()).isEqualTo(1L);
        assertThat(item1.getQuantity()).isEqualTo(2);

        CartItem item2 = new CartItem("c2", 2L, 3);
        assertThat(item2.getCustomerId()).isEqualTo("c2");
        assertThat(item2.getProductId()).isEqualTo(2L);
        assertThat(item2.getQuantity()).isEqualTo(3);
    }

    @Test
    void builder_toString_shouldWork() {
        String builderString = CartItem.builder()
            .customerId("c1")
            .productId(1L)
            .quantity(2)
            .toString();
        assertThat(builderString).contains("c1");
    }
}
