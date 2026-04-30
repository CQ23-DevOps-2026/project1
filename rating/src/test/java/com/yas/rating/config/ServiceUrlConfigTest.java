package com.yas.rating.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ServiceUrlConfigTest {

    @Test
    void testServiceUrlConfig() {
        ServiceUrlConfig config = new ServiceUrlConfig("product", "customer", "order");
        assertThat(config.product()).isEqualTo("product");
        assertThat(config.customer()).isEqualTo("customer");
        assertThat(config.order()).isEqualTo("order");
    }
}
