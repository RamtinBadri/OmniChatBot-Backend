package dev.ramtin.aiplatform.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    @NotNull
    private List<@NotBlank @Pattern(regexp = "[^*]+", message = "Wildcard origins are not allowed") String> allowedOrigins =
            List.of("http://localhost:5173");
}
