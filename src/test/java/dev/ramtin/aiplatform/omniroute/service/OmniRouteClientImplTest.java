package dev.ramtin.aiplatform.omniroute.service;

import dev.ramtin.aiplatform.config.OmniRouteProperties;
import dev.ramtin.aiplatform.omniroute.request.OmniRouteMessage;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteModelsResponse;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

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

    @Test
    void sendMessage_passesComboModelIdUnchanged() throws Exception {
        OmniRouteProperties properties = new OmniRouteProperties();
        properties.setBaseUrl("http://localhost:20128");
        properties.setApiKey("test-key");

        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OmniRouteClientImpl client = new OmniRouteClientImpl(properties, builder);
        server.expect(requestTo(new URI("http://localhost:20128/v1/chat/completions")))
                .andExpect(method(POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andExpect(content().json("{\"model\":\"combo/my-combo\",\"messages\":[{\"role\":\"user\",\"content\":\"hello\"}]}", true))
                .andRespond(withSuccess("{\"id\":\"response-1\",\"model\":\"combo/my-combo\",\"choices\":[],\"usage\":{\"prompt_tokens\":0,\"completion_tokens\":0,\"total_tokens\":0}}", MediaType.APPLICATION_JSON));

        OmniRouteResponse response = client.sendMessage(
                "combo/my-combo",
                List.of(new OmniRouteMessage("user", "hello")));

        assertNotNull(response);
        assertEquals("combo/my-combo", response.getModel());
        server.verify();
    }
}
