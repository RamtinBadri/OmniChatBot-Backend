package dev.ramtin.aiplatform.omniroute.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmniRouteModelsResponse {
    private List<OmniRouteModel> data;
}
