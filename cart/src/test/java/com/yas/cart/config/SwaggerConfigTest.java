package com.yas.cart.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SwaggerConfigTest {

    @Test
    void swaggerConfig_canBeInstantiated() {
        SwaggerConfig config = new SwaggerConfig();
        assertThat(config).isNotNull();
    }
}
