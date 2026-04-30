package com.yas.rating.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SwaggerConfigTest {

    @Test
    void testSwaggerConfigInstantiation() {
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        assertThat(swaggerConfig).isNotNull();
    }
}
