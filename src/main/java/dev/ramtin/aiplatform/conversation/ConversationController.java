package dev.ramtin.aiplatform.conversation;

import dev.ramtin.aiplatform.conversation.dto.ConversationDTO;
import dev.ramtin.aiplatform.conversation.dto.CreateConversationRequest;
import dev.ramtin.aiplatform.conversation.dto.UpdateConversationRequest;
import dev.ramtin.aiplatform.conversation.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> getConversationById(@PathVariable String conversationId) {
        ConversationDTO conversationDTO = conversationService.getConversationById(conversationId);
        return ResponseEntity.ok(conversationDTO);
    }

    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestBody CreateConversationRequest request) {
        String userId = "dummy-id";
        ConversationDTO conversationDTO = conversationService
                .createConversation(userId, request.getTitle(), request.getModel());
        return ResponseEntity.status(HttpStatus.CREATED).body(conversationDTO);
    }

    @PutMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> updateConversation(
            @PathVariable String conversationId, @Valid @RequestBody UpdateConversationRequest request) {
        ConversationDTO conversationDTO = conversationService
                .updateConversation(conversationId, request.getTitle());
        return ResponseEntity.ok(conversationDTO);
    }

    @PatchMapping("/{conversationId}/archive")
    public ResponseEntity<Void> archiveConversation(@PathVariable String conversationId) {
        conversationService.archiveConversation(conversationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ConversationDTO>> getConversations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean archived,
            Pageable pageable) {
        String userId = "dummy-id";
        Page<ConversationDTO> conversations = conversationService
                .getConversations(userId, search, archived, pageable);
        return ResponseEntity.ok(conversations);
    }
}