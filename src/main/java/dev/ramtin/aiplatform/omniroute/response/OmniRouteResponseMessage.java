package dev.ramtin.aiplatform.omniroute.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OmniRouteResponseMessage {
    private String role;

    private String content;
}