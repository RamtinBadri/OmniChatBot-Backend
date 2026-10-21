package dev.ramtin.aiplatform.omniroute.service;

import dev.ramtin.aiplatform.config.OmniRouteProperties;
import dev.ramtin.aiplatform.exception.AIProviderException;
import dev.ramtin.aiplatform.omniroute.request.OmniRouteMessage;
import dev.ramtin.aiplatform.omniroute.request.OmniRouteRequest;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteResponse;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteModelsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OmniRouteClientImpl implements OmniRouteClient {
    private final RestClient restClient;
    private final OmniRouteProperties properties;

    @Autowired
    public OmniRouteClientImpl(OmniRouteProperties properties) {
        this(properties, RestClient.builder().baseUrl(properties.getBaseUrl())
                .requestFactory(createRequestFactory(properties.getReadTimeout())));
    }

    OmniRouteClientImpl(OmniRouteProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
    }

    private static ClientHttpRequestFactory createRequestFactory(Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        return requestFactory;
    }

    @Override
    public OmniRouteModelsResponse getModels() {
        try {
            Map<String, Object> response = restClient.get().uri("/v1/models")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (response == null || !(response.get("data") instanceof List<?> models)) {
                throw new AIProviderException("AI provider returned an invalid model-list response");
            }

            List<dev.ramtin.aiplatform.omniroute.response.OmniRouteModel> parsedModels = new ArrayList<>();
            for (Object item : models) {
                if (item instanceof Map<?, ?> model && model.get("id") instanceof String id) {
                    Object name = model.get("name");
                    parsedModels.add(new dev.ramtin.aiplatform.omniroute.response.OmniRouteModel(
                            id, name instanceof String ? (String) name : id));
                }
            }
            return new OmniRouteModelsResponse(parsedModels);
        } catch (RestClientException exception) {
            throw new AIProviderException("AI provider model-list request failed", exception);
        }
    }

    @Override
    public OmniRouteResponse sendMessage(String model, List<OmniRouteMessage> messages) {
        OmniRouteRequest request = OmniRouteRequest.builder().model(model).messages(messages).build();
        try {
            return restClient.post().uri("/v1/chat/completions")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON).body(request).retrieve()
                    .body(OmniRouteResponse.class);
        } catch (RestClientException exception) {
            throw new AIProviderException("AI provider request failed", exception);
        }
    }
}