package dev.ramtin.aiplatform.omniroute.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmniRouteChoice {
    private int index;

    @JsonProperty("finish_reason")
    private String finishReason;

    private OmniRouteResponseMessage message;
}