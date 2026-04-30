package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class TaxClassServiceTest {

    private TaxClassRepository taxClassRepository;
    private TaxClassService taxClassService;

    @BeforeEach
    void setUp() {
        taxClassRepository = mock(TaxClassRepository.class);
        taxClassService = new TaxClassService(taxClassRepository);
    }

    @Test
    void findAllTaxClasses_ShouldReturnSortedList() {
        TaxClass taxClass1 = TaxClass.builder().id(1L).name("A").build();
        TaxClass taxClass2 = TaxClass.builder().id(2L).name("B").build();
        when(taxClassRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of(taxClass1, taxClass2));

        List<TaxClassVm> result = taxClassService.findAllTaxClasses();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("A");
    }

    @Test
    void findById_WhenExisted_ShouldReturnVm() {
        TaxClass taxClass = TaxClass.builder().id(1L).name("A").build();
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));

        TaxClassVm result = taxClassService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_WhenNotExisted_ShouldThrowNotFoundException() {
        when(taxClassRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxClassService.findById(1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_WhenNameNotExisted_ShouldSave() {
        // TaxClassPostVm has 2 fields: id (String) and name (String)
        TaxClassPostVm postVm = new TaxClassPostVm("id-1", "A");
        TaxClass taxClass = TaxClass.builder().id(1L).name("A").build();
        when(taxClassRepository.existsByName("A")).thenReturn(false);
        when(taxClassRepository.save(any(TaxClass.class))).thenReturn(taxClass);

        TaxClass result = taxClassService.create(postVm);

        assertThat(result).isNotNull();
        verify(taxClassRepository).save(any(TaxClass.class));
    }

    @Test
    void create_WhenNameExisted_ShouldThrowDuplicatedException() {
        TaxClassPostVm postVm = new TaxClassPostVm("id-1", "A");
        when(taxClassRepository.existsByName("A")).thenReturn(true);

        assertThatThrownBy(() -> taxClassService.create(postVm))
            .isInstanceOf(DuplicatedException.class);
    }

    @Test
    void update_WhenExistedAndNameNotExisted_ShouldSave() {
        TaxClassPostVm postVm = new TaxClassPostVm("id-1", "B");
        TaxClass taxClass = TaxClass.builder().id(1L).name("A").build();
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));
        when(taxClassRepository.existsByNameNotUpdatingTaxClass("B", 1L)).thenReturn(false);

        taxClassService.update(postVm, 1L);

        assertThat(taxClass.getName()).isEqualTo("B");
        verify(taxClassRepository).save(taxClass);
    }

    @Test
    void update_WhenNotExisted_ShouldThrowNotFoundException() {
        TaxClassPostVm postVm = new TaxClassPostVm("id-1", "B");
        when(taxClassRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxClassService.update(postVm, 1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_WhenNameExisted_ShouldThrowDuplicatedException() {
        TaxClassPostVm postVm = new TaxClassPostVm("id-1", "B");
        TaxClass taxClass = TaxClass.builder().id(1L).name("A").build();
        when(taxClassRepository.findById(1L)).thenReturn(Optional.of(taxClass));
        when(taxClassRepository.existsByNameNotUpdatingTaxClass("B", 1L)).thenReturn(true);

        assertThatThrownBy(() -> taxClassService.update(postVm, 1L))
            .isInstanceOf(DuplicatedException.class);
    }

    @Test
    void delete_WhenExisted_ShouldDelete() {
        when(taxClassRepository.existsById(1L)).thenReturn(true);

        taxClassService.delete(1L);

        verify(taxClassRepository).deleteById(1L);
    }

    @Test
    void delete_WhenNotExisted_ShouldThrowNotFoundException() {
        when(taxClassRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> taxClassService.delete(1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getPageableTaxClasses_ShouldReturnVm() {
        TaxClass taxClass = TaxClass.builder().id(1L).name("A").build();
        Page<TaxClass> page = new PageImpl<>(List.of(taxClass));
        Pageable pageable = PageRequest.of(0, 10);
        when(taxClassRepository.findAll(pageable)).thenReturn(page);

        TaxClassListGetVm result = taxClassService.getPageableTaxClasses(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.taxClassContent()).hasSize(1);
    }
}
