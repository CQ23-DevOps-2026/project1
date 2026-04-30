package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.tax.config.ServiceUrlConfig;
import com.yas.tax.viewmodel.location.StateOrProvinceAndCountryGetNameVm;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;

class LocationServiceTest {

    private RestClient restClient;
    private ServiceUrlConfig serviceUrlConfig;
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        serviceUrlConfig = mock(ServiceUrlConfig.class);
        locationService = new LocationService(restClient, serviceUrlConfig);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getStateOrProvinceAndCountryNames_ShouldReturnList() {
        // Mock SecurityContext
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(serviceUrlConfig.location()).thenReturn("http://location");

        // Mock RestClient fluent API
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(any(java.net.URI.class))).thenReturn(headersSpec);
        when(headersSpec.headers(any(Consumer.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        // StateOrProvinceAndCountryGetNameVm(stateOrProvinceId, stateOrProvinceName, countryName)
        List<StateOrProvinceAndCountryGetNameVm> expected =
            List.of(new StateOrProvinceAndCountryGetNameVm(1L, "S", "C"));
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expected);

        List<StateOrProvinceAndCountryGetNameVm> result =
            locationService.getStateOrProvinceAndCountryNames(List.of(1L));

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void handleLocationNameListFallback_ShouldThrowException() {
        Throwable throwable = new RuntimeException("Error");
        assertThatThrownBy(() -> locationService.handleLocationNameListFallback(throwable))
            .isEqualTo(throwable);
    }
}
