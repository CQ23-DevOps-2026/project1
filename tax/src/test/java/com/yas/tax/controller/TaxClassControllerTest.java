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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.tax.model.TaxClass;
import com.yas.tax.service.TaxClassService;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
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

@WebMvcTest(controllers = TaxClassController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class TaxClassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TaxClassService taxClassService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getPageableTaxClasses_ShouldReturnOk() throws Exception {
        // TaxClassListGetVm(taxClassContent, pageNo, pageSize, totalElements, totalPages, isLast)
        TaxClassListGetVm vm = new TaxClassListGetVm(List.of(), 0, 10, 0, 0, true);
        when(taxClassService.getPageableTaxClasses(anyInt(), anyInt())).thenReturn(vm);

        mockMvc.perform(get("/backoffice/tax-classes/paging"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pageNo").value(0));
    }

    @Test
    void listTaxClasses_ShouldReturnOk() throws Exception {
        when(taxClassService.findAllTaxClasses()).thenReturn(List.of());

        mockMvc.perform(get("/backoffice/tax-classes"))
            .andExpect(status().isOk());
    }

    @Test
    void getTaxClass_ShouldReturnOk() throws Exception {
        TaxClassVm vm = new TaxClassVm(1L, "Class 1");
        when(taxClassService.findById(1L)).thenReturn(vm);

        mockMvc.perform(get("/backoffice/tax-classes/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createTaxClass_ShouldReturnCreated() throws Exception {
        // TaxClassPostVm has 2 fields: id (String) and name (String)
        TaxClassPostVm postVm = new TaxClassPostVm("id-1", "Class 1");
        TaxClass taxClass = TaxClass.builder().id(1L).name("Class 1").build();
        when(taxClassService.create(any(TaxClassPostVm.class))).thenReturn(taxClass);

        mockMvc.perform(post("/backoffice/tax-classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postVm)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateTaxClass_ShouldReturnNoContent() throws Exception {
        TaxClassPostVm postVm = new TaxClassPostVm("id-1", "Class 1");

        mockMvc.perform(put("/backoffice/tax-classes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postVm)))
            .andExpect(status().isNoContent());

        verify(taxClassService).update(any(TaxClassPostVm.class), anyLong());
    }

    @Test
    void deleteTaxClass_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/backoffice/tax-classes/1"))
            .andExpect(status().isNoContent());

        verify(taxClassService).delete(1L);
    }
}
