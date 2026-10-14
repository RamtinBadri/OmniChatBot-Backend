package dev.ramtin.aiplatform.omniroute.service;

import dev.ramtin.aiplatform.config.OmniRouteProperties;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteModelsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class OmniRouteClientImplTest {
    @Test
    void getModels_sendsAuthenticatedRequestAndMapsModelList() throws Exception {
        OmniRouteProperties properties = new OmniRouteProperties();
        properties.setBaseUrl("http://localhost:20128");
        properties.setApiKey("test-key");

        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OmniRouteClientImpl client = new OmniRouteClientImpl(properties, builder);
        server.expect(requestTo(new URI("http://localhost:20128/v1/models")))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andRespond(withSuccess("{\"object\":\"list\",\"data\":[{\"id\":\"provider/model-a\",\"object\":\"model\",\"created\":1791971996,\"owned_by\":\"combo\",\"permission\":[],\"root\":\"provider/model-a\",\"parent\":null,\"context_length\":1000000}]}", MediaType.APPLICATION_JSON));

        OmniRouteModelsResponse response = client.getModels();

        assertEquals("provider/model-a", response.getData().get(0).getId());
        assertEquals("provider/model-a", response.getData().get(0).getName());
        server.verify();
    }
}
