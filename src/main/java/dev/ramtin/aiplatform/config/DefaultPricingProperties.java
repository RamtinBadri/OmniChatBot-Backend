package dev.ramtin.aiplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Data
@ConfigurationProperties(prefix = "ai.pricing")
public class DefaultPricingProperties {
    private BigDecimal defaultInputPricePerMillion;
    private BigDecimal defaultOutputPricePerMillion;
}