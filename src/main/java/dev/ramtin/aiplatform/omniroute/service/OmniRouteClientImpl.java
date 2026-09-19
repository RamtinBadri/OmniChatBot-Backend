package dev.ramtin.aiplatform.omniroute.service;

import dev.ramtin.aiplatform.config.OmniRouteProperties;
import dev.ramtin.aiplatform.exception.AIProviderException;
import dev.ramtin.aiplatform.omniroute.request.OmniRouteMessage;
import dev.ramtin.aiplatform.omniroute.request.OmniRouteRequest;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteResponse;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Service
public class OmniRouteClientImpl implements OmniRouteClient {
    private final RestClient restClient;
    private final OmniRouteProperties properties;

    public OmniRouteClientImpl(OmniRouteProperties properties) {
        this.properties = properties;

        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30)).build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory).build();
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