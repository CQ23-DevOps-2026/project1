package com.yas.tax.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.viewmodel.error.ErrorVm;
import com.yas.tax.viewmodel.location.StateOrProvinceAndCountryGetNameVm;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
import com.yas.tax.viewmodel.taxrate.TaxRateGetDetailVm;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import org.junit.jupiter.api.Test;

class ViewModelCoverageTest {

    @Test
    void testTaxClassVms() {
        TaxClass taxClass = TaxClass.builder().id(1L).name("Class 1").build();

        TaxClassVm vm = TaxClassVm.fromModel(taxClass);
        assertThat(vm.id()).isEqualTo(1L);
        assertThat(vm.name()).isEqualTo("Class 1");

        // TaxClassPostVm has 2 fields: id (String) and name (String)
        TaxClassPostVm postVm = new TaxClassPostVm("id-1", "New Class");
        assertThat(postVm.name()).isEqualTo("New Class");
        assertThat(postVm.id()).isEqualTo("id-1");
        TaxClass fromPost = postVm.toModel();
        assertThat(fromPost.getName()).isEqualTo("New Class");

        // TaxClassListGetVm uses field name "taxClassContent"
        TaxClassListGetVm listVm = new TaxClassListGetVm(List.of(vm), 0, 10, 1, 1, true);
        assertThat(listVm.taxClassContent()).hasSize(1);
        assertThat(listVm.pageNo()).isEqualTo(0);
        assertThat(listVm.pageSize()).isEqualTo(10);
        assertThat(listVm.totalElements()).isEqualTo(1);
        assertThat(listVm.totalPages()).isEqualTo(1);
        assertThat(listVm.isLast()).isTrue();
    }

    @Test
    void testTaxRateVms() {
        TaxClass taxClass = TaxClass.builder().id(1L).name("Class 1").build();
        TaxRate taxRate = TaxRate.builder()
            .id(1L)
            .rate(10.0)
            .zipCode("10000")
            .stateOrProvinceId(1L)
            .countryId(1L)
            .taxClass(taxClass)
            .build();

        // TaxRateVm(id, rate, zipCode, taxClassId, stateOrProvinceId, countryId)
        TaxRateVm vm = TaxRateVm.fromModel(taxRate);
        assertThat(vm.id()).isEqualTo(1L);
        assertThat(vm.rate()).isEqualTo(10.0);
        assertThat(vm.zipCode()).isEqualTo("10000");

        // TaxRatePostVm(rate, zipCode, taxClassId, stateOrProvinceId, countryId)
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "10000", 1L, 1L, 1L);
        assertThat(postVm.rate()).isEqualTo(10.0);

        TaxRateGetDetailVm detailVm = new TaxRateGetDetailVm(1L, 10.0, "10000", "Class 1", "State 1", "Country 1");
        assertThat(detailVm.id()).isEqualTo(1L);
        assertThat(detailVm.stateOrProvinceName()).isEqualTo("State 1");

        // TaxRateListGetVm uses field name "taxRateGetDetailContent"
        TaxRateListGetVm listVm = new TaxRateListGetVm(List.of(detailVm), 0, 10, 1, 1, true);
        assertThat(listVm.taxRateGetDetailContent()).hasSize(1);
    }

    @Test
    void testOtherVms() {
        // StateOrProvinceAndCountryGetNameVm(stateOrProvinceId, stateOrProvinceName, countryName)
        StateOrProvinceAndCountryGetNameVm locationVm =
            new StateOrProvinceAndCountryGetNameVm(1L, "State 1", "Country 1");
        assertThat(locationVm.stateOrProvinceId()).isEqualTo(1L);
        assertThat(locationVm.stateOrProvinceName()).isEqualTo("State 1");
        assertThat(locationVm.countryName()).isEqualTo("Country 1");

        ErrorVm errorVm = new ErrorVm("404", "Not Found", "Detail");
        assertThat(errorVm.statusCode()).isEqualTo("404");
        assertThat(errorVm.title()).isEqualTo("Not Found");
        assertThat(errorVm.detail()).isEqualTo("Detail");
    }

    @Test
    void testModels() {
        TaxClass taxClass = new TaxClass();
        taxClass.setId(1L);
        taxClass.setName("N");
        assertThat(taxClass.getId()).isEqualTo(1L);
        assertThat(taxClass.getName()).isEqualTo("N");

        TaxRate taxRate = new TaxRate();
        taxRate.setId(1L);
        taxRate.setRate(10.0);
        taxRate.setZipCode("Z");
        taxRate.setCountryId(1L);
        taxRate.setStateOrProvinceId(1L);
        taxRate.setTaxClass(taxClass);

        assertThat(taxRate.getId()).isEqualTo(1L);
        assertThat(taxRate.getRate()).isEqualTo(10.0);
        assertThat(taxRate.getZipCode()).isEqualTo("Z");
        assertThat(taxRate.getCountryId()).isEqualTo(1L);
        assertThat(taxRate.getStateOrProvinceId()).isEqualTo(1L);
        assertThat(taxRate.getTaxClass()).isEqualTo(taxClass);
    }
}
