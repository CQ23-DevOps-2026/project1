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

    @Test
    void recordMethodsShouldWork() {
        CartItemPostVm postVm1 = new CartItemPostVm(1L, 2);
        CartItemPostVm postVm2 = new CartItemPostVm(1L, 2);
        assertThat(postVm1.equals(postVm2)).isTrue();
        assertThat(postVm1.hashCode()).isEqualTo(postVm2.hashCode());
        assertThat(postVm1.toString()).contains("1");

        CartItemDeleteVm deleteVm1 = new CartItemDeleteVm(1L, 2);
        CartItemDeleteVm deleteVm2 = new CartItemDeleteVm(1L, 2);
        assertThat(deleteVm1.equals(deleteVm2)).isTrue();
        assertThat(deleteVm1.hashCode()).isEqualTo(deleteVm2.hashCode());
        assertThat(deleteVm1.toString()).contains("1");

        CartItemPutVm putVm1 = new CartItemPutVm(1);
        CartItemPutVm putVm2 = new CartItemPutVm(1);
        assertThat(putVm1.equals(putVm2)).isTrue();
        assertThat(putVm1.hashCode()).isEqualTo(putVm2.hashCode());
        assertThat(putVm1.toString()).contains("1");

        CartItemGetVm getVm1 = new CartItemGetVm("user", 1L, 2);
        CartItemGetVm getVm2 = new CartItemGetVm("user", 1L, 2);
        assertThat(getVm1.equals(getVm2)).isTrue();
        assertThat(getVm1.hashCode()).isEqualTo(getVm2.hashCode());
        assertThat(getVm1.toString()).contains("user");

        ProductThumbnailVm thumbVm1 = new ProductThumbnailVm(1L, "name", "slug", "url");
        ProductThumbnailVm thumbVm2 = new ProductThumbnailVm(1L, "name", "slug", "url");
        assertThat(thumbVm1.equals(thumbVm2)).isTrue();
        assertThat(thumbVm1.hashCode()).isEqualTo(thumbVm2.hashCode());
        assertThat(thumbVm1.toString()).contains("name");
    }
}
