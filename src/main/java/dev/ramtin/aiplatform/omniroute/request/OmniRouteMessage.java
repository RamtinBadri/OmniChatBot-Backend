package dev.ramtin.aiplatform.omniroute.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmniRouteMessage {
    private String role;
    private String content;
}