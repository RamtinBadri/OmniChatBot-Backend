package dev.ramtin.aiplatform.subscription;

import dev.ramtin.aiplatform.security.CustomUserDetails;
import dev.ramtin.aiplatform.subscription.dto.CreateSubscriptionRequest;
import dev.ramtin.aiplatform.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<Subscription> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();

        Subscription subscription = subscriptionService.createSubscription(userId, request.getPlanId());

        return ResponseEntity.ok(subscription);
    }
}