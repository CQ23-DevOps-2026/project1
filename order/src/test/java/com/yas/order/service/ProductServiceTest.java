package com.yas.order.service;

import static com.yas.order.utils.SecurityContextUtils.setUpSecurityContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.order.config.ServiceUrlConfig;
import com.yas.order.viewmodel.order.OrderItemVm;
import com.yas.order.viewmodel.order.OrderVm;
import com.yas.order.viewmodel.product.ProductCheckoutListVm;
import com.yas.order.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.order.viewmodel.product.ProductVariationVm;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ProductServiceTest {

    private RestClient restClient;
    private ServiceUrlConfig serviceUrlConfig;
    private ProductService productService;
    private RestClient.ResponseSpec responseSpec;
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    private static final String PRODUCT_URL = "http://api.yas.local/product";

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        serviceUrlConfig = mock(ServiceUrlConfig.class);
        productService = new ProductService(restClient, serviceUrlConfig);
        responseSpec = mock(RestClient.ResponseSpec.class);
        requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        
        setUpSecurityContext("test");
        when(serviceUrlConfig.product()).thenReturn(PRODUCT_URL);
    }

    @Test
    void getProductVariations_Success_ReturnsList() {
        // Arrange
        List<ProductVariationVm> variations = Instancio.ofList(ProductVariationVm.class).size(2).create();
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(variations));

        // Act
        List<ProductVariationVm> result = productService.getProductVariations(1L);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void subtractProductStockQuantity_Success_NoException() {
        // Arrange
        OrderVm orderVm = Instancio.create(OrderVm.class);
        when(restClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(URI.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(Object.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

        // Act & Assert
        assertDoesNotThrow(() -> productService.subtractProductStockQuantity(orderVm));
    }

    @Test
    void getProductInfomation_Success_ReturnsMap() {
        // Arrange
        ProductCheckoutListVm product = Instancio.create(ProductCheckoutListVm.class);
        ProductGetCheckoutListVm response = new ProductGetCheckoutListVm(List.of(product), 0, 1, 1, 1, true);
        
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(response));

        // Act
        Map<Long, ProductCheckoutListVm> result = productService.getProductInfomation(Set.of(1L), 0, 10);

        // Assert
        assertThat(result).containsKey(product.getId());
    }

    @Test
    void getProductInfomation_NullResponse_ThrowsNotFoundException() {
        // Arrange
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productService.getProductInfomation(Set.of(1L), 0, 10));
    }
}
