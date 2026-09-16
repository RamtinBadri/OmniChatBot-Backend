package dev.ramtin.aiplatform.omniroute.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmniRouteUsage {
    @JsonProperty("prompt_tokens")
    private long promptTokens;

    @JsonProperty("completion_tokens")
    private long completionTokens;

    @JsonProperty("total_tokens")
    private long totalTokens;
}