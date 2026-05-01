package com.yas.rating.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

class RestClientConfigTest {

    @Test
    void getRestClient_shouldReturnConfiguredRestClient() {
        RestClientConfig restClientConfig = new RestClientConfig();
        RestClient.Builder restClientBuilder = mock(RestClient.Builder.class);
        RestClient restClient = mock(RestClient.class);

        when(restClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        RestClient result = restClientConfig.getRestClient(restClientBuilder);

        assertThat(result).isEqualTo(restClient);
        verify(restClientBuilder).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        verify(restClientBuilder).build();
    }
}
