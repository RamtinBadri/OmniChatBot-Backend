package dev.ramtin.aiplatform.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Data
@Validated
@ConfigurationProperties(prefix = "omniroute")
public class OmniRouteProperties {
    private String baseUrl;

    private String apiKey;

    // The JDK request factory schedules timeouts in milliseconds.
    @NotNull
    @DurationMin(millis = 1)
    @DurationMax(millis = Long.MAX_VALUE)
    private Duration readTimeout = Duration.ofSeconds(120);
}
