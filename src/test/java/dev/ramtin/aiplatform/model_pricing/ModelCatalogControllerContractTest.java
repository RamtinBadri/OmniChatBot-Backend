package dev.ramtin.aiplatform.model_pricing;

import dev.ramtin.aiplatform.model_pricing.dto.AvailableModelDTO;
import dev.ramtin.aiplatform.model_pricing.dto.ModelCatalogDTO;
import dev.ramtin.aiplatform.model_pricing.service.ModelPricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ModelCatalogControllerContractTest {
    private ModelPricingService modelPricingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        modelPricingService = mock(ModelPricingService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ModelCatalogController(modelPricingService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        when(modelPricingService.getModelCatalog(any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(0);
            return new ModelCatalogDTO("default-model", new PageImpl<>(
                    List.of(new AvailableModelDTO("default-model", "Default", true)), pageable, 1));
        });
    }

    @Test
    void getModels_keepsNestedDirectPageWithDefaultSize20() throws Exception {
        mockMvc.perform(get("/api/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultModel").value("default-model"))
                .andExpect(jsonPath("$.models.content[0].id").value("default-model"))
                .andExpect(jsonPath("$.models.number").value(0))
                .andExpect(jsonPath("$.models.size").value(20))
                .andExpect(jsonPath("$.models.totalElements").value(1))
                .andExpect(jsonPath("$.models.page").doesNotExist());

        verify(modelPricingService).getModelCatalog(PageRequest.of(0, 20));
    }

    @Test
    void getModels_capsOversizedPageAtExistingResolverLimit() throws Exception {
        mockMvc.perform(get("/api/models?size=2147483647"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.models.size").value(2000));

        verify(modelPricingService).getModelCatalog(PageRequest.of(0, 2000));
    }
}
