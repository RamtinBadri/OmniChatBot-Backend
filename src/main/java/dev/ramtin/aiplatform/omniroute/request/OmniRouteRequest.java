package dev.ramtin.aiplatform.omniroute.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmniRouteRequest {
    private String model;
    private List<OmniRouteMessage> messages;
}