package com.yas.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.repository.TaxClassRepository;
import com.yas.tax.repository.TaxRateRepository;
import com.yas.tax.viewmodel.location.StateOrProvinceAndCountryGetNameVm;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class TaxRateServiceTest {

    private TaxRateRepository taxRateRepository;
    private TaxClassRepository taxClassRepository;
    private LocationService locationService;
    private TaxRateService taxRateService;

    @BeforeEach
    void setUp() {
        taxRateRepository = mock(TaxRateRepository.class);
        taxClassRepository = mock(TaxClassRepository.class);
        locationService = mock(LocationService.class);
        taxRateService = new TaxRateService(locationService, taxRateRepository, taxClassRepository);
    }

    @Test
    void createTaxRate_WhenTaxClassExisted_ShouldReturnTaxRate() {
        // rate, zipCode, taxClassId, stateOrProvinceId, countryId
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "10000", 1L, 1L, 1L);
        TaxClass taxClass = TaxClass.builder().id(1L).name("Class 1").build();
        TaxRate taxRate = TaxRate.builder().id(1L).rate(10.0).build();

        when(taxClassRepository.existsById(1L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(1L)).thenReturn(taxClass);
        when(taxRateRepository.save(any(TaxRate.class))).thenReturn(taxRate);

        TaxRate result = taxRateService.createTaxRate(postVm);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void createTaxRate_WhenTaxClassNotExisted_ShouldThrowNotFoundException() {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "10000", 1L, 1L, 1L);
        when(taxClassRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> taxRateService.createTaxRate(postVm))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateTaxRate_WhenTaxRateExistedAndTaxClassExisted_ShouldSaveTaxRate() {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "10000", 1L, 1L, 1L);
        TaxRate taxRate = TaxRate.builder().id(1L).build();
        TaxClass taxClass = TaxClass.builder().id(1L).build();

        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        when(taxClassRepository.existsById(1L)).thenReturn(true);
        when(taxClassRepository.getReferenceById(1L)).thenReturn(taxClass);

        taxRateService.updateTaxRate(postVm, 1L);

        verify(taxRateRepository).save(taxRate);
    }

    @Test
    void updateTaxRate_WhenTaxRateNotExisted_ShouldThrowNotFoundException() {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "10000", 1L, 1L, 1L);
        when(taxRateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxRateService.updateTaxRate(postVm, 1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateTaxRate_WhenTaxClassNotExisted_ShouldThrowNotFoundException() {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "10000", 1L, 1L, 1L);
        TaxRate taxRate = TaxRate.builder().id(1L).build();

        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        when(taxClassRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> taxRateService.updateTaxRate(postVm, 1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_WhenTaxRateExisted_ShouldDelete() {
        when(taxRateRepository.existsById(1L)).thenReturn(true);

        taxRateService.delete(1L);

        verify(taxRateRepository).deleteById(1L);
    }

    @Test
    void delete_WhenTaxRateNotExisted_ShouldThrowNotFoundException() {
        when(taxRateRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> taxRateService.delete(1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_WhenTaxRateExisted_ShouldReturnTaxRateVm() {
        TaxClass taxClass = TaxClass.builder().id(1L).name("Class 1").build();
        TaxRate taxRate = TaxRate.builder().id(1L).rate(10.0).taxClass(taxClass).build();

        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));

        TaxRateVm result = taxRateService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_WhenTaxRateNotExisted_ShouldThrowNotFoundException() {
        when(taxRateRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taxRateService.findById(1L))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findAll_ShouldReturnList() {
        TaxClass taxClass = TaxClass.builder().id(1L).name("Class 1").build();
        TaxRate taxRate = TaxRate.builder().id(1L).rate(10.0).taxClass(taxClass).build();
        when(taxRateRepository.findAll()).thenReturn(List.of(taxRate));

        List<TaxRateVm> result = taxRateService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void getPageableTaxRates_WithStateOrProvinceIds_ShouldReturnList() {
        TaxClass taxClass = TaxClass.builder().id(1L).name("Class 1").build();
        TaxRate taxRate = TaxRate.builder().id(1L).stateOrProvinceId(1L).taxClass(taxClass).build();
        Page<TaxRate> page = new PageImpl<>(List.of(taxRate));
        Pageable pageable = PageRequest.of(0, 10);

        when(taxRateRepository.findAll(pageable)).thenReturn(page);
        // StateOrProvinceAndCountryGetNameVm(stateOrProvinceId, stateOrProvinceName, countryName)
        StateOrProvinceAndCountryGetNameVm nameVm = new StateOrProvinceAndCountryGetNameVm(1L, "State 1", "Country 1");
        when(locationService.getStateOrProvinceAndCountryNames(any())).thenReturn(List.of(nameVm));

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.taxRateGetDetailContent()).hasSize(1);
        assertThat(result.taxRateGetDetailContent().get(0).stateOrProvinceName()).isEqualTo("State 1");
    }

    @Test
    void getPageableTaxRates_WithEmptyStateOrProvinceIds_ShouldReturnEmptyList() {
        Page<TaxRate> page = new PageImpl<>(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10);

        when(taxRateRepository.findAll(pageable)).thenReturn(page);

        TaxRateListGetVm result = taxRateService.getPageableTaxRates(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.taxRateGetDetailContent()).isEmpty();
    }

    @Test
    void getTaxPercent_WhenTaxPercentNotNull_ShouldReturnTaxPercent() {
        when(taxRateRepository.getTaxPercent(anyLong(), anyLong(), any(), anyLong())).thenReturn(10.0);

        double result = taxRateService.getTaxPercent(1L, 1L, 1L, "10000");

        assertThat(result).isEqualTo(10.0);
    }

    @Test
    void getTaxPercent_WhenTaxPercentNull_ShouldReturnZero() {
        when(taxRateRepository.getTaxPercent(anyLong(), anyLong(), any(), anyLong())).thenReturn(null);

        double result = taxRateService.getTaxPercent(1L, 1L, 1L, "10000");

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void getBulkTaxRate_ShouldReturnList() {
        TaxClass taxClass = TaxClass.builder().id(1L).name("Class 1").build();
        TaxRate taxRate = TaxRate.builder().id(1L).taxClass(taxClass).build();
        when(taxRateRepository.getBatchTaxRates(anyLong(), anyLong(), any(), any())).thenReturn(List.of(taxRate));

        List<TaxRateVm> result = taxRateService.getBulkTaxRate(List.of(1L), 1L, 1L, "10000");

        assertThat(result).hasSize(1);
    }
}
