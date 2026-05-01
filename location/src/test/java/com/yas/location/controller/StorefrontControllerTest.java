package com.yas.location.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.location.LocationApplication;
import com.yas.location.service.CountryService;
import com.yas.location.service.DistrictService;
import com.yas.location.service.StateOrProvinceService;
import com.yas.location.viewmodel.country.CountryVm;
import com.yas.location.viewmodel.district.DistrictGetVm;
import com.yas.location.viewmodel.stateorprovince.StateOrProvinceVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

@WebMvcTest(
    controllers = {
        CountryStorefrontController.class,
        StateOrProvinceStoreFrontController.class,
        DistrictStorefrontController.class
    },
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class StorefrontControllerTest {

    @MockitoBean
    private CountryService countryService;

    @MockitoBean
    private StateOrProvinceService stateOrProvinceService;

    @MockitoBean
    private DistrictService districtService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @Test
    void testListCountries_whenNormalCase_thenReturnOk() throws Exception {
        List<CountryVm> response = List.of(new CountryVm(1L, "VN", "Vietnam", "VNM", true, true, true, true, true));
        given(countryService.findAllCountries()).willReturn(response);

        mockMvc.perform(get("/storefront/countries"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void testGetStateOrProvince_whenNormalCase_thenReturnOk() throws Exception {
        List<StateOrProvinceVm> response = List.of(new StateOrProvinceVm(1L, "HCM", "SG", "city", 1L));
        given(stateOrProvinceService.getAllByCountryId(1L)).willReturn(response);

        mockMvc.perform(get("/storefront/state-or-provinces/1"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void testGetDistrictList_whenNormalCase_thenReturnOk() throws Exception {
        List<DistrictGetVm> response = List.of(new DistrictGetVm(1L, "District 1"));
        given(districtService.getList(1L)).willReturn(response);

        mockMvc.perform(get("/storefront/district/1"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void testGetDistrictListFromBackoffice_whenNormalCase_thenReturnOk() throws Exception {
        List<DistrictGetVm> response = List.of(new DistrictGetVm(2L, "District 2"));
        given(districtService.getList(2L)).willReturn(response);

        mockMvc.perform(get("/backoffice/district/2"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }
}
