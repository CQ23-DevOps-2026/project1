package com.yas.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.yas.cart.viewmodel.ProductThumbnailVm;
import com.yas.commonlibrary.config.ServiceUrlConfig;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

class ProductServiceTest {

    private static final String BASE_URL = "http://api.yas.local/product";

    RestClient restClient;
    ServiceUrlConfig serviceUrlConfig;
    ProductService productService;
    RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        restClient = Mockito.mock(RestClient.class);
        serviceUrlConfig = Mockito.mock(ServiceUrlConfig.class);
        productService = new ProductService(restClient, serviceUrlConfig);
        requestHeadersUriSpec = Mockito.mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = Mockito.mock(RestClient.ResponseSpec.class);
    }

    // ──────────────────────────────────────────────────────────────
    // getProducts
    // ──────────────────────────────────────────────────────────────
    @Nested
    class GetProductsTest {

        @Test
        void getProducts_NormalCase_ReturnProductThumbnailVms() {
            List<Long> ids = List.of(1L, 2L, 3L);
            URI url = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .path("/storefront/products/list-featured")
                .queryParam("productId", ids)
                .build()
                .toUri();

            when(serviceUrlConfig.product()).thenReturn(BASE_URL);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(new ParameterizedTypeReference<List<ProductThumbnailVm>>() {}))
                .thenReturn(ResponseEntity.ok(buildProducts()));

            List<ProductThumbnailVm> result = productService.getProducts(ids);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(1).id()).isEqualTo(2L);
            assertThat(result.get(2).id()).isEqualTo(3L);
        }

        @Test
        void getProducts_WhenResponseIsEmpty_ReturnEmptyList() {
            List<Long> ids = List.of(99L);
            URI url = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .path("/storefront/products/list-featured")
                .queryParam("productId", ids)
                .build()
                .toUri();

            when(serviceUrlConfig.product()).thenReturn(BASE_URL);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(new ParameterizedTypeReference<List<ProductThumbnailVm>>() {}))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

            List<ProductThumbnailVm> result = productService.getProducts(ids);

            assertThat(result).isEmpty();
        }

        @Test
        void getProducts_WhenResponseBodyIsNull_ReturnNull() {
            List<Long> ids = List.of(1L);
            URI url = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .path("/storefront/products/list-featured")
                .queryParam("productId", ids)
                .build()
                .toUri();

            when(serviceUrlConfig.product()).thenReturn(BASE_URL);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(new ParameterizedTypeReference<List<ProductThumbnailVm>>() {}))
                .thenReturn(ResponseEntity.ok().build());

            List<ProductThumbnailVm> result = productService.getProducts(ids);

            assertThat(result).isNull();
        }
    }

    // ──────────────────────────────────────────────────────────────
    // getProductById
    // ──────────────────────────────────────────────────────────────
    @Nested
    class GetProductByIdTest {

        @Test
        void getProductById_WhenProductExists_ReturnProduct() {
            Long id = 1L;
            List<Long> ids = List.of(id);
            URI url = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .path("/storefront/products/list-featured")
                .queryParam("productId", ids)
                .build()
                .toUri();

            ProductThumbnailVm expected = new ProductThumbnailVm(id, "P1", "p1", "img");

            when(serviceUrlConfig.product()).thenReturn(BASE_URL);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(new ParameterizedTypeReference<List<ProductThumbnailVm>>() {}))
                .thenReturn(ResponseEntity.ok(List.of(expected)));

            ProductThumbnailVm result = productService.getProductById(id);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(id);
        }

        @Test
        void getProductById_WhenProductDoesNotExist_ReturnNull() {
            Long id = 999L;
            List<Long> ids = List.of(id);
            URI url = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .path("/storefront/products/list-featured")
                .queryParam("productId", ids)
                .build()
                .toUri();

            when(serviceUrlConfig.product()).thenReturn(BASE_URL);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(new ParameterizedTypeReference<List<ProductThumbnailVm>>() {}))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

            ProductThumbnailVm result = productService.getProductById(id);

            assertThat(result).isNull();
        }

        @Test
        void getProductById_WhenResponseBodyIsNull_ReturnNull() {
            Long id = 123L;
            List<Long> ids = List.of(id);
            URI url = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .path("/storefront/products/list-featured")
                .queryParam("productId", ids)
                .build()
                .toUri();

            when(serviceUrlConfig.product()).thenReturn(BASE_URL);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(new ParameterizedTypeReference<List<ProductThumbnailVm>>() {}))
                .thenReturn(ResponseEntity.ok().build());

            ProductThumbnailVm result = productService.getProductById(id);

            assertThat(result).isNull();
        }
    }

    // ──────────────────────────────────────────────────────────────
    // existsById
    // ──────────────────────────────────────────────────────────────
    @Nested
    class ExistsByIdTest {

        @Test
        void existsById_WhenProductExists_ReturnTrue() {
            Long id = 1L;
            List<Long> ids = List.of(id);
            URI url = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .path("/storefront/products/list-featured")
                .queryParam("productId", ids)
                .build()
                .toUri();

            when(serviceUrlConfig.product()).thenReturn(BASE_URL);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(new ParameterizedTypeReference<List<ProductThumbnailVm>>() {}))
                .thenReturn(ResponseEntity.ok(List.of(new ProductThumbnailVm(id, "P1", "p1", "img"))));

            assertThat(productService.existsById(id)).isTrue();
        }

        @Test
        void existsById_WhenProductDoesNotExist_ReturnFalse() {
            Long id = 999L;
            List<Long> ids = List.of(id);
            URI url = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .path("/storefront/products/list-featured")
                .queryParam("productId", ids)
                .build()
                .toUri();

            when(serviceUrlConfig.product()).thenReturn(BASE_URL);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(new ParameterizedTypeReference<List<ProductThumbnailVm>>() {}))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

            assertThat(productService.existsById(id)).isFalse();
        }

        @Test
        void existsById_WhenResponseBodyIsNull_ReturnFalse() {
            Long id = 123L;
            List<Long> ids = List.of(id);
            URI url = UriComponentsBuilder
                .fromUriString(BASE_URL)
                .path("/storefront/products/list-featured")
                .queryParam("productId", ids)
                .build()
                .toUri();

            when(serviceUrlConfig.product()).thenReturn(BASE_URL);
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.toEntity(new ParameterizedTypeReference<List<ProductThumbnailVm>>() {}))
                .thenReturn(ResponseEntity.ok().build());

            assertThat(productService.existsById(id)).isFalse();
        }
    }

    // ──────────────────────────────────────────────────────────────
    // handleProductThumbnailFallback
    // ──────────────────────────────────────────────────────────────
    @Nested
    class HandleProductThumbnailFallbackTest {

        @Test
        void handleProductThumbnailFallback_ShouldRethrowException() {
            RuntimeException cause = new RuntimeException("circuit open");
            Throwable thrown = null;

            try {
                productService.handleProductThumbnailFallback(cause);
            } catch (Throwable ex) {
                thrown = ex;
            }

            assertThat(thrown).isSameAs(cause);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────
    private List<ProductThumbnailVm> buildProducts() {
        return List.of(
            new ProductThumbnailVm(1L, "Product 1", "product-1", "http://example.com/p1.jpg"),
            new ProductThumbnailVm(2L, "Product 2", "product-2", "http://example.com/p2.jpg"),
            new ProductThumbnailVm(3L, "Product 3", "product-3", "http://example.com/p3.jpg")
        );
    }
}