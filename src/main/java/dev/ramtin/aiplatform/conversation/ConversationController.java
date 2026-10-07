package dev.ramtin.aiplatform.conversation;

import dev.ramtin.aiplatform.conversation.dto.ConversationDTO;
import dev.ramtin.aiplatform.conversation.dto.CreateConversationRequest;
import dev.ramtin.aiplatform.conversation.dto.UpdateConversationRequest;
import dev.ramtin.aiplatform.conversation.service.ConversationService;
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
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> getConversationById(@PathVariable String conversationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();
        ConversationDTO conversationDTO = conversationService.getConversationById(conversationId, userId);
        return ResponseEntity.ok(conversationDTO);
    }

    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestBody CreateConversationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String userId = userDetails.getId();

        ConversationDTO conversationDTO = conversationService
                .createConversation(userId, request.getTitle(), request.getModel());
        return ResponseEntity.status(HttpStatus.CREATED).body(conversationDTO);
    }

    @PutMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> updateConversation(
            @PathVariable String conversationId, @Valid @RequestBody UpdateConversationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();
        ConversationDTO conversationDTO = conversationService.updateConversation(
                        conversationId,
                        userId,
                        request.getTitle());
        return ResponseEntity.ok(conversationDTO);
    }

    @PatchMapping("/{conversationId}/archive")
    public ResponseEntity<Void> archiveConversation(@PathVariable String conversationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();

        conversationService.archiveConversation(conversationId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ConversationDTO>> getConversations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ConversationStatus status,
            Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();

        Page<ConversationDTO> conversations = conversationService
                .getConversations(userId, search, status, pageable);
        return ResponseEntity.ok(conversations);
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();

        conversationService.deleteConversation(conversationId, userId);
        return ResponseEntity.noContent().build();
    }
}