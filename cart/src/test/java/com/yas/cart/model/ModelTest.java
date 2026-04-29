package com.yas.cart.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ModelTest {

    @Test
    void testCartItem() {
        CartItem cartItem = CartItem.builder()
            .customerId("c1")
            .productId(1L)
            .quantity(5)
            .build();
        
        assertThat(cartItem.getCustomerId()).isEqualTo("c1");
        assertThat(cartItem.getProductId()).isEqualTo(1L);
        assertThat(cartItem.getQuantity()).isEqualTo(5);
        
        cartItem.setQuantity(10);
        assertThat(cartItem.getQuantity()).isEqualTo(10);
        
        CartItem other = new CartItem();
        other.setCustomerId("c1");
        other.setProductId(1L);
        
        assertThat(other.getCustomerId()).isEqualTo("c1");
    }

    @Test
    void testCartItemId() {
        CartItemId id1 = new CartItemId("c1", 1L);
        CartItemId id2 = new CartItemId("c1", 1L);
        CartItemId id3 = new CartItemId("c2", 2L);
        
        assertThat(id1).isEqualTo(id2);
        assertThat(id1).isNotEqualTo(id3);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        assertThat(id1.getCustomerId()).isEqualTo("c1");
        assertThat(id1.getProductId()).isEqualTo(1L);
        
        id1.setCustomerId("c3");
        assertThat(id1.getCustomerId()).isEqualTo("c3");
    }
}
