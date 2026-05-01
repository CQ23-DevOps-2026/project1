package com.yas.tax.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// CI note: touched to validate SonarQube/SonarCloud pipeline stages.

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.service.TaxRateService;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TaxRateController.class, excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class TaxRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TaxRateService taxRateService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getPageableTaxRates_ShouldReturnOk() throws Exception {
        TaxRateListGetVm vm = new TaxRateListGetVm(List.of(), 0, 10, 0, 0, true);
        when(taxRateService.getPageableTaxRates(anyInt(), anyInt())).thenReturn(vm);

        mockMvc.perform(get("/backoffice/tax-rates/paging"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNo").value(0));
    }

    @Test
    void getTaxRate_ShouldReturnOk() throws Exception {
        // TaxRateVm(id, rate, zipCode, taxClassId, stateOrProvinceId, countryId)
        TaxRateVm vm = new TaxRateVm(1L, 10.0, "10000", 1L, 1L, 1L);
        when(taxRateService.findById(1L)).thenReturn(vm);

        mockMvc.perform(get("/backoffice/tax-rates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createTaxRate_ShouldReturnCreated() throws Exception {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "10000", 1L, 1L, 1L);
        TaxClass taxClass = TaxClass.builder().id(1L).name("Class 1").build();
        TaxRate taxRate = TaxRate.builder().id(1L).rate(10.0).taxClass(taxClass).build();
        when(taxRateService.createTaxRate(any(TaxRatePostVm.class))).thenReturn(taxRate);

        mockMvc.perform(post("/backoffice/tax-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postVm)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateTaxRate_ShouldReturnNoContent() throws Exception {
        TaxRatePostVm postVm = new TaxRatePostVm(10.0, "10000", 1L, 1L, 1L);

        mockMvc.perform(put("/backoffice/tax-rates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postVm)))
                .andExpect(status().isNoContent());

        verify(taxRateService).updateTaxRate(any(TaxRatePostVm.class), anyLong());
    }

    @Test
    void deleteTaxRate_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/backoffice/tax-rates/1"))
                .andExpect(status().isNoContent());

        verify(taxRateService).delete(1L);
    }

    @Test
    void getTaxPercentByAddress_ShouldReturnOk() throws Exception {
        when(taxRateService.getTaxPercent(anyLong(), anyLong(), any(), any())).thenReturn(10.0);

        mockMvc.perform(get("/backoffice/tax-rates/tax-percent")
                .param("taxClassId", "1")
                .param("countryId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getBatchTaxPercentsByAddress_ShouldReturnOk() throws Exception {
        when(taxRateService.getBulkTaxRate(any(), anyLong(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/backoffice/tax-rates/location-based-batch")
                .param("taxClassIds", "1,2")
                .param("countryId", "1"))
                .andExpect(status().isOk());
    }
}
