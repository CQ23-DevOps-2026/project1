package com.yas.location.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import com.yas.location.model.Country;
import com.yas.location.model.StateOrProvince;
import com.yas.location.service.StateOrProvinceService;
import com.yas.location.utils.Constants;
import com.yas.location.viewmodel.stateorprovince.StateOrProvinceAndCountryGetNameVm;
import com.yas.location.viewmodel.stateorprovince.StateOrProvinceListGetVm;
import com.yas.location.viewmodel.stateorprovince.StateOrProvincePostVm;
import com.yas.location.viewmodel.stateorprovince.StateOrProvinceVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StateOrProvinceController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class StateOrProvinceControllerTest {

    @MockitoBean
    private StateOrProvinceService stateOrProvinceService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @Test
    void testCreateStateOrProvince_whenRequestIsValid_thenReturnOk() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("name")
            .code("code")
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);
        StateOrProvince stateOrProvince = StateOrProvince.builder()
            .id(1L)
            .country(Country.builder().id(1L).build())
            .build();
        given(stateOrProvinceService.createStateOrProvince(stateOrProvincePostVm)).willReturn(
            stateOrProvince);

        this.mockMvc.perform(post(Constants.ApiConstant.STATE_OR_PROVINCES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated());
    }

    @Test
    void testCreateStateOrProvince_whenCodeIsOverMaxLength_thenReturnBadRequest() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("name")
            .code("1234567890".repeat(26))
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);

        this.mockMvc.perform(post(Constants.ApiConstant.STATE_OR_PROVINCES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateStateOrProvince_whenNameIsBlank_thenReturnBadRequest() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("")
            .code("code")
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);

        this.mockMvc.perform(post(Constants.ApiConstant.STATE_OR_PROVINCES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateStateOrProvince_whenRequestIsValid_thenReturnOk() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("name")
            .code("code")
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);

        this.mockMvc.perform(put(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateStateOrProvince_whenCodeIsOverMaxLength_thenReturnBadRequest() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("name")
            .code("1234567890".repeat(26))
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);

        this.mockMvc.perform(put(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateStateOrProvince_whenNameIsBlank_thenReturnBadRequest() throws Exception {
        StateOrProvincePostVm stateOrProvincePostVm = StateOrProvincePostVm.builder()
            .name("")
            .code("code")
            .type("type")
            .countryId(1L)
            .build();

        String request = objectWriter.writeValueAsString(stateOrProvincePostVm);

        this.mockMvc.perform(put(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPageableStateOrProvinces_whenNormalCase_thenReturnOk() throws Exception {
        StateOrProvinceListGetVm response = new StateOrProvinceListGetVm(List.of(), 0, 10, 0, 0, true);
        given(stateOrProvinceService.getPageableStateOrProvinces(0, 10, 1L)).willReturn(response);

        mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/paging")
                .param("countryId", "1"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void testGetAllByCountryId_whenNormalCase_thenReturnOk() throws Exception {
        List<StateOrProvinceVm> response = List.of(new StateOrProvinceVm(1L, "HCM", "SG", "city", 1L));
        given(stateOrProvinceService.getAllByCountryId(1L)).willReturn(response);

        mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL)
                .param("countryId", "1"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void testGetStateOrProvince_whenNormalCase_thenReturnOk() throws Exception {
        StateOrProvinceVm response = new StateOrProvinceVm(1L, "HCM", "SG", "city", 1L);
        given(stateOrProvinceService.findById(1L)).willReturn(response);

        mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/1"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void testGetStateOrProvinceAndCountryNames_whenNormalCase_thenReturnOk() throws Exception {
        List<StateOrProvinceAndCountryGetNameVm> response =
            List.of(new StateOrProvinceAndCountryGetNameVm(1L, "HCM", "Vietnam"));
        given(stateOrProvinceService.getStateOrProvinceAndCountryNames(List.of(1L, 2L))).willReturn(response);

        mockMvc.perform(get(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/state-country-names")
                .param("stateOrProvinceIds", "1", "2"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void testDeleteStateOrProvince_whenNormalCase_thenReturnNoContent() throws Exception {
        mockMvc.perform(delete(Constants.ApiConstant.STATE_OR_PROVINCES_URL + "/1"))
            .andExpect(status().isNoContent());
    }
}
