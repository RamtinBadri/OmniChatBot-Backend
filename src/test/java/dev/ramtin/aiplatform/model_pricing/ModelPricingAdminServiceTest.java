package dev.ramtin.aiplatform.model_pricing;

import dev.ramtin.aiplatform.config.AIProperties;
import dev.ramtin.aiplatform.config.DefaultPricingProperties;
import dev.ramtin.aiplatform.model_pricing.repository.ModelPricingRepository;
import dev.ramtin.aiplatform.model_pricing.service.ModelPricingServiceImpl;
import dev.ramtin.aiplatform.omniroute.service.OmniRouteClient;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteModel;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteModelsResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelPricingAdminServiceTest {
    @Mock private ModelPricingRepository repository;
    @Mock private OmniRouteClient omniRouteClient;
    private ModelPricingServiceImpl service;

    @BeforeEach
    void setUp() {
        DefaultPricingProperties defaults = new DefaultPricingProperties();
        defaults.setDefaultInputPricePerMillion(new BigDecimal("0.50"));
        defaults.setDefaultOutputPricePerMillion(new BigDecimal("2.00"));
        AIProperties aiProperties = new AIProperties();
        aiProperties.setDefaultModel("auto/default");
        service = new ModelPricingServiceImpl(repository, defaults, aiProperties, omniRouteClient);
    }

    @Test
    void savePricing_createsNewOverrideWithTimestamps() {
        when(omniRouteClient.getModels()).thenReturn(new OmniRouteModelsResponse(List.of(new OmniRouteModel("provider/model", "Model"))));
        when(repository.findByModel("provider/model")).thenReturn(Optional.empty());
        when(repository.save(any(ModelPricing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ModelPricing saved = service.savePricing("provider/model", new BigDecimal("0.125"), new BigDecimal("0.75"), true);

        assertEquals("provider/model", saved.getModel());
        assertEquals(new BigDecimal("0.125"), saved.getInputPricePerMillion());
        assertEquals(new BigDecimal("0.75"), saved.getOutputPricePerMillion());
        assertTrue(saved.isActive());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(saved.getCreatedAt(), saved.getUpdatedAt());
    }

    @Test
    void savePricing_updatesExistingOverrideAndKeepsCreatedAt() {
        var createdAt = java.time.LocalDateTime.parse("2025-01-01T00:00:00");
        ModelPricing existing = ModelPricing.builder().model("provider/model").createdAt(createdAt).active(true).build();
        when(repository.findByModel("provider/model")).thenReturn(Optional.of(existing));
        when(repository.save(any(ModelPricing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ModelPricing saved = service.savePricing("provider/model", new BigDecimal("1.25"), new BigDecimal("3.75"), false);

        assertSame(existing, saved);
        assertEquals(createdAt, saved.getCreatedAt());
        assertEquals(new BigDecimal("1.25"), saved.getInputPricePerMillion());
        assertEquals(new BigDecimal("3.75"), saved.getOutputPricePerMillion());
        assertFalse(saved.isActive());
        assertNotNull(saved.getUpdatedAt());
        verify(repository).save(existing);
    }

    @Test
    void savePricing_rejectsNegativeOrExcessPrecisionPrices() {
        assertThrows(IllegalArgumentException.class,
                () -> service.savePricing("provider/model", new BigDecimal("-0.01"), BigDecimal.ONE, true));
        assertThrows(IllegalArgumentException.class,
                () -> service.savePricing("provider/model", new BigDecimal("1.1234567"), BigDecimal.ONE, true));
        assertThrows(IllegalArgumentException.class,
                () -> service.savePricing("  ", BigDecimal.ONE, BigDecimal.ONE, true));
        verify(repository, never()).save(any());
    }

    @Test
    void deactivatePricing_setsInactiveAndUpdatesTimestamp() {
        ModelPricing existing = ModelPricing.builder().model("provider/model").active(true).build();
        when(repository.findByModel("provider/model")).thenReturn(Optional.of(existing));
        when(repository.save(any(ModelPricing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deactivatePricing("provider/model");

        assertFalse(existing.isActive());
        assertNotNull(existing.getUpdatedAt());
        verify(repository).save(existing);
    }

    @Test
    void deactivatePricing_causesRuntimeLookupToUseDefaults() {
        ModelPricing existing = ModelPricing.builder().model("provider/model")
                .inputPricePerMillion(new BigDecimal("0.10")).outputPricePerMillion(new BigDecimal("0.90"))
                .active(true).build();
        when(repository.findByModel("provider/model")).thenReturn(Optional.of(existing));
        when(repository.findByModelAndActiveTrue("provider/model")).thenReturn(Optional.empty());
        when(repository.save(any(ModelPricing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.deactivatePricing("provider/model");
        ModelPricing effective = service.getPricing("provider/model");

        assertEquals(new BigDecimal("0.50"), effective.getInputPricePerMillion());
        assertEquals(new BigDecimal("2.00"), effective.getOutputPricePerMillion());
        assertTrue(effective.isActive());
    }

    @Test
    void deactivatePricing_throwsWhenOverrideMissing() {
        when(repository.findByModel("missing/model")).thenReturn(Optional.empty());
        assertThrows(dev.ramtin.aiplatform.exception.ResourceNotFoundException.class, () -> service.deactivatePricing("missing/model"));
        verify(repository, never()).save(any());
    }
}
