package dev.ramtin.aiplatform.model_pricing;

import dev.ramtin.aiplatform.config.DefaultPricingProperties;
import dev.ramtin.aiplatform.model_pricing.service.ModelPricingServiceImpl;
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
class ModelPricingServiceImplTest {
    @Mock
    private ModelPricingRepository modelPricingRepository;

    private ModelPricingServiceImpl modelPricingService;

    @BeforeEach
    void setUp() {
        DefaultPricingProperties defaultPricingProperties = new DefaultPricingProperties();

        defaultPricingProperties.setDefaultInputPricePerMillion(
                new BigDecimal("0.50")
        );

        defaultPricingProperties.setDefaultOutputPricePerMillion(
                new BigDecimal("2.00")
        );

        modelPricingService =
                new ModelPricingServiceImpl(
                        modelPricingRepository,
                        defaultPricingProperties
                );
    }

    @Test
    void getPricing_whenSpecificActivePricingExists_returnsSpecificPricing() {
        ModelPricing specificPricing =
                ModelPricing.builder()
                        .model("glm-5")
                        .inputPricePerMillion(
                                new BigDecimal("0.30")
                        )
                        .outputPricePerMillion(
                                new BigDecimal("1.50")
                        )
                        .active(true)
                        .build();

        when(modelPricingRepository.findByModelAndActiveTrue("glm-5"))
                .thenReturn(Optional.of(specificPricing));

        ModelPricing result =
                modelPricingService.getPricing("glm-5");

        assertSame(
                specificPricing,
                result
        );

        verify(modelPricingRepository)
                .findByModelAndActiveTrue("glm-5");

        verify(modelPricingRepository, never())
                .save(any(ModelPricing.class));
    }

    @Test
    void getPricing_whenSpecificPricingDoesNotExist_returnsDefaultPricing() {
        when(modelPricingRepository.findByModelAndActiveTrue("glm-5"))
                .thenReturn(Optional.empty());

        ModelPricing result =
                modelPricingService.getPricing("glm-5");

        assertNotNull(result);

        assertEquals(
                "glm-5",
                result.getModel()
        );

        assertEquals(
                0,
                new BigDecimal("0.50")
                        .compareTo(
                                result.getInputPricePerMillion()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("2.00")
                        .compareTo(
                                result.getOutputPricePerMillion()
                        )
        );

        assertTrue(result.isActive());

        verify(modelPricingRepository)
                .findByModelAndActiveTrue("glm-5");

        verify(modelPricingRepository, never())
                .save(any(ModelPricing.class));
    }

    @Test
    void getPricing_whenSpecificPricingIsInactive_returnsDefaultPricing() {
        when(modelPricingRepository.findByModelAndActiveTrue("glm-5"))
                .thenReturn(Optional.empty());

        ModelPricing result =
                modelPricingService.getPricing("glm-5");

        assertNotNull(result);

        assertEquals(
                "glm-5",
                result.getModel()
        );

        assertEquals(
                0,
                new BigDecimal("0.50")
                        .compareTo(
                                result.getInputPricePerMillion()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("2.00")
                        .compareTo(
                                result.getOutputPricePerMillion()
                        )
        );

        assertTrue(result.isActive());

        verify(modelPricingRepository)
                .findByModelAndActiveTrue("glm-5");

        verify(modelPricingRepository, never())
                .save(any(ModelPricing.class));
    }
}