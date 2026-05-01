package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.yas.commonlibrary.exception.BadRequestException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.product.model.Category;
import com.yas.product.repository.CategoryRepository;
import com.yas.product.viewmodel.NoFileMediaVm;
import com.yas.product.viewmodel.category.CategoryGetDetailVm;
import com.yas.product.viewmodel.category.CategoryGetVm;
import com.yas.product.viewmodel.category.CategoryListGetVm;
import com.yas.product.viewmodel.category.CategoryPostVm;
import java.util.Collections;
import java.util.List;
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
class CategoryServiceUnitTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private MediaService mediaService;

    @InjectMocks private CategoryService categoryService;

    private Category category;
    private NoFileMediaVm noFileMediaVm;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setDescription("Electronic items");
        category.setMetaKeyword("electronics");
        category.setMetaDescription("Meta desc");
        category.setDisplayOrder((short) 1);
        category.setIsPublished(true);
        category.setImageId(10L);

        noFileMediaVm = new NoFileMediaVm(10L, "cap", "img.jpg", "image/jpeg", "http://img.url");
    }

    // ========== getPageableCategories ==========
    @Test
    void getPageableCategories_ShouldReturnList() {
        Page<Category> page = new PageImpl<>(List.of(category));
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);

        CategoryListGetVm result = categoryService.getPageableCategories(0, 10);

        assertEquals(1, result.categoryContent().size());
        assertEquals(0, result.pageNo());
    }

    @Test
    void getPageableCategories_EmptyResult() {
        Page<Category> page = new PageImpl<>(Collections.emptyList());
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);

        CategoryListGetVm result = categoryService.getPageableCategories(0, 10);

        assertTrue(result.categoryContent().isEmpty());
    }

    // ========== create ==========
    @Test
    void create_Success_WithoutParent() {
        CategoryPostVm vm = new CategoryPostVm("NewCat", "new-cat", "desc", null,
            "meta", "metaDesc", (short) 1, true, 1L);
        when(categoryRepository.findExistedName("NewCat", null)).thenReturn(null);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.create(vm);

        assertEquals("NewCat", result.getName());
        assertNull(result.getParent());
    }

    @Test
    void create_Success_WithParent() {
        Category parent = new Category();
        parent.setId(2L);
        parent.setName("Parent");
        CategoryPostVm vm = new CategoryPostVm("Child", "child", "desc", 2L,
            "meta", "metaDesc", (short) 1, true, null);
        when(categoryRepository.findExistedName("Child", null)).thenReturn(null);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.create(vm);

        assertEquals("Child", result.getName());
        assertEquals(parent, result.getParent());
    }

    @Test
    void create_DuplicateName_ShouldThrow() {
        CategoryPostVm vm = new CategoryPostVm("Electronics", "elec", "d", null,
            null, null, null, true, null);
        when(categoryRepository.findExistedName("Electronics", null)).thenReturn(category);

        assertThrows(DuplicatedException.class, () -> categoryService.create(vm));
    }

    @Test
    void create_ParentNotFound_ShouldThrow() {
        CategoryPostVm vm = new CategoryPostVm("NewCat", "new", "d", 999L,
            null, null, null, true, null);
        when(categoryRepository.findExistedName("NewCat", null)).thenReturn(null);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> categoryService.create(vm));
    }

    // ========== update ==========
    @Test
    void update_Success_RemoveParent() {
        CategoryPostVm vm = new CategoryPostVm("Updated", "updated", "d", null,
            null, null, null, true, null);
        when(categoryRepository.findExistedName("Updated", 1L)).thenReturn(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.update(vm, 1L);

        assertEquals("Updated", category.getName());
        assertNull(category.getParent());
    }

    @Test
    void update_Success_WithParent() {
        Category parent = new Category();
        parent.setId(2L);
        parent.setName("Parent");
        CategoryPostVm vm = new CategoryPostVm("Updated", "updated", "d", 2L,
            null, null, null, true, null);
        when(categoryRepository.findExistedName("Updated", 1L)).thenReturn(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(parent));

        categoryService.update(vm, 1L);

        assertEquals(parent, category.getParent());
    }

    @Test
    void update_NotFound_ShouldThrow() {
        CategoryPostVm vm = new CategoryPostVm("X", "x", "d", null,
            null, null, null, true, null);
        when(categoryRepository.findExistedName("X", 999L)).thenReturn(null);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.update(vm, 999L));
    }

    @Test
    void update_DuplicateName_ShouldThrow() {
        CategoryPostVm vm = new CategoryPostVm("Existing", "ex", "d", null,
            null, null, null, true, null);
        when(categoryRepository.findExistedName("Existing", 1L)).thenReturn(new Category());

        assertThrows(DuplicatedException.class, () -> categoryService.update(vm, 1L));
    }

    @Test
    void update_ParentIsItself_ShouldThrow() {
        CategoryPostVm vm = new CategoryPostVm("Self", "self", "d", 1L,
            null, null, null, true, null);
        when(categoryRepository.findExistedName("Self", 1L)).thenReturn(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(BadRequestException.class, () -> categoryService.update(vm, 1L));
    }

    // ========== getCategoryById ==========
    @Test
    void getCategoryById_WithImage_WithParent() {
        Category parent = new Category();
        parent.setId(2L);
        category.setParent(parent);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(mediaService.getMedia(10L)).thenReturn(noFileMediaVm);

        CategoryGetDetailVm result = categoryService.getCategoryById(1L);

        assertEquals("Electronics", result.name());
        assertEquals(2L, result.parentId());
        assertNotNull(result.categoryImage());
    }

    @Test
    void getCategoryById_NoImage_NoParent() {
        category.setImageId(null);
        category.setParent(null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryGetDetailVm result = categoryService.getCategoryById(1L);

        assertNull(result.categoryImage());
        assertEquals(0L, result.parentId());
    }

    @Test
    void getCategoryById_NotFound_ShouldThrow() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(999L));
    }

    // ========== getCategories ==========
    @Test
    void getCategories_WithImage() {
        when(categoryRepository.findByNameContainingIgnoreCase("Elec")).thenReturn(List.of(category));
        when(mediaService.getMedia(10L)).thenReturn(noFileMediaVm);

        List<CategoryGetVm> result = categoryService.getCategories("Elec");

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).name());
    }

    @Test
    void getCategories_NoImage_WithParent() {
        category.setImageId(null);
        Category parent = new Category();
        parent.setId(5L);
        category.setParent(parent);
        when(categoryRepository.findByNameContainingIgnoreCase("Elec")).thenReturn(List.of(category));

        List<CategoryGetVm> result = categoryService.getCategories("Elec");

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).parentId());
    }

    @Test
    void getCategories_Empty() {
        when(categoryRepository.findByNameContainingIgnoreCase("xyz")).thenReturn(Collections.emptyList());

        List<CategoryGetVm> result = categoryService.getCategories("xyz");

        assertTrue(result.isEmpty());
    }

    // ========== getCategoryByIds ==========
    @Test
    void getCategoryByIds_ShouldReturnList() {
        when(categoryRepository.findAllById(List.of(1L))).thenReturn(List.of(category));

        List<CategoryGetVm> result = categoryService.getCategoryByIds(List.of(1L));

        assertEquals(1, result.size());
    }

    // ========== getTopNthCategories ==========
    @Test
    void getTopNthCategories_ShouldReturnNames() {
        when(categoryRepository.findCategoriesOrderedByProductCount(any(Pageable.class)))
            .thenReturn(List.of("Electronics", "Books"));

        List<String> result = categoryService.getTopNthCategories(2);

        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0));
    }
}
