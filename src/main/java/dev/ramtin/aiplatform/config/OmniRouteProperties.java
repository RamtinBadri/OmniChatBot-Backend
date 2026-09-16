package dev.ramtin.aiplatform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "omniroute")
public class OmniRouteProperties {
    private String baseUrl;

    private String apiKey;
}