package com.yas.location.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yas.location.model.Country;
import com.yas.location.model.StateOrProvince;
import com.yas.location.viewmodel.country.CountryPostVm;
import com.yas.location.viewmodel.country.CountryVm;
import com.yas.location.viewmodel.stateorprovince.StateOrProvinceVm;
import org.junit.jupiter.api.Test;

class MapperTest {

    private final CountryMapper countryMapper = new CountryMapperImpl();
    private final StateOrProvinceMapper stateOrProvinceMapper = new StateOrProvinceMapperImpl();

    @Test
    void testCountryMapperToCountry_whenDtoIsNull_thenReturnNull() {
        assertNull(countryMapper.toCountryFromCountryPostViewModel(null));
    }

    @Test
    void testCountryMapperToCountry_whenDtoHasValues_thenMapAllFields() {
        CountryPostVm dto = new CountryPostVm(
            "VN",
            "VN",
            "Vietnam",
            "VNM",
            true,
            false,
            true,
            false,
            true
        );

        Country country = countryMapper.toCountryFromCountryPostViewModel(dto);

        assertNotNull(country);
        assertEquals("Vietnam", country.getName());
        assertEquals("VN", country.getCode2());
        assertEquals("VNM", country.getCode3());
        assertTrue(country.getIsBillingEnabled());
        assertFalse(country.getIsShippingEnabled());
        assertTrue(country.getIsDistrictEnabled());
    }

    @Test
    void testCountryMapperUpdate_whenDtoIsNull_thenKeepOriginalValues() {
        Country country = Country.builder().name("Vietnam").code2("VN").build();

        countryMapper.toCountryFromCountryPostViewModel(country, null);

        assertEquals("Vietnam", country.getName());
        assertEquals("VN", country.getCode2());
    }

    @Test
    void testCountryMapperUpdate_whenDtoHasMixedNulls_thenOnlyNonNullFieldsUpdated() {
        Country country = Country.builder()
            .name("Vietnam")
            .code2("VN")
            .code3("VNM")
            .isBillingEnabled(false)
            .isShippingEnabled(false)
            .isCityEnabled(false)
            .isZipCodeEnabled(false)
            .isDistrictEnabled(false)
            .build();
        CountryPostVm dto = new CountryPostVm(
            "VN",
            null,
            "Viet Nam",
            null,
            true,
            null,
            true,
            null,
            true
        );

        countryMapper.toCountryFromCountryPostViewModel(country, dto);

        assertEquals("Viet Nam", country.getName());
        assertEquals("VN", country.getCode2());
        assertEquals("VNM", country.getCode3());
        assertTrue(country.getIsBillingEnabled());
        assertFalse(country.getIsShippingEnabled());
        assertTrue(country.getIsCityEnabled());
        assertFalse(country.getIsZipCodeEnabled());
        assertTrue(country.getIsDistrictEnabled());
    }

    @Test
    void testCountryMapperToViewModel_whenCountryIsNull_thenReturnNull() {
        assertNull(countryMapper.toCountryViewModelFromCountry(null));
    }

    @Test
    void testCountryMapperToViewModel_whenCountryHasValues_thenMapAllFields() {
        Country country = Country.builder()
            .id(1L)
            .name("Vietnam")
            .code2("VN")
            .code3("VNM")
            .isBillingEnabled(true)
            .isShippingEnabled(true)
            .isCityEnabled(true)
            .isZipCodeEnabled(true)
            .isDistrictEnabled(true)
            .build();

        CountryVm result = countryMapper.toCountryViewModelFromCountry(country);

        assertEquals(1L, result.id());
        assertEquals("Vietnam", result.name());
        assertEquals("VN", result.code2());
        assertEquals("VNM", result.code3());
        assertTrue(result.isDistrictEnabled());
    }

    @Test
    void testStateOrProvinceMapper_whenInputIsNull_thenReturnNull() {
        assertNull(stateOrProvinceMapper.toStateOrProvinceViewModelFromStateOrProvince(null));
    }

    @Test
    void testStateOrProvinceMapper_whenCountryIsNull_thenCountryIdIsNull() {
        StateOrProvince state = StateOrProvince.builder()
            .id(1L)
            .name("HCM")
            .code("SG")
            .type("city")
            .country(null)
            .build();

        StateOrProvinceVm result = stateOrProvinceMapper.toStateOrProvinceViewModelFromStateOrProvince(state);

        assertEquals(1L, result.id());
        assertNull(result.countryId());
    }

    @Test
    void testStateOrProvinceMapper_whenCountryExists_thenMapCountryId() {
        StateOrProvince state = StateOrProvince.builder()
            .id(2L)
            .name("Hanoi")
            .code("HN")
            .type("city")
            .country(Country.builder().id(99L).build())
            .build();

        StateOrProvinceVm result = stateOrProvinceMapper.toStateOrProvinceViewModelFromStateOrProvince(state);

        assertEquals(99L, result.countryId());
        assertEquals("Hanoi", result.name());
    }
}
