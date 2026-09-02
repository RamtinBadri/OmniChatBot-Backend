package dev.ramtin.aiplatform.message;

import dev.ramtin.aiplatform.message.dto.MessageDTO;
import dev.ramtin.aiplatform.message.dto.SendMessageRequest;
import dev.ramtin.aiplatform.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageDTO> createMessage(@PathVariable String conversationId,
                                                    @Valid @RequestBody SendMessageRequest request) {
        MessageDTO messageDTO = messageService.createMessage(conversationId, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(messageDTO);
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<MessageDTO> getMessageById(@PathVariable String conversationId,
                                                     @PathVariable String messageId) {
        MessageDTO messageDTO = messageService.getMessageById(messageId);
        return ResponseEntity.ok(messageDTO);
    }

    @GetMapping
    public ResponseEntity<Page<MessageDTO>> getMessages(@PathVariable String conversationId,
            Pageable pageable) {
        Page<MessageDTO> messages = messageService.getMessages(conversationId, pageable);
        return ResponseEntity.ok(messages);
    }
}