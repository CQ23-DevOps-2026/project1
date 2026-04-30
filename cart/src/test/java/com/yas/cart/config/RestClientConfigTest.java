package com.yas.cart.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RestClientConfigTest {

    @Test
    void restClientConfig_canBeInstantiated() {
        RestClientConfig config = new RestClientConfig();
        assertThat(config).isNotNull();
    }

    @Test
    void restClient_shouldBuildInstance() {
        RestClientConfig config = new RestClientConfig();

        assertThat(config.restClient()).isNotNull();
    }
}
