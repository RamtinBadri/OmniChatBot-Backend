package dev.ramtin.aiplatform.omniroute.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmniRouteResponse {
    private String id;

    private String model;

    private List<OmniRouteChoice> choices;

    private OmniRouteUsage usage;
}