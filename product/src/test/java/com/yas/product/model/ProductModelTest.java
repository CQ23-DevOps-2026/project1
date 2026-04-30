package com.yas.product.model;

import static org.junit.jupiter.api.Assertions.*;

import com.yas.product.model.attribute.ProductAttribute;
import com.yas.product.model.attribute.ProductAttributeGroup;
import com.yas.product.model.attribute.ProductAttributeValue;
import com.yas.product.model.enumeration.DimensionUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductModelTest {

    // ========== Product equals/hashCode ==========
    @Test
    void product_equals_sameInstance() {
        Product p = Product.builder().id(1L).build();
        assertEquals(p, p);
    }

    @Test
    void product_equals_sameId() {
        Product p1 = Product.builder().id(1L).name("A").build();
        Product p2 = Product.builder().id(1L).name("B").build();
        assertEquals(p1, p2);
    }

    @Test
    void product_equals_differentId() {
        Product p1 = Product.builder().id(1L).build();
        Product p2 = Product.builder().id(2L).build();
        assertNotEquals(p1, p2);
    }

    @Test
    void product_equals_nullId() {
        Product p1 = Product.builder().build();
        Product p2 = Product.builder().id(1L).build();
        assertNotEquals(p1, p2);
    }

    @Test
    void product_equals_notProduct() {
        Product p = Product.builder().id(1L).build();
        assertNotEquals(p, "not a product");
    }

    @Test
    void product_hashCode_consistent() {
        Product p1 = Product.builder().id(1L).build();
        Product p2 = Product.builder().id(2L).build();
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void product_builder_allFields() {
        Product p = Product.builder()
            .id(1L).name("Test").slug("test").sku("SKU1").gtin("GTIN1")
            .price(10.0).hasOptions(true).isAllowedToOrder(true)
            .isPublished(true).isFeatured(false).isVisibleIndividually(true)
            .stockTrackingEnabled(true).stockQuantity(100L)
            .taxClassId(1L).thumbnailMediaId(5L)
            .shortDescription("short").description("desc")
            .specification("spec").metaTitle("title")
            .metaKeyword("key").metaDescription("meta")
            .weight(1.5).dimensionUnit(DimensionUnit.CM)
            .length(10.0).width(5.0).height(3.0)
            .build();

        assertEquals("Test", p.getName());
        assertEquals("SKU1", p.getSku());
        assertEquals(DimensionUnit.CM, p.getDimensionUnit());
        assertTrue(p.isHasOptions());
        assertEquals(100L, p.getStockQuantity());
    }

    @Test
    void product_setters() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Test");
        p.setSlug("test");
        p.setPrice(50.0);
        p.setPublished(true);
        p.setFeatured(true);
        p.setAllowedToOrder(true);
        p.setVisibleIndividually(true);
        p.setStockTrackingEnabled(true);
        p.setStockQuantity(10L);

        assertEquals(1L, p.getId());
        assertEquals("Test", p.getName());
        assertTrue(p.isPublished());
        assertTrue(p.isFeatured());
        assertEquals(10L, p.getStockQuantity());
    }

    @Test
    void product_parentRelationship() {
        Product parent = Product.builder().id(1L).name("Parent").build();
        Product child = Product.builder().id(2L).name("Child").parent(parent).build();

        assertEquals(parent, child.getParent());
        assertNull(parent.getParent());
    }

    @Test
    void product_brandRelationship() {
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("BrandX");
        Product p = Product.builder().id(1L).brand(brand).build();

        assertEquals("BrandX", p.getBrand().getName());
    }

    @Test
    void product_categoriesRelationship() {
        Product p = Product.builder().id(1L).build();
        Category cat = new Category();
        cat.setId(1L);
        ProductCategory pc = ProductCategory.builder().product(p).category(cat).build();
        p.setProductCategories(List.of(pc));

        assertEquals(1, p.getProductCategories().size());
    }

    @Test
    void product_imagesRelationship() {
        Product p = Product.builder().id(1L).build();
        ProductImage img = ProductImage.builder().id(1L).imageId(10L).product(p).build();
        p.setProductImages(List.of(img));

        assertEquals(1, p.getProductImages().size());
        assertEquals(10L, p.getProductImages().get(0).getImageId());
    }

    @Test
    void product_relatedProducts() {
        Product p1 = Product.builder().id(1L).build();
        Product p2 = Product.builder().id(2L).build();
        ProductRelated pr = ProductRelated.builder().product(p1).relatedProduct(p2).build();
        p1.setRelatedProducts(List.of(pr));

        assertEquals(1, p1.getRelatedProducts().size());
        assertEquals(2L, p1.getRelatedProducts().get(0).getRelatedProduct().getId());
    }

    @Test
    void product_defaultCollections() {
        Product p = Product.builder().build();
        assertNotNull(p.getProductCategories());
        assertNotNull(p.getProductImages());
        assertNotNull(p.getRelatedProducts());
        assertNotNull(p.getProducts());
        assertNotNull(p.getAttributeValues());
    }

    // ========== Brand equals/hashCode ==========
    @Test
    void brand_equals_sameId() {
        Brand b1 = new Brand(); b1.setId(1L);
        Brand b2 = new Brand(); b2.setId(1L);
        assertEquals(b1, b2);
    }

    @Test
    void brand_equals_differentId() {
        Brand b1 = new Brand(); b1.setId(1L);
        Brand b2 = new Brand(); b2.setId(2L);
        assertNotEquals(b1, b2);
    }

    @Test
    void brand_equals_sameInstance() {
        Brand b = new Brand(); b.setId(1L);
        assertEquals(b, b);
    }

    @Test
    void brand_equals_notBrand() {
        Brand b = new Brand(); b.setId(1L);
        assertNotEquals(b, "string");
    }

    @Test
    void brand_equals_nullId() {
        Brand b1 = new Brand();
        Brand b2 = new Brand(); b2.setId(1L);
        assertNotEquals(b1, b2);
    }

    @Test
    void brand_hashCode_consistent() {
        Brand b1 = new Brand(); b1.setId(1L);
        Brand b2 = new Brand(); b2.setId(2L);
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    @Test
    void brand_setters() {
        Brand b = new Brand();
        b.setId(1L);
        b.setName("Nike");
        b.setSlug("nike");
        b.setPublished(true);
        assertEquals("Nike", b.getName());
        assertEquals("nike", b.getSlug());
        assertTrue(b.isPublished());
    }

    // ========== Category equals/hashCode ==========
    @Test
    void category_equals_sameId() {
        Category c1 = new Category(); c1.setId(1L);
        Category c2 = new Category(); c2.setId(1L);
        assertEquals(c1, c2);
    }

    @Test
    void category_equals_differentId() {
        Category c1 = new Category(); c1.setId(1L);
        Category c2 = new Category(); c2.setId(2L);
        assertNotEquals(c1, c2);
    }

    @Test
    void category_equals_sameInstance() {
        Category c = new Category(); c.setId(1L);
        assertEquals(c, c);
    }

    @Test
    void category_equals_notCategory() {
        Category c = new Category(); c.setId(1L);
        assertNotEquals(c, "string");
    }

    @Test
    void category_equals_nullId() {
        Category c = new Category();
        Category c2 = new Category(); c2.setId(1L);
        assertNotEquals(c, c2);
    }

    @Test
    void category_setters() {
        Category c = new Category();
        c.setId(1L);
        c.setName("Electronics");
        c.setSlug("electronics");
        c.setDescription("desc");
        c.setDisplayOrder((short) 1);
        c.setIsPublished(true);
        c.setImageId(10L);
        assertEquals("Electronics", c.getName());
        assertEquals(10L, c.getImageId());
    }

    @Test
    void category_parentRelationship() {
        Category parent = new Category(); parent.setId(1L);
        Category child = new Category(); child.setId(2L);
        child.setParent(parent);
        assertEquals(parent, child.getParent());
    }

    // ========== ProductRelated equals/hashCode ==========
    @Test
    void productRelated_equals_sameId() {
        ProductRelated pr1 = ProductRelated.builder().id(1L).build();
        ProductRelated pr2 = ProductRelated.builder().id(1L).build();
        assertEquals(pr1, pr2);
    }

    @Test
    void productRelated_equals_differentId() {
        ProductRelated pr1 = ProductRelated.builder().id(1L).build();
        ProductRelated pr2 = ProductRelated.builder().id(2L).build();
        assertNotEquals(pr1, pr2);
    }

    @Test
    void productRelated_equals_sameInstance() {
        ProductRelated pr = ProductRelated.builder().id(1L).build();
        assertEquals(pr, pr);
    }

    @Test
    void productRelated_equals_notProductRelated() {
        ProductRelated pr = ProductRelated.builder().id(1L).build();
        assertNotEquals(pr, "string");
    }

    @Test
    void productRelated_equals_nullId() {
        ProductRelated pr1 = ProductRelated.builder().build();
        ProductRelated pr2 = ProductRelated.builder().id(1L).build();
        assertNotEquals(pr1, pr2);
    }

    // ========== ProductOption equals/hashCode ==========
    @Test
    void productOption_equals_sameId() {
        ProductOption po1 = new ProductOption(); po1.setId(1L);
        ProductOption po2 = new ProductOption(); po2.setId(1L);
        assertEquals(po1, po2);
    }

    @Test
    void productOption_equals_differentId() {
        ProductOption po1 = new ProductOption(); po1.setId(1L);
        ProductOption po2 = new ProductOption(); po2.setId(2L);
        assertNotEquals(po1, po2);
    }

    @Test
    void productOption_equals_sameInstance() {
        ProductOption po = new ProductOption(); po.setId(1L);
        assertEquals(po, po);
    }

    @Test
    void productOption_equals_notProductOption() {
        ProductOption po = new ProductOption(); po.setId(1L);
        assertNotEquals(po, "string");
    }

    @Test
    void productOption_equals_nullId() {
        ProductOption po1 = new ProductOption();
        ProductOption po2 = new ProductOption(); po2.setId(1L);
        assertNotEquals(po1, po2);
    }

    // ========== ProductOptionCombination equals/hashCode ==========
    @Test
    void productOptionCombination_equals_sameId() {
        ProductOptionCombination c1 = ProductOptionCombination.builder().id(1L).build();
        ProductOptionCombination c2 = ProductOptionCombination.builder().id(1L).build();
        assertEquals(c1, c2);
    }

    @Test
    void productOptionCombination_equals_differentId() {
        ProductOptionCombination c1 = ProductOptionCombination.builder().id(1L).build();
        ProductOptionCombination c2 = ProductOptionCombination.builder().id(2L).build();
        assertNotEquals(c1, c2);
    }

    @Test
    void productOptionCombination_equals_sameInstance() {
        ProductOptionCombination c = ProductOptionCombination.builder().id(1L).build();
        assertEquals(c, c);
    }

    @Test
    void productOptionCombination_equals_notSameType() {
        ProductOptionCombination c = ProductOptionCombination.builder().id(1L).build();
        assertNotEquals(c, "string");
    }

    @Test
    void productOptionCombination_equals_nullId() {
        ProductOptionCombination c1 = ProductOptionCombination.builder().build();
        ProductOptionCombination c2 = ProductOptionCombination.builder().id(1L).build();
        assertNotEquals(c1, c2);
    }

    // ========== ProductOptionValue equals/hashCode ==========
    @Test
    void productOptionValue_equals_sameId() {
        ProductOptionValue v1 = ProductOptionValue.builder().id(1L).build();
        ProductOptionValue v2 = ProductOptionValue.builder().id(1L).build();
        assertEquals(v1, v2);
    }

    @Test
    void productOptionValue_equals_differentId() {
        ProductOptionValue v1 = ProductOptionValue.builder().id(1L).build();
        ProductOptionValue v2 = ProductOptionValue.builder().id(2L).build();
        assertNotEquals(v1, v2);
    }

    @Test
    void productOptionValue_equals_sameInstance() {
        ProductOptionValue v = ProductOptionValue.builder().id(1L).build();
        assertEquals(v, v);
    }

    @Test
    void productOptionValue_equals_notSameType() {
        ProductOptionValue v = ProductOptionValue.builder().id(1L).build();
        assertNotEquals(v, "string");
    }

    @Test
    void productOptionValue_equals_nullId() {
        ProductOptionValue v1 = ProductOptionValue.builder().build();
        ProductOptionValue v2 = ProductOptionValue.builder().id(1L).build();
        assertNotEquals(v1, v2);
    }

    @Test
    void productOptionValue_setters() {
        ProductOptionValue v = new ProductOptionValue();
        v.setId(1L);
        v.setValue("Red");
        v.setDisplayType("color");
        v.setDisplayOrder(1);
        assertEquals("Red", v.getValue());
        assertEquals("color", v.getDisplayType());
        assertEquals(1, v.getDisplayOrder());
    }

    // ========== ProductAttribute equals/hashCode ==========
    @Test
    void productAttribute_equals_sameId() {
        ProductAttribute a1 = ProductAttribute.builder().id(1L).build();
        ProductAttribute a2 = ProductAttribute.builder().id(1L).build();
        assertEquals(a1, a2);
    }

    @Test
    void productAttribute_equals_differentId() {
        ProductAttribute a1 = ProductAttribute.builder().id(1L).build();
        ProductAttribute a2 = ProductAttribute.builder().id(2L).build();
        assertNotEquals(a1, a2);
    }

    @Test
    void productAttribute_equals_sameInstance() {
        ProductAttribute a = ProductAttribute.builder().id(1L).build();
        assertEquals(a, a);
    }

    @Test
    void productAttribute_equals_notSameType() {
        ProductAttribute a = ProductAttribute.builder().id(1L).build();
        assertNotEquals(a, "string");
    }

    @Test
    void productAttribute_equals_nullId() {
        ProductAttribute a1 = ProductAttribute.builder().build();
        ProductAttribute a2 = ProductAttribute.builder().id(1L).build();
        assertNotEquals(a1, a2);
    }

    // ========== ProductAttributeGroup equals/hashCode ==========
    @Test
    void productAttributeGroup_equals_sameId() {
        ProductAttributeGroup g1 = new ProductAttributeGroup(); g1.setId(1L);
        ProductAttributeGroup g2 = new ProductAttributeGroup(); g2.setId(1L);
        assertEquals(g1, g2);
    }

    @Test
    void productAttributeGroup_equals_differentId() {
        ProductAttributeGroup g1 = new ProductAttributeGroup(); g1.setId(1L);
        ProductAttributeGroup g2 = new ProductAttributeGroup(); g2.setId(2L);
        assertNotEquals(g1, g2);
    }

    @Test
    void productAttributeGroup_equals_sameInstance() {
        ProductAttributeGroup g = new ProductAttributeGroup(); g.setId(1L);
        assertEquals(g, g);
    }

    @Test
    void productAttributeGroup_equals_notSameType() {
        ProductAttributeGroup g = new ProductAttributeGroup(); g.setId(1L);
        assertNotEquals(g, "string");
    }

    @Test
    void productAttributeGroup_equals_nullId() {
        ProductAttributeGroup g1 = new ProductAttributeGroup();
        ProductAttributeGroup g2 = new ProductAttributeGroup(); g2.setId(1L);
        assertNotEquals(g1, g2);
    }

    // ========== ProductImage ==========
    @Test
    void productImage_builder() {
        Product p = Product.builder().id(1L).build();
        ProductImage img = ProductImage.builder().id(1L).imageId(10L).product(p).build();
        assertEquals(1L, img.getId());
        assertEquals(10L, img.getImageId());
        assertEquals(p, img.getProduct());
    }

    // ========== ProductCategory ==========
    @Test
    void productCategory_builder() {
        Product p = Product.builder().id(1L).build();
        Category c = new Category(); c.setId(1L);
        ProductCategory pc = ProductCategory.builder()
            .id(1L).product(p).category(c).displayOrder(1).isFeaturedProduct(true).build();
        assertEquals(1L, pc.getId());
        assertEquals(p, pc.getProduct());
        assertEquals(c, pc.getCategory());
        assertEquals(1, pc.getDisplayOrder());
        assertTrue(pc.isFeaturedProduct());
    }

    // ========== ProductOptionCombination setters ==========
    @Test
    void productOptionCombination_setters() {
        ProductOptionCombination c = new ProductOptionCombination();
        c.setId(1L);
        c.setValue("Large");
        c.setDisplayOrder(2);
        assertEquals("Large", c.getValue());
        assertEquals(2, c.getDisplayOrder());
    }

    // ========== DimensionUnit enum ==========
    @Test
    void dimensionUnit_values() {
        assertEquals("cm", DimensionUnit.CM.getName());
        assertEquals("inch", DimensionUnit.INCH.getName());
        assertEquals(2, DimensionUnit.values().length);
    }

    // ========== FilterExistInWhSelection enum ==========
    @Test
    void filterExistInWhSelection_values() {
        assertEquals(3, com.yas.product.model.enumeration.FilterExistInWhSelection.values().length);
        assertEquals("ALL", com.yas.product.model.enumeration.FilterExistInWhSelection.ALL.name());
        assertEquals("YES", com.yas.product.model.enumeration.FilterExistInWhSelection.YES.name());
        assertEquals("NO", com.yas.product.model.enumeration.FilterExistInWhSelection.NO.name());
    }

    // ========== ProductAttributeValue ==========
    @Test
    void productAttributeValue_setters() {
        ProductAttributeValue v = new ProductAttributeValue();
        v.setId(1L);
        v.setValue("Blue");
        Product p = Product.builder().id(1L).build();
        v.setProduct(p);
        ProductAttribute attr = ProductAttribute.builder().id(1L).name("Color").build();
        v.setProductAttribute(attr);
        assertEquals("Blue", v.getValue());
        assertEquals(p, v.getProduct());
        assertEquals("Color", v.getProductAttribute().getName());
    }
}
