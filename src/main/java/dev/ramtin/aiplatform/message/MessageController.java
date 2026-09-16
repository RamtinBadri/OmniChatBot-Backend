package dev.ramtin.aiplatform.message;

import dev.ramtin.aiplatform.message.dto.MessageDTO;
import dev.ramtin.aiplatform.message.dto.MessageExchangeDTO;
import dev.ramtin.aiplatform.message.dto.SendMessageRequest;
import dev.ramtin.aiplatform.message.service.MessageService;
import dev.ramtin.aiplatform.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageExchangeDTO> createMessage(@PathVariable String conversationId, @Valid @RequestBody SendMessageRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();

        MessageExchangeDTO messageExchangeDTO = messageService.createMessage(
                conversationId,
                request.getContent(),
                userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(messageExchangeDTO);
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<MessageDTO> getMessageById(@PathVariable String conversationId, @PathVariable String messageId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();

        MessageDTO messageDTO = messageService.getMessageById(conversationId, messageId, userId);
        return ResponseEntity.ok(messageDTO);
    }

    @GetMapping
    public ResponseEntity<Page<MessageDTO>> getMessages(@PathVariable String conversationId, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();
        Page<MessageDTO> messages = messageService.getMessages(conversationId, userId, pageable);
        return ResponseEntity.ok(messages);
    }
}