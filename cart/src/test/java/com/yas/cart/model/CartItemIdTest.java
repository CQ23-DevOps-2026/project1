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
        
        assertThat(first.equals(null)).isFalse();
        assertThat(first.equals(new Object())).isFalse();
        
        CartItemId different = new CartItemId("user-2", 10L);
        assertThat(first.equals(different)).isFalse();
        
        CartItemId different2 = new CartItemId("user-1", 11L);
        assertThat(first.equals(different2)).isFalse();
    }
}
