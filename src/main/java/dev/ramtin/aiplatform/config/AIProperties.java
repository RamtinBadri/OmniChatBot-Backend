package dev.ramtin.aiplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class AIProperties {
    private String defaultModel;
}