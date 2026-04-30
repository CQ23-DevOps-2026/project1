package com.yas.rating.service;

import static com.yas.rating.util.SecurityContextUtils.setUpSecurityContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.rating.config.ServiceUrlConfig;
import com.yas.rating.viewmodel.CustomerVm;
import java.net.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

class CustomerServiceTest {

    private RestClient restClient;
    private ServiceUrlConfig serviceUrlConfig;
    private CustomerService customerService;
    private RestClient.ResponseSpec responseSpec;

    private static final String CUSTOMER_URL = "http://api.yas.local/customer";

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        serviceUrlConfig = mock(ServiceUrlConfig.class);
        customerService = new CustomerService(restClient, serviceUrlConfig);
        responseSpec = Mockito.mock(RestClient.ResponseSpec.class);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCustomer_whenNormalCase_shouldReturnCustomerVm() {
        // Arrange
        when(serviceUrlConfig.customer()).thenReturn(CUSTOMER_URL);
        URI url = UriComponentsBuilder
                .fromUriString(CUSTOMER_URL)
                .path("/storefront/customer/profile")
                .buildAndExpand()
                .toUri();

        setUpSecurityContext("test-user");
        
        RestClient.RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        CustomerVm expectedCustomerVm = new CustomerVm("user", "email", "first", "last");
        when(responseSpec.body(CustomerVm.class)).thenReturn(expectedCustomerVm);

        // Act
        CustomerVm result = customerService.getCustomer();

        // Assert
        assertThat(result).isEqualTo(expectedCustomerVm);
    }

    @Test
    void handleFallback_shouldReturnNull() throws Throwable {
        // Act
        CustomerVm result = customerService.handleFallback(new RuntimeException("Test exception"));

        // Assert
        assertThat(result).isNull();
    }
}
