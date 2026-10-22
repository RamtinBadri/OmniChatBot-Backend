package dev.ramtin.aiplatform.admin;

import dev.ramtin.aiplatform.admin.dto.ModelPricingCatalogDTO;
import dev.ramtin.aiplatform.admin.dto.ModelPricingRequest;
import dev.ramtin.aiplatform.model_pricing.ModelPricing;
import dev.ramtin.aiplatform.model_pricing.service.ModelPricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminModelPricingControllerTest {
    private ModelPricingService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(ModelPricingService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminModelPricingController(service)).build();
    }

    @Test
    void getPricing_returnsDefaultsAndPersistedOverrides() throws Exception {
        when(service.getDefaultInputPricePerMillion()).thenReturn(new BigDecimal("0.50"));
        when(service.getDefaultOutputPricePerMillion()).thenReturn(new BigDecimal("2.00"));
        when(service.getAllPricing()).thenReturn(List.of(ModelPricing.builder().model("provider/model")
                .inputPricePerMillion(new BigDecimal("0.25")).outputPricePerMillion(new BigDecimal("1.25"))
                .active(true).build()));

        mockMvc.perform(get("/api/admin/model-pricing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultInputPricePerMillion").value(0.50))
                .andExpect(jsonPath("$.defaultOutputPricePerMillion").value(2.00))
                .andExpect(jsonPath("$.pricing[0].model").value("provider/model"))
                .andExpect(jsonPath("$.pricing[0].inputPricePerMillion").value(0.25));
    }

    @Test
    void savePricing_acceptsValidRatesAndReturnsSavedOverride() throws Exception {
        when(service.savePricing(eq("provider/model"), any(BigDecimal.class), any(BigDecimal.class), eq(true)))
                .thenReturn(ModelPricing.builder().model("provider/model").inputPricePerMillion(new BigDecimal("0.25"))
                        .outputPricePerMillion(new BigDecimal("1.25")).active(true).build());

        mockMvc.perform(put("/api/admin/model-pricing")
                        .contentType("application/json")
                        .content("""
                                {"model":"provider/model","inputPricePerMillion":0.25,"outputPricePerMillion":1.25}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("provider/model"))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).savePricing(eq("provider/model"), eq(new BigDecimal("0.25")), eq(new BigDecimal("1.25")), eq(true));
    }

    @Test
    void savePricing_rejectsNegativeRatesAndBlankModel() throws Exception {
        mockMvc.perform(put("/api/admin/model-pricing").contentType("application/json")
                        .content("{\"model\":\"provider/model\",\"inputPricePerMillion\":-1,\"outputPricePerMillion\":1}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/admin/model-pricing").contentType("application/json")
                        .content("{\"model\":\" \",\"inputPricePerMillion\":1,\"outputPricePerMillion\":1}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
