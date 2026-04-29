package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.*;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeGroup;
import com.yas.product.model.enumeration.DimensionUnit;
import com.yas.product.model.enumeration.FilterExistInWhSelection;
import com.yas.product.repository.*;
import com.yas.product.viewmodel.ImageVm;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.product.*;
import com.yas.product.viewmodel.productoption.ProductOptionValuePostVm;
import com.yas.product.viewmodel.productoption.ProductOptionValuePutVm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private MediaService mediaService;
    @Mock private BrandRepository brandRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductOptionRepository productOptionRepository;
    @Mock private ProductOptionValueRepository productOptionValueRepository;
    @Mock private ProductOptionCombinationRepository productOptionCombinationRepository;
    @Mock private ProductRelatedRepository productRelatedRepository;

    @InjectMocks private ProductService productService;

    private Product product;
    private Brand brand;
    private Category category;
    private NoFileMediaVm noFileMediaVm;

    @BeforeEach
    void setUp() {
        brand = new Brand();
        brand.setId(1L);
        brand.setName("TestBrand");
        brand.setSlug("test-brand");

        category = new Category();
        category.setId(1L);
        category.setName("TestCategory");
        category.setSlug("test-category");

        product = Product.builder()
            .id(1L).name("Test Product").slug("test-product")
            .sku("SKU001").gtin("GTIN001").price(100.0)
            .isPublished(true).isFeatured(true)
            .isAllowedToOrder(true).isVisibleIndividually(true)
            .stockTrackingEnabled(true).stockQuantity(50L)
            .thumbnailMediaId(1L).brand(brand)
            .shortDescription("short").description("desc")
            .specification("spec").metaTitle("meta")
            .metaKeyword("key").metaDescription("metaDesc")
            .taxClassId(1L).hasOptions(false)
            .build();

        ProductCategory pc = ProductCategory.builder().product(product).category(category).build();
        product.setProductCategories(List.of(pc));

        noFileMediaVm = new NoFileMediaVm(1L, "caption", "file.jpg", "image/jpeg", "http://url/file.jpg");
    }

    // ========== getProductsWithFilter ==========
    @Test
    void getProductsWithFilter_ShouldReturnProductList() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.getProductsWithFilter(anyString(), anyString(), any(Pageable.class))).thenReturn(page);

        ProductListGetVm result = productService.getProductsWithFilter(0, 10, "Test", "TestBrand");

        assertNotNull(result);
        assertEquals(1, result.productContent().size());
        assertEquals(0, result.pageNo());
    }

    @Test
    void getProductsWithFilter_EmptyResult() {
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(productRepository.getProductsWithFilter(anyString(), anyString(), any(Pageable.class))).thenReturn(page);

        ProductListGetVm result = productService.getProductsWithFilter(0, 10, "NoMatch", "");

        assertEquals(0, result.productContent().size());
    }

    // ========== getProductById ==========
    @Test
    void getProductById_WhenExists_ShouldReturnDetail() {
        product.setProductImages(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductDetailVm result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.name());
        assertEquals("test-product", result.slug());
        assertEquals(1L, result.brandId());
    }

    @Test
    void getProductById_WhenNotFound_ShouldThrow() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    void getProductById_WithImages() {
        ProductImage img = ProductImage.builder().imageId(2L).product(product).build();
        product.setProductImages(List.of(img));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductDetailVm result = productService.getProductById(1L);

        assertEquals(1, result.productImageMedias().size());
    }

    @Test
    void getProductById_NoBrand() {
        product.setBrand(null);
        product.setProductImages(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductDetailVm result = productService.getProductById(1L);

        assertNull(result.brandId());
    }

    @Test
    void getProductById_NoThumbnail() {
        product.setThumbnailMediaId(null);
        product.setProductImages(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailVm result = productService.getProductById(1L);

        assertNull(result.thumbnailMedia());
    }

    // ========== getLatestProducts ==========
    @Test
    void getLatestProducts_WithResults() {
        when(productRepository.getLatestProducts(any(Pageable.class))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getLatestProducts(5);

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
    }

    @Test
    void getLatestProducts_ZeroCount_ReturnsEmpty() {
        List<ProductListVm> result = productService.getLatestProducts(0);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestProducts_NegativeCount_ReturnsEmpty() {
        List<ProductListVm> result = productService.getLatestProducts(-1);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestProducts_EmptyResult() {
        when(productRepository.getLatestProducts(any(Pageable.class))).thenReturn(Collections.emptyList());
        List<ProductListVm> result = productService.getLatestProducts(5);
        assertTrue(result.isEmpty());
    }

    // ========== getProductsByBrand ==========
    @Test
    void getProductsByBrand_WhenBrandExists() {
        when(brandRepository.findBySlug("test-brand")).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(List.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        List<ProductThumbnailVm> result = productService.getProductsByBrand("test-brand");

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
    }

    @Test
    void getProductsByBrand_WhenBrandNotFound_ShouldThrow() {
        when(brandRepository.findBySlug("no-brand")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductsByBrand("no-brand"));
    }

    // ========== getProductsFromCategory ==========
    @Test
    void getProductsFromCategory_WhenCategoryNotFound_ShouldThrow() {
        when(categoryRepository.findBySlug("no-cat")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductsFromCategory(0, 10, "no-cat"));
    }

    @Test
    void getProductsFromCategory_WhenCategoryExists() {
        ProductCategory pc = ProductCategory.builder().product(product).category(category).build();
        Page<ProductCategory> page = new PageImpl<>(List.of(pc));
        when(categoryRepository.findBySlug("test-category")).thenReturn(Optional.of(category));
        when(productCategoryRepository.findAllByCategory(any(Pageable.class), eq(category))).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductListGetFromCategoryVm result = productService.getProductsFromCategory(0, 10, "test-category");

        assertEquals(1, result.productContent().size());
    }

    // ========== deleteProduct ==========
    @Test
    void deleteProduct_WhenExists_ShouldSetUnpublished() {
        product.setParent(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.deleteProduct(1L);

        assertFalse(product.isPublished());
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_WhenNotFound_ShouldThrow() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.deleteProduct(999L));
    }

    @Test
    void deleteProduct_WithParent_ShouldDeleteCombinations() {
        Product parent = Product.builder().id(2L).name("Parent").build();
        product.setParent(parent);
        List<ProductOptionCombination> combos = List.of(
            ProductOptionCombination.builder().id(1L).product(product).build()
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(product)).thenReturn(combos);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.deleteProduct(1L);

        verify(productOptionCombinationRepository).deleteAll(combos);
    }

    // ========== getProductSlug ==========
    @Test
    void getProductSlug_WhenExists_NoParent() {
        product.setParent(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductSlugGetVm result = productService.getProductSlug(1L);

        assertEquals("test-product", result.slug());
        assertNull(result.productVariantId());
    }

    @Test
    void getProductSlug_WithParent() {
        Product parent = Product.builder().id(2L).slug("parent-slug").build();
        product.setParent(parent);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductSlugGetVm result = productService.getProductSlug(1L);

        assertEquals("parent-slug", result.slug());
        assertEquals(1L, result.productVariantId());
    }

    @Test
    void getProductSlug_WhenNotFound_ShouldThrow() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductSlug(999L));
    }

    // ========== getProductEsDetailById ==========
    @Test
    void getProductEsDetailById_WhenExists() {
        ProductAttributeGroup group = new ProductAttributeGroup();
        group.setName("TestGroup");
        ProductAttribute attr = ProductAttribute.builder().name("Color").productAttributeGroup(group).build();
        ProductAttributeValue attrVal = new ProductAttributeValue();
        attrVal.setProductAttribute(attr);
        attrVal.setValue("Red");
        product.setAttributeValues(List.of(attrVal));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductEsDetailVm result = productService.getProductEsDetailById(1L);

        assertEquals(1L, result.id());
        assertEquals("Test Product", result.name());
        assertEquals("TestBrand", result.brand());
        assertEquals(1, result.categories().size());
        assertEquals(1, result.attributes().size());
    }

    @Test
    void getProductEsDetailById_NoBrandNoThumbnail() {
        product.setBrand(null);
        product.setThumbnailMediaId(null);
        product.setAttributeValues(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductEsDetailVm result = productService.getProductEsDetailById(1L);

        assertNull(result.brand());
        assertNull(result.thumbnailMediaId());
    }

    @Test
    void getProductEsDetailById_WhenNotFound_ShouldThrow() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductEsDetailById(999L));
    }

    // ========== getListFeaturedProducts ==========
    @Test
    void getListFeaturedProducts_ShouldReturnResults() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.getFeaturedProduct(any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductFeatureGetVm result = productService.getListFeaturedProducts(0, 10);

        assertEquals(1, result.productList().size());
    }

    // ========== getProductsByMultiQuery ==========
    @Test
    void getProductsByMultiQuery_ShouldReturnResults() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByProductNameAndCategorySlugAndPriceBetween(
            anyString(), anyString(), any(), any(), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductsGetVm result = productService.getProductsByMultiQuery(0, 10, "Test", "cat", 0.0, 200.0);

        assertEquals(1, result.productContent().size());
    }

    // ========== getProductVariationsByParentId ==========
    @Test
    void getProductVariationsByParentId_WhenNotFound_ShouldThrow() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductVariationsByParentId(999L));
    }

    @Test
    void getProductVariationsByParentId_WhenNoOptions_ReturnsEmpty() {
        product.setHasOptions(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(1L);

        assertTrue(result.isEmpty());
    }

    // ========== getRelatedProductsBackoffice ==========
    @Test
    void getRelatedProductsBackoffice_WhenNotFound_ShouldThrow() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getRelatedProductsBackoffice(999L));
    }

    @Test
    void getRelatedProductsBackoffice_WhenExists() {
        Product relatedProduct = Product.builder().id(2L).name("Related").slug("related")
            .isAllowedToOrder(true).isPublished(true).isFeatured(false)
            .isVisibleIndividually(true).price(50.0).taxClassId(1L).build();
        ProductRelated pr = ProductRelated.builder().product(product).relatedProduct(relatedProduct).build();
        product.setRelatedProducts(List.of(pr));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<ProductListVm> result = productService.getRelatedProductsBackoffice(1L);

        assertEquals(1, result.size());
        assertEquals("Related", result.get(0).name());
    }

    // ========== getRelatedProductsStorefront ==========
    @Test
    void getRelatedProductsStorefront_WhenNotFound_ShouldThrow() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getRelatedProductsStorefront(999L, 0, 10));
    }

    // ========== getProductsForWarehouse ==========
    @Test
    void getProductsForWarehouse_ShouldReturnResults() {
        when(productRepository.findProductForWarehouse("test", "sku", List.of(1L), "ALL"))
            .thenReturn(List.of(product));

        List<ProductInfoVm> result = productService.getProductsForWarehouse(
            "test", "sku", List.of(1L), FilterExistInWhSelection.ALL);

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
    }

    // ========== updateProductQuantity ==========
    @Test
    void updateProductQuantity_ShouldUpdateStock() {
        ProductQuantityPostVm qVm = new ProductQuantityPostVm(1L, 100L);
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));

        productService.updateProductQuantity(List.of(qVm));

        assertEquals(100L, product.getStockQuantity());
        verify(productRepository).saveAll(anyList());
    }

    // ========== subtractStockQuantity ==========
    @Test
    void subtractStockQuantity_ShouldDecrease() {
        product.setStockQuantity(50L);
        product.setStockTrackingEnabled(true);
        ProductQuantityPutVm qVm = new ProductQuantityPutVm(1L, 10L);
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));

        productService.subtractStockQuantity(List.of(qVm));

        assertEquals(40L, product.getStockQuantity());
    }

    @Test
    void subtractStockQuantity_ShouldNotGoBelowZero() {
        product.setStockQuantity(5L);
        product.setStockTrackingEnabled(true);
        ProductQuantityPutVm qVm = new ProductQuantityPutVm(1L, 10L);
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));

        productService.subtractStockQuantity(List.of(qVm));

        assertEquals(0L, product.getStockQuantity());
    }

    // ========== restoreStockQuantity ==========
    @Test
    void restoreStockQuantity_ShouldIncrease() {
        product.setStockQuantity(50L);
        product.setStockTrackingEnabled(true);
        ProductQuantityPutVm qVm = new ProductQuantityPutVm(1L, 10L);
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));

        productService.restoreStockQuantity(List.of(qVm));

        assertEquals(60L, product.getStockQuantity());
    }

    // ========== getProductByIds ==========
    @Test
    void getProductByIds_ShouldReturnList() {
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByIds(List.of(1L));

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
    }

    // ========== getProductByCategoryIds ==========
    @Test
    void getProductByCategoryIds_ShouldReturnList() {
        when(productRepository.findByCategoryIdsIn(List.of(1L))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByCategoryIds(List.of(1L));

        assertEquals(1, result.size());
    }

    // ========== getProductByBrandIds ==========
    @Test
    void getProductByBrandIds_ShouldReturnList() {
        when(productRepository.findByBrandIdsIn(List.of(1L))).thenReturn(List.of(product));

        List<ProductListVm> result = productService.getProductByBrandIds(List.of(1L));

        assertEquals(1, result.size());
    }

    // ========== exportProducts ==========
    @Test
    void exportProducts_ShouldReturnExportingDetails() {
        when(productRepository.getExportingProducts(anyString(), anyString())).thenReturn(List.of(product));

        List<ProductExportingDetailVm> result = productService.exportProducts("Test", "TestBrand");

        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).name());
    }

    // ========== getFeaturedProductsById ==========
    @Test
    void getFeaturedProductsById_WithThumbnail() {
        product.setParent(null);
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));

        assertEquals(1, result.size());
        assertEquals("http://url/file.jpg", result.get(0).thumbnailUrl());
    }

    // ========== getProductCheckoutList ==========
    @Test
    void getProductCheckoutList_ShouldReturnResults() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAllPublishedProductsByIds(anyList(), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 10, List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.productCheckoutListVms().size());
    }

    @Test
    void getProductCheckoutList_EmptyThumbnail() {
        NoFileMediaVm emptyMedia = new NoFileMediaVm(1L, "", "", "", "");
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAllPublishedProductsByIds(anyList(), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(emptyMedia);

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 10, List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.productCheckoutListVms().size());
    }

    // ========== getProductDetail (by slug) ==========
    @Test
    void getProductDetail_WhenSlugExists_ShouldReturnDetail() {
        product.setProductImages(List.of());
        product.setAttributeValues(List.of());
        when(productRepository.findBySlugAndIsPublishedTrue("test-product")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductDetailGetVm result = productService.getProductDetail("test-product");

        assertNotNull(result);
        assertEquals("Test Product", result.name());
    }

    @Test
    void getProductDetail_WhenSlugNotFound_ShouldThrow() {
        when(productRepository.findBySlugAndIsPublishedTrue("not-exist")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductDetail("not-exist"));
    }

    @Test
    void getProductDetail_NoBrand_ShouldReturnNullBrand() {
        product.setBrand(null);
        product.setProductImages(List.of());
        product.setAttributeValues(List.of());
        when(productRepository.findBySlugAndIsPublishedTrue("test-product")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductDetailGetVm result = productService.getProductDetail("test-product");

        assertNull(result.brandName());
    }

    // ========== deleteProduct - empty combinations ==========
    @Test
    void deleteProduct_WithParent_EmptyCombinations() {
        Product parent = Product.builder().id(2L).name("Parent").build();
        product.setParent(parent);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(product)).thenReturn(Collections.emptyList());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.deleteProduct(1L);

        verify(productOptionCombinationRepository, never()).deleteAll(anyList());
        assertFalse(product.isPublished());
    }

    // ========== subtractStockQuantity - tracking disabled ==========
    @Test
    void subtractStockQuantity_TrackingDisabled_ShouldNotChange() {
        product.setStockQuantity(50L);
        product.setStockTrackingEnabled(false);
        ProductQuantityPutVm qVm = new ProductQuantityPutVm(1L, 10L);
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));

        productService.subtractStockQuantity(List.of(qVm));

        assertEquals(50L, product.getStockQuantity());
    }

    // ========== restoreStockQuantity - tracking disabled ==========
    @Test
    void restoreStockQuantity_TrackingDisabled_ShouldNotChange() {
        product.setStockQuantity(50L);
        product.setStockTrackingEnabled(false);
        ProductQuantityPutVm qVm = new ProductQuantityPutVm(1L, 10L);
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));

        productService.restoreStockQuantity(List.of(qVm));

        assertEquals(50L, product.getStockQuantity());
    }

    // ========== getRelatedProductsStorefront - with data ==========
    @Test
    void getRelatedProductsStorefront_WhenExists_ShouldReturnPublishedOnly() {
        Product relatedPub = Product.builder().id(2L).name("Published Related").slug("pub-rel")
            .isPublished(true).thumbnailMediaId(2L).price(50.0).build();
        ProductRelated pr = ProductRelated.builder().product(product).relatedProduct(relatedPub).build();
        Page<ProductRelated> page = new PageImpl<>(List.of(pr));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRelatedRepository.findAllByProduct(eq(product), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(2L)).thenReturn(noFileMediaVm);

        ProductsGetVm result = productService.getRelatedProductsStorefront(1L, 0, 10);

        assertEquals(1, result.productContent().size());
    }

    // ========== getProductsByMultiQuery - empty ==========
    @Test
    void getProductsByMultiQuery_EmptyResult() {
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(productRepository.findByProductNameAndCategorySlugAndPriceBetween(
            anyString(), anyString(), any(), any(), any(Pageable.class))).thenReturn(page);

        ProductsGetVm result = productService.getProductsByMultiQuery(0, 10, "none", "", null, null);

        assertTrue(result.productContent().isEmpty());
    }

    // ========== getProductByIds - empty ==========
    @Test
    void getProductByIds_EmptyList_ShouldReturnEmpty() {
        when(productRepository.findAllByIdIn(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ProductListVm> result = productService.getProductByIds(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    // ========== setProductImages ==========
    @Test
    void setProductImages_NullImageIds_ShouldClearImages() {
        List<ProductImage> result = productService.setProductImages(null, product);
        assertTrue(result.isEmpty());
    }

    @Test
    void setProductImages_EmptyImageIds_ShouldClearImages() {
        List<ProductImage> result = productService.setProductImages(Collections.emptyList(), product);
        assertTrue(result.isEmpty());
    }

    @Test
    void setProductImages_NewProduct_NullExistingImages() {
        product.setProductImages(null);
        List<Long> imageIds = List.of(10L, 20L);

        List<ProductImage> result = productService.setProductImages(imageIds, product);

        assertEquals(2, result.size());
    }

    @Test
    void setProductImages_ExistingProduct_AddNew() {
        ProductImage existing = ProductImage.builder().imageId(10L).product(product).build();
        product.setProductImages(List.of(existing));
        List<Long> imageIds = List.of(10L, 20L);

        List<ProductImage> result = productService.setProductImages(imageIds, product);

        assertEquals(1, result.size()); // only new image 20L
    }

    // ========== getFeaturedProductsById - with parent fallback ==========
    @Test
    void getFeaturedProductsById_EmptyThumbnail_WithParent() {
        NoFileMediaVm emptyThumb = new NoFileMediaVm(1L, "", "", "", "");
        Product parent = Product.builder().id(2L).name("Parent").thumbnailMediaId(20L).build();
        product.setParent(parent);

        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
        when(mediaService.getMedia(1L)).thenReturn(emptyThumb);
        when(productRepository.findById(2L)).thenReturn(Optional.of(parent));
        when(mediaService.getMedia(20L)).thenReturn(noFileMediaVm);

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));

        assertEquals(1, result.size());
    }

    // ========== exportProducts - empty ==========
    @Test
    void exportProducts_EmptyResult() {
        when(productRepository.getExportingProducts(anyString(), anyString())).thenReturn(Collections.emptyList());

        List<ProductExportingDetailVm> result = productService.exportProducts("none", "none");

        assertTrue(result.isEmpty());
    }

    // ========== createProduct - no variations ==========
    @Test
    void createProduct_WithoutVariations_ShouldSucceed() {
        ProductPostVm postVm = new ProductPostVm(
            "New Product", "new-product", 1L, List.of(1L),
            "short", "desc", "spec", "SKU-NEW", "GTIN-NEW",
            1.0, DimensionUnit.CM, 10.0, 5.0, 3.0,
            99.0, true, true, false, true, false,
            "meta", "key", "metaDesc", 1L, List.of(10L),
            Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList(), List.of(), 1L
        );

        when(productRepository.findBySlugAndIsPublishedTrue("new-product")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-NEW")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("GTIN-NEW")).thenReturn(Optional.empty());
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(10L);
            p.setProductCategories(List.of());
            return p;
        });

        ProductGetDetailVm result = productService.createProduct(postVm);

        assertNotNull(result);
        verify(productRepository, atLeastOnce()).save(any(Product.class));
    }

    // ========== createProduct - validation: length < width ==========
    @Test
    void createProduct_LengthLessThanWidth_ShouldThrow() {
        ProductPostVm postVm = new ProductPostVm(
            "Prod", "prod", null, List.of(),
            "s", "d", "sp", "SKU", "GTIN",
            1.0, DimensionUnit.CM, 5.0, 10.0, 3.0,
            10.0, true, true, false, true, false,
            null, null, null, null, List.of(),
            Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList(), List.of(), null
        );

        assertThrows(BadRequestException.class, () -> productService.createProduct(postVm));
    }

    // ========== createProduct - duplicate slug ==========
    @Test
    void createProduct_DuplicateSlug_ShouldThrow() {
        ProductPostVm postVm = new ProductPostVm(
            "Dup", "test-product", null, List.of(),
            "s", "d", "sp", "SKU-UNIQUE", "",
            1.0, null, 10.0, 5.0, 3.0,
            10.0, true, true, false, true, false,
            null, null, null, null, List.of(),
            Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList(), List.of(), null
        );

        when(productRepository.findBySlugAndIsPublishedTrue("test-product")).thenReturn(Optional.of(product));

        assertThrows(DuplicatedException.class, () -> productService.createProduct(postVm));
    }

    // ========== createProduct - duplicate sku ==========
    @Test
    void createProduct_DuplicateSku_ShouldThrow() {
        ProductPostVm postVm = new ProductPostVm(
            "Dup", "unique-slug", null, List.of(),
            "s", "d", "sp", "SKU001", "",
            1.0, null, 10.0, 5.0, 3.0,
            10.0, true, true, false, true, false,
            null, null, null, null, List.of(),
            Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList(), List.of(), null
        );

        when(productRepository.findBySlugAndIsPublishedTrue("unique-slug")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU001")).thenReturn(Optional.of(product));

        assertThrows(DuplicatedException.class, () -> productService.createProduct(postVm));
    }

    // ========== createProduct - duplicate gtin ==========
    @Test
    void createProduct_DuplicateGtin_ShouldThrow() {
        ProductPostVm postVm = new ProductPostVm(
            "Dup", "unique-slug2", null, List.of(),
            "s", "d", "sp", "SKU-UNIQUE2", "GTIN001",
            1.0, null, 10.0, 5.0, 3.0,
            10.0, true, true, false, true, false,
            null, null, null, null, List.of(),
            Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList(), List.of(), null
        );

        when(productRepository.findBySlugAndIsPublishedTrue("unique-slug2")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("GTIN001")).thenReturn(Optional.of(product));

        assertThrows(DuplicatedException.class, () -> productService.createProduct(postVm));
    }

    // ========== getProductDetail with attributes (group branches) ==========
    @Test
    void getProductDetail_WithAttributeGroups_ShouldReturnGroupedAttributes() {
        ProductAttributeGroup group = new ProductAttributeGroup();
        group.setId(1L);
        group.setName("General");

        ProductAttribute attr = ProductAttribute.builder().id(1L).name("Color")
            .productAttributeGroup(group).build();
        ProductAttributeValue attrVal = new ProductAttributeValue();
        attrVal.setProductAttribute(attr);
        attrVal.setValue("Red");

        product.setProductImages(List.of());
        product.setAttributeValues(List.of(attrVal));

        when(productRepository.findBySlugAndIsPublishedTrue("test-product")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductDetailGetVm result = productService.getProductDetail("test-product");

        assertNotNull(result);
        assertFalse(result.productAttributeGroups().isEmpty());
        assertEquals("General", result.productAttributeGroups().get(0).name());
    }

    @Test
    void getProductDetail_WithNullAttributeGroup_ShouldReturnNoneGroup() {
        ProductAttribute attr = ProductAttribute.builder().id(1L).name("Weight")
            .productAttributeGroup(null).build();
        ProductAttributeValue attrVal = new ProductAttributeValue();
        attrVal.setProductAttribute(attr);
        attrVal.setValue("5kg");

        product.setProductImages(List.of());
        product.setAttributeValues(List.of(attrVal));

        when(productRepository.findBySlugAndIsPublishedTrue("test-product")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductDetailGetVm result = productService.getProductDetail("test-product");

        assertFalse(result.productAttributeGroups().isEmpty());
        assertEquals("None group", result.productAttributeGroups().get(0).name());
    }

    @Test
    void getProductDetail_WithProductImages_ShouldReturnImageUrls() {
        ProductImage img = ProductImage.builder().imageId(20L).product(product).build();
        product.setProductImages(List.of(img));
        product.setAttributeValues(List.of());

        when(productRepository.findBySlugAndIsPublishedTrue("test-product")).thenReturn(Optional.of(product));
        when(mediaService.getMedia(anyLong())).thenReturn(noFileMediaVm);

        ProductDetailGetVm result = productService.getProductDetail("test-product");

        assertEquals(1, result.productImageMediaUrls().size());
    }

    // ========== getProductVariationsByParentId with hasOptions=true ==========
    @Test
    void getProductVariationsByParentId_WithOptions_ShouldReturnVariations() {
        product.setHasOptions(true);

        Product variation = Product.builder()
            .id(2L).name("Var A").slug("var-a")
            .sku("V-SKU").gtin("V-GTIN").price(120.0)
            .isPublished(true).thumbnailMediaId(11L)
            .build();
        variation.setProductImages(new ArrayList<>());
        product.setProducts(List.of(variation));

        ProductOption opt = new ProductOption();
        opt.setId(1L);
        opt.setName("Size");

        ProductOptionCombination combo = ProductOptionCombination.builder()
            .id(1L).product(variation).productOption(opt).value("Large").build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(List.of(combo));
        when(mediaService.getMedia(11L)).thenReturn(noFileMediaVm);

        List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(1L);

        assertEquals(1, result.size());
        assertEquals("Var A", result.get(0).name());
        assertEquals(1, result.get(0).options().size());
    }

    @Test
    void getProductVariationsByParentId_VariationNoThumbnail() {
        product.setHasOptions(true);

        Product variation = Product.builder()
            .id(2L).name("Var B").slug("var-b")
            .sku("V-SKU2").price(80.0)
            .isPublished(true).thumbnailMediaId(null)
            .build();
        variation.setProductImages(new ArrayList<>());
        product.setProducts(List.of(variation));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(variation)).thenReturn(Collections.emptyList());

        List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(1L);

        assertEquals(1, result.size());
        assertNull(result.get(0).thumbnail());
    }

    @Test
    void getProductVariationsByParentId_FiltersUnpublished() {
        product.setHasOptions(true);

        Product pubVar = Product.builder().id(2L).name("Pub").slug("pub")
            .isPublished(true).thumbnailMediaId(null).build();
        pubVar.setProductImages(new ArrayList<>());
        Product unpubVar = Product.builder().id(3L).name("Unpub").slug("unpub")
            .isPublished(false).build();
        unpubVar.setProductImages(new ArrayList<>());
        product.setProducts(List.of(pubVar, unpubVar));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productOptionCombinationRepository.findAllByProduct(pubVar)).thenReturn(Collections.emptyList());

        List<ProductVariationGetVm> result = productService.getProductVariationsByParentId(1L);

        assertEquals(1, result.size());
        assertEquals("Pub", result.get(0).name());
    }

    // ========== getProductById - with parent ==========
    @Test
    void getProductById_WithParent_ShouldReturnParentId() {
        Product parent = Product.builder().id(2L).slug("parent").build();
        product.setParent(parent);
        product.setProductImages(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductDetailVm result = productService.getProductById(1L);

        assertEquals(2L, result.parentId());
    }

    @Test
    void getProductById_WithNullCategories_ShouldReturnEmptyCategories() {
        product.setProductCategories(null);
        product.setProductImages(List.of());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductDetailVm result = productService.getProductById(1L);

        assertTrue(result.categories().isEmpty());
    }

    @Test
    void getProductById_WithNullProductImages_ShouldReturnEmptyImages() {
        product.setProductImages(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(mediaService.getMedia(1L)).thenReturn(noFileMediaVm);

        ProductDetailVm result = productService.getProductById(1L);

        assertTrue(result.productImageMedias().isEmpty());
    }

    // ========== setProductImages - delete old images ==========
    @Test
    void setProductImages_ShouldDeleteOldAndAddNew() {
        ProductImage existing1 = ProductImage.builder().imageId(10L).product(product).build();
        ProductImage existing2 = ProductImage.builder().imageId(20L).product(product).build();
        product.setProductImages(List.of(existing1, existing2));

        List<Long> newImageIds = List.of(20L, 30L); // keep 20, add 30, delete 10

        List<ProductImage> result = productService.setProductImages(newImageIds, product);

        // only new image 30L should be returned
        assertEquals(1, result.size());
        verify(productImageRepository).deleteByImageIdInAndProductId(List.of(10L), product.getId());
    }

    // ========== getProductCheckoutList - thumbnailUrl empty ==========
    @Test
    void getProductCheckoutList_EmptyThumbnailUrl_ShouldReturnDefault() {
        NoFileMediaVm emptyMedia = new NoFileMediaVm(1L, "", "", "", "");
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAllPublishedProductsByIds(anyList(), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(1L)).thenReturn(emptyMedia);

        ProductGetCheckoutListVm result = productService.getProductCheckoutList(0, 10, List.of(1L));

        assertNotNull(result);
        assertEquals(1, result.productCheckoutListVms().size());
        // when thumbnail URL is empty, the original ProductCheckoutListVm is returned without modification
        assertEquals("", result.productCheckoutListVms().get(0).thumbnailUrl());
    }

    // ========== getRelatedProductsStorefront - filter unpublished ==========
    @Test
    void getRelatedProductsStorefront_FiltersUnpublished() {
        Product pubProduct = Product.builder().id(2L).name("Pub").slug("pub")
            .isPublished(true).thumbnailMediaId(2L).price(20.0).build();
        Product unpubProduct = Product.builder().id(3L).name("Unpub").slug("unpub")
            .isPublished(false).thumbnailMediaId(3L).price(30.0).build();

        ProductRelated pr1 = ProductRelated.builder().product(product).relatedProduct(pubProduct).build();
        ProductRelated pr2 = ProductRelated.builder().product(product).relatedProduct(unpubProduct).build();
        Page<ProductRelated> page = new PageImpl<>(List.of(pr1, pr2));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRelatedRepository.findAllByProduct(eq(product), any(Pageable.class))).thenReturn(page);
        when(mediaService.getMedia(2L)).thenReturn(noFileMediaVm);

        ProductsGetVm result = productService.getRelatedProductsStorefront(1L, 0, 10);

        assertEquals(1, result.productContent().size());
        assertEquals("Pub", result.productContent().get(0).name());
    }

    // ========== getRelatedProductsBackoffice - with parent in related ==========
    @Test
    void getRelatedProductsBackoffice_RelatedProductHasParent() {
        Product parent = Product.builder().id(10L).name("Parent").build();
        Product relatedProduct = Product.builder().id(2L).name("Related").slug("related")
            .isAllowedToOrder(true).isPublished(true).isFeatured(false)
            .isVisibleIndividually(true).price(50.0).taxClassId(1L)
            .parent(parent).build();
        ProductRelated pr = ProductRelated.builder().product(product).relatedProduct(relatedProduct).build();
        product.setRelatedProducts(List.of(pr));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<ProductListVm> result = productService.getRelatedProductsBackoffice(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).parentId());
    }

    // ========== getFeaturedProductsById - no parent, no thumbnail ==========
    @Test
    void getFeaturedProductsById_NoParent_NoThumbnail() {
        product.setParent(null);
        NoFileMediaVm emptyThumb = new NoFileMediaVm(1L, "", "", "", "url-ok");
        when(productRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(product));
        when(mediaService.getMedia(1L)).thenReturn(emptyThumb);

        List<ProductThumbnailGetVm> result = productService.getFeaturedProductsById(List.of(1L));

        assertEquals(1, result.size());
        assertEquals("url-ok", result.get(0).thumbnailUrl());
    }

    // ========== updateProductQuantity - multiple products ==========
    @Test
    void updateProductQuantity_MultipleProducts() {
        Product product2 = Product.builder().id(2L).name("Prod2").stockQuantity(10L).build();
        ProductQuantityPostVm q1 = new ProductQuantityPostVm(1L, 200L);
        ProductQuantityPostVm q2 = new ProductQuantityPostVm(2L, 300L);
        when(productRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(product, product2));

        productService.updateProductQuantity(List.of(q1, q2));

        assertEquals(200L, product.getStockQuantity());
        assertEquals(300L, product2.getStockQuantity());
    }

    // ========== subtractStockQuantity - multiple items same product (merge) ==========
    @Test
    void subtractStockQuantity_MultipleItemsSameProduct_ShouldMerge() {
        product.setStockQuantity(100L);
        product.setStockTrackingEnabled(true);
        ProductQuantityPutVm q1 = new ProductQuantityPutVm(1L, 10L);
        ProductQuantityPutVm q2 = new ProductQuantityPutVm(1L, 20L);
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));

        productService.subtractStockQuantity(List.of(q1, q2));

        assertEquals(70L, product.getStockQuantity());
    }

    // ========== restoreStockQuantity - multiple items same product ==========
    @Test
    void restoreStockQuantity_MultipleItemsSameProduct_ShouldMerge() {
        product.setStockQuantity(50L);
        product.setStockTrackingEnabled(true);
        ProductQuantityPutVm q1 = new ProductQuantityPutVm(1L, 10L);
        ProductQuantityPutVm q2 = new ProductQuantityPutVm(1L, 20L);
        when(productRepository.findAllByIdIn(anyList())).thenReturn(List.of(product));

        productService.restoreStockQuantity(List.of(q1, q2));

        assertEquals(80L, product.getStockQuantity());
    }

    // ========== getProductsFromCategory - empty products ==========
    @Test
    void getProductsFromCategory_EmptyPage() {
        Page<ProductCategory> page = new PageImpl<>(Collections.emptyList());
        when(categoryRepository.findBySlug("test-category")).thenReturn(Optional.of(category));
        when(productCategoryRepository.findAllByCategory(any(Pageable.class), eq(category))).thenReturn(page);

        ProductListGetFromCategoryVm result = productService.getProductsFromCategory(0, 10, "test-category");

        assertTrue(result.productContent().isEmpty());
    }

    // ========== getProductsByBrand - empty products ==========
    @Test
    void getProductsByBrand_EmptyResult() {
        when(brandRepository.findBySlug("test-brand")).thenReturn(Optional.of(brand));
        when(productRepository.findAllByBrandAndIsPublishedTrueOrderByIdAsc(brand)).thenReturn(Collections.emptyList());

        List<ProductThumbnailVm> result = productService.getProductsByBrand("test-brand");

        assertTrue(result.isEmpty());
    }

    // ========== getListFeaturedProducts - empty ==========
    @Test
    void getListFeaturedProducts_EmptyResult() {
        Page<Product> page = new PageImpl<>(Collections.emptyList());
        when(productRepository.getFeaturedProduct(any(Pageable.class))).thenReturn(page);

        ProductFeatureGetVm result = productService.getListFeaturedProducts(0, 10);

        assertTrue(result.productList().isEmpty());
    }

    // ========== getProductsForWarehouse - empty ==========
    @Test
    void getProductsForWarehouse_EmptyResult() {
        when(productRepository.findProductForWarehouse("test", "sku", List.of(), "ALL"))
            .thenReturn(Collections.emptyList());

        List<ProductInfoVm> result = productService.getProductsForWarehouse(
            "test", "sku", List.of(), FilterExistInWhSelection.ALL);

        assertTrue(result.isEmpty());
    }

    // ========== updateProduct - duplicate variations ==========
    @Test
    void updateProduct_WithDuplicateVariationSku_ShouldThrow() {
        ProductVariationPutVm var1 = new ProductVariationPutVm(
            null, "Var1", "var1", "DUPE-SKU", "GTIN1", 50.0, null, List.of(), Map.of()
        );
        ProductVariationPutVm var2 = new ProductVariationPutVm(
            null, "Var2", "var2", "DUPE-SKU", "GTIN2", 50.0, null, List.of(), Map.of()
        );

        ProductPutVm putVm = new ProductPutVm(
            "Updated", "updated", 99.0, true, true, false, true, false,
            null, List.of(), "s", "d", "sp", "SKU-MAIN", "GTIN-MAIN",
            1.0, DimensionUnit.CM, 10.0, 5.0, 3.0,
            null, null, null, null, List.of(),
            List.of(var1, var2), List.of(), Collections.emptyList(),
            List.of(), null
        );

        product.setProducts(new ArrayList<>());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySlugAndIsPublishedTrue("updated")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("GTIN-MAIN")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-MAIN")).thenReturn(Optional.empty());

        assertThrows(DuplicatedException.class, () -> productService.updateProduct(1L, putVm));
    }

    @Test
    void updateProduct_WithDuplicateVariationSlug_ShouldThrow() {
        ProductVariationPutVm var1 = new ProductVariationPutVm(
            null, "Var1", "DUPE-SLUG", "SKU1", "GTIN1", 50.0, null, List.of(), Map.of()
        );
        ProductVariationPutVm var2 = new ProductVariationPutVm(
            null, "Var2", "DUPE-SLUG", "SKU2", "GTIN2", 50.0, null, List.of(), Map.of()
        );

        ProductPutVm putVm = new ProductPutVm(
            "Updated", "updated", 99.0, true, true, false, true, false,
            null, List.of(), "s", "d", "sp", "SKU-MAIN", "GTIN-MAIN",
            1.0, DimensionUnit.CM, 10.0, 5.0, 3.0,
            null, null, null, null, List.of(),
            List.of(var1, var2), List.of(), Collections.emptyList(),
            List.of(), null
        );

        product.setProducts(new ArrayList<>());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySlugAndIsPublishedTrue("updated")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("GTIN-MAIN")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-MAIN")).thenReturn(Optional.empty());

        assertThrows(DuplicatedException.class, () -> productService.updateProduct(1L, putVm));
    }

    // ========== updateProduct - not found ==========
    @Test
    void updateProduct_NotFound_ShouldThrow() {
        ProductPutVm putVm = new ProductPutVm(
            "Updated", "updated", 99.0, true, true, false, true, false,
            null, List.of(), "s", "d", "sp", "SKU", "GTIN",
            1.0, DimensionUnit.CM, 10.0, 5.0, 3.0,
            null, null, null, null, List.of(),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(), null
        );

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.updateProduct(999L, putVm));
    }

    // ========== updateProduct - success without variations ==========
    @Test
    void updateProduct_SuccessWithoutVariations() {
        ProductPutVm putVm = new ProductPutVm(
            "Updated", "updated", 99.0, true, true, false, true, false,
            1L, List.of(1L), "s", "d", "sp", "SKU-UP", "GTIN-UP",
            1.0, DimensionUnit.CM, 10.0, 5.0, 3.0,
            null, null, null, null, List.of(10L, 20L),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
            List.of(2L), null
        );

        product.setProducts(new ArrayList<>());
        product.setProductImages(new ArrayList<>());
        product.setProductCategories(new ArrayList<>());
        product.setRelatedProducts(new ArrayList<>());

        Product relatedProduct = Product.builder().id(2L).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySlugAndIsPublishedTrue("updated")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("GTIN-UP")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-UP")).thenReturn(Optional.empty());
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category));
        when(productRepository.findAllById(List.of(2L))).thenReturn(List.of(relatedProduct));

        productService.updateProduct(1L, putVm);

        verify(productRepository).save(product);
        assertEquals("Updated", product.getName());
    }

    // ========== updateProduct - with new variation and missing option value ==========
    @Test
    void updateProduct_WithVariation_MissingOptionValue_ShouldThrow() {
        ProductOptionValuePutVm optValVm = new ProductOptionValuePutVm(1L, "Text", 1, List.of("Red"));
        ProductVariationPutVm varVm = new ProductVariationPutVm(
            null, "Var1", "var1", "V-SKU", "V-GTIN", 50.0, 11L, List.of(), Map.of(1L, "Blue")
        );

        ProductPutVm putVm = new ProductPutVm(
            "Updated", "updated", 99.0, true, true, false, true, false,
            null, List.of(), "s", "d", "sp", "SKU-UP2", "GTIN-UP2",
            1.0, DimensionUnit.CM, 10.0, 5.0, 3.0,
            null, null, null, null, List.of(),
            List.of(varVm), List.of(optValVm), Collections.emptyList(),
            List.of(), null
        );

        product.setProducts(new ArrayList<>());
        product.setProductImages(new ArrayList<>());
        product.setProductCategories(new ArrayList<>());
        product.setRelatedProducts(new ArrayList<>());

        ProductOption opt = new ProductOption();
        opt.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findBySlugAndIsPublishedTrue("updated")).thenReturn(Optional.empty());
        when(productRepository.findByGtinAndIsPublishedTrue("GTIN-UP2")).thenReturn(Optional.empty());
        when(productRepository.findBySkuAndIsPublishedTrue("SKU-UP2")).thenReturn(Optional.empty());

        when(productOptionRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(opt));
        
        // Return saved variation
        Product savedVar = Product.builder().id(3L).slug("var1").build();
        when(productRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Product> list = inv.getArgument(0);
            if (!list.isEmpty() && "var1".equals(list.get(0).getSlug())) {
                return List.of(savedVar);
            }
            return list;
        });

        // This will throw because the option value "Blue" is mapped but productOptionValueDisplays doesn't have it
        assertThrows(BadRequestException.class, () -> productService.updateProduct(1L, putVm));
    }
}

