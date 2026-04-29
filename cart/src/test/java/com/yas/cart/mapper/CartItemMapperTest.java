package com.yas.cart.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.cart.model.CartItem;
import com.yas.cart.viewmodel.CartItemGetVm;
import com.yas.cart.viewmodel.CartItemPostVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CartItemMapperTest {

    private CartItemMapper cartItemMapper;

    private static final String CUSTOMER_ID = "user-123";
    private static final Long PRODUCT_ID = 42L;
    private static final int QUANTITY = 5;

    @BeforeEach
    void setUp() {
        cartItemMapper = new CartItemMapper();
    }

    // ──────────────────────────────────────────────────────────────
    // toGetVm
    // ──────────────────────────────────────────────────────────────
    @Nested
    class ToGetVmTest {

        @Test
        void toGetVm_shouldMapAllFields() {
            CartItem cartItem = CartItem.builder()
                .customerId(CUSTOMER_ID)
                .productId(PRODUCT_ID)
                .quantity(QUANTITY)
                .build();

            CartItemGetVm result = cartItemMapper.toGetVm(cartItem);

            assertThat(result).isNotNull();
            assertThat(result.customerId()).isEqualTo(CUSTOMER_ID);
            assertThat(result.productId()).isEqualTo(PRODUCT_ID);
            assertThat(result.quantity()).isEqualTo(QUANTITY);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // toCartItem(CartItemPostVm, String userId)
    // ──────────────────────────────────────────────────────────────
    @Nested
    class ToCartItemFromPostVmTest {

        @Test
        void toCartItem_fromPostVm_shouldMapAllFields() {
            CartItemPostVm postVm = CartItemPostVm.builder()
                .productId(PRODUCT_ID)
                .quantity(QUANTITY)
                .build();

            CartItem result = cartItemMapper.toCartItem(postVm, CUSTOMER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(result.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(result.getQuantity()).isEqualTo(QUANTITY);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // toCartItem(String userId, Long productId, int quantity)
    // ──────────────────────────────────────────────────────────────
    @Nested
    class ToCartItemFromFieldsTest {

        @Test
        void toCartItem_fromFields_shouldMapAllFields() {
            CartItem result = cartItemMapper.toCartItem(CUSTOMER_ID, PRODUCT_ID, QUANTITY);

            assertThat(result).isNotNull();
            assertThat(result.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(result.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(result.getQuantity()).isEqualTo(QUANTITY);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // toGetVms
    // ──────────────────────────────────────────────────────────────
    @Nested
    class ToGetVmsTest {

        @Test
        void toGetVms_withMultipleItems_shouldMapAll() {
            CartItem item1 = CartItem.builder().customerId(CUSTOMER_ID).productId(1L).quantity(1).build();
            CartItem item2 = CartItem.builder().customerId(CUSTOMER_ID).productId(2L).quantity(2).build();

            List<CartItemGetVm> result = cartItemMapper.toGetVms(List.of(item1, item2));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(1L);
            assertThat(result.get(1).productId()).isEqualTo(2L);
        }

        @Test
        void toGetVms_withEmptyList_shouldReturnEmptyList() {
            List<CartItemGetVm> result = cartItemMapper.toGetVms(List.of());

            assertThat(result).isEmpty();
        }
    }

    @Test
    void cartItemMapper_canBeInstantiated() {
        CartItemMapper mapper = new CartItemMapper();
        assertThat(mapper).isNotNull();
    }
}
