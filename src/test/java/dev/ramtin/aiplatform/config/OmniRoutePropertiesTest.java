package dev.ramtin.aiplatform.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OmniRoutePropertiesTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class);

    @Test
    void readTimeout_defaultsTo120Seconds() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(OmniRouteProperties.class).getReadTimeout())
                    .isEqualTo(Duration.ofSeconds(120));
        });
    }

    @Test
    void readTimeout_blankUsesDefaultLikeAnAbsentProperty() {
        contextRunner.withPropertyValues("omniroute.read-timeout=").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(OmniRouteProperties.class).getReadTimeout())
                    .isEqualTo(Duration.ofSeconds(120));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"750ms", "PT0.75S"})
    void readTimeout_bindsDuration(String value) {
        contextRunner.withPropertyValues("omniroute.read-timeout=" + value).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(OmniRouteProperties.class).getReadTimeout())
                    .isEqualTo(Duration.ofMillis(750));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"1ms", "9223372036854775807ms"})
    void readTimeout_acceptsRepresentableBounds(String value) {
        contextRunner.withPropertyValues("omniroute.read-timeout=" + value).run(context -> {
            assertThat(context).hasNotFailed();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0ms", "-1s", "PT0.000000001S", "infinite", "invalid",
            "PT9223372036854775807S"})
    void readTimeout_rejectsInvalidOrUnrepresentableValues(String value) {
        contextRunner.withPropertyValues("omniroute.read-timeout=" + value).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("read-timeout");
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(OmniRouteProperties.class)
    static class PropertiesConfiguration {
    }
}
