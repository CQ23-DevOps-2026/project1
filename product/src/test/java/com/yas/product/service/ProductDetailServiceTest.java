package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.*;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeGroup;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.repository.ProductOptionCombinationRepository;
import com.yas.product.repository.ProductRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.ProductDetailInfoVm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductDetailServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private MediaService mediaService;
    @Mock private ProductOptionCombinationRepository productOptionCombinationRepository;

    @InjectMocks private ProductDetailService productDetailService;

    private Product product;
    private Brand brand;
    private Category category;
    private NoFileMediaVm noFileMediaVm;

    @BeforeEach
    void setUp() {
        brand = new Brand();
        brand.setId(1L);
        brand.setName("TestBrand");

        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        product = Product.builder()
            .id(1L).name("Product A").slug("product-a")
            .sku("SKU-A").gtin("GTIN-A").price(99.99)
            .isPublished(true).isFeatured(false)
            .isAllowedToOrder(true).isVisibleIndividually(true)
            .stockTrackingEnabled(false).hasOptions(false)
            .thumbnailMediaId(10L).brand(brand)
            .shortDescription("Short desc").description("Full desc")
            .specification("Spec").metaTitle("Meta title")
            .metaKeyword("keyword").metaDescription("Meta desc")
            .taxClassId(1L)
            .build();

        ProductCategory pc = ProductCategory.builder().product(product).category(category).build();
        product.setProductCategories(List.of(pc));
        product.setAttributeValues(new ArrayList<>());
        product.setProductImages(new ArrayList<>());

        noFileMediaVm = new NoFileMediaVm(10L, "cap", "img.jpg", "image/jpeg", "http://img/url.jpg");
    }

    @Test
    void getProductDetailById_WhenProductExists_ShouldReturnDetail() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(10L)).thenReturn(noFileMediaVm);

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Product A", result.getName());
        assertEquals("product-a", result.getSlug());
        assertEquals(1L, result.getBrandId());
        assertEquals("TestBrand", result.getBrandName());
        assertEquals(1, result.getCategories().size());
    }

    @Test
    void getProductDetailById_WhenNotFound_ShouldThrow() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productDetailService.getProductDetailById(999L));
    }

    @Test
    void getProductDetailById_WhenUnpublished_ShouldThrow() {
        product.setPublished(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        assertThrows(NotFoundException.class, () -> productDetailService.getProductDetailById(1L));
    }

    @Test
    void getProductDetailById_NoBrand_ShouldReturnNullBrand() {
        product.setBrand(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(10L)).thenReturn(noFileMediaVm);

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertNull(result.getBrandId());
        assertNull(result.getBrandName());
    }

    @Test
    void getProductDetailById_NoThumbnail() {
        product.setThumbnailMediaId(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertNull(result.getThumbnail());
    }

    @Test
    void getProductDetailById_WithProductImages() {
        ProductImage img1 = ProductImage.builder().imageId(20L).product(product).build();
        ProductImage img2 = ProductImage.builder().imageId(21L).product(product).build();
        product.setProductImages(List.of(img1, img2));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertEquals(2, result.getProductImages().size());
    }

    @Test
    void getProductDetailById_WithAttributes() {
        ProductAttributeGroup group = new ProductAttributeGroup();
        group.setName("General");
        ProductAttribute attr = ProductAttribute.builder().name("Color").productAttributeGroup(group).build();
        ProductAttributeValue attrVal = new ProductAttributeValue();
        attrVal.setId(1L);
        attrVal.setProductAttribute(attr);
        attrVal.setValue("Red");
        product.setAttributeValues(List.of(attrVal));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(10L)).thenReturn(noFileMediaVm);

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertEquals(1, result.getAttributeValues().size());
        assertEquals("Color", result.getAttributeValues().get(0).nameProductAttribute());
    }

    @Test
    void getProductDetailById_WithVariations() {
        product.setHasOptions(true);

        Product variation = Product.builder()
            .id(2L).name("Variation A").slug("var-a")
            .sku("SKU-V").gtin("GTIN-V").price(109.99)
            .isPublished(true).thumbnailMediaId(11L)
            .build();
        variation.setProductImages(new ArrayList<>());
        product.setProducts(List.of(variation));

        ProductOption option = new ProductOption();
        option.setId(1L);
        option.setName("Size");

        ProductOptionCombination combo = ProductOptionCombination.builder()
            .id(1L).product(variation).productOption(option).value("Large").build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combo));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertEquals(1, result.getVariations().size());
        assertEquals("Variation A", result.getVariations().get(0).name());
    }

    @Test
    void getProductDetailById_WithVariations_UnpublishedFiltered() {
        product.setHasOptions(true);

        Product pubVar = Product.builder().id(2L).name("Published").slug("pub")
            .sku("S1").price(10.0).isPublished(true).thumbnailMediaId(11L).build();
        pubVar.setProductImages(new ArrayList<>());

        Product unpubVar = Product.builder().id(3L).name("Unpublished").slug("unpub")
            .sku("S2").price(20.0).isPublished(false).build();
        unpubVar.setProductImages(new ArrayList<>());

        product.setProducts(List.of(pubVar, unpubVar));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(pubVar)).thenReturn(Collections.emptyList());
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertEquals(1, result.getVariations().size());
        assertEquals("Published", result.getVariations().get(0).name());
    }

    @Test
    void getProductDetailById_NullCategories() {
        product.setProductCategories(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(10L)).thenReturn(noFileMediaVm);

        ProductDetailInfoVm result = productDetailService.getProductDetailById(1L);

        assertTrue(result.getCategories().isEmpty());
    }
}
