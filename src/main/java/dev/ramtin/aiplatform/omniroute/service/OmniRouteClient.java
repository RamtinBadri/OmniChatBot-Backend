package dev.ramtin.aiplatform.omniroute.service;

import dev.ramtin.aiplatform.omniroute.request.OmniRouteMessage;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteResponse;
import dev.ramtin.aiplatform.omniroute.response.OmniRouteModelsResponse;

import java.util.List;

public interface OmniRouteClient {
    OmniRouteResponse sendMessage(String model, List<OmniRouteMessage> messages);

    OmniRouteModelsResponse getModels();
}