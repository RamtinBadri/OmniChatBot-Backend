package dev.ramtin.aiplatform;

import dev.ramtin.aiplatform.config.AIProperties;
import dev.ramtin.aiplatform.config.OmniRouteProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({OmniRouteProperties.class, AIProperties.class})
public class AiPlatformApplication {
	public static void main(String[] args) {
		SpringApplication.run(AiPlatformApplication.class, args);
	}
}