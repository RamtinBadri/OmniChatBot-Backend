package dev.ramtin.aiplatform.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageExchangeDTO {
    private MessageDTO userMessage;
    private MessageDTO assistantMessage;
}