package com.yas.cart.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CartViewModelTest {

    @Test
    void cartItemPostVm_builderShouldSetFields() {
        CartItemPostVm vm = CartItemPostVm.builder()
            .productId(1L)
            .quantity(2)
            .build();

        assertThat(vm.productId()).isEqualTo(1L);
        assertThat(vm.quantity()).isEqualTo(2);
    }

    @Test
    void cartItemDeleteVm_builderShouldSetFields() {
        CartItemDeleteVm vm = CartItemDeleteVm.builder()
            .productId(2L)
            .quantity(1)
            .build();

        assertThat(vm.productId()).isEqualTo(2L);
        assertThat(vm.quantity()).isEqualTo(1);
    }

    @Test
    void cartItemPutVm_constructorShouldSetFields() {
        CartItemPutVm vm = new CartItemPutVm(3);

        assertThat(vm.quantity()).isEqualTo(3);
    }

    @Test
    void cartItemGetVm_builderShouldSetFields() {
        CartItemGetVm vm = CartItemGetVm.builder()
            .customerId("user-1")
            .productId(3L)
            .quantity(5)
            .build();

        assertThat(vm.customerId()).isEqualTo("user-1");
        assertThat(vm.productId()).isEqualTo(3L);
        assertThat(vm.quantity()).isEqualTo(5);
    }

    @Test
    void productThumbnailVm_builderShouldSetFields() {
        ProductThumbnailVm vm = ProductThumbnailVm.builder()
            .id(9L)
            .name("Name")
            .slug("slug")
            .thumbnailUrl("url")
            .build();

        assertThat(vm.id()).isEqualTo(9L);
        assertThat(vm.name()).isEqualTo("Name");
        assertThat(vm.slug()).isEqualTo("slug");
        assertThat(vm.thumbnailUrl()).isEqualTo("url");
    }
}
