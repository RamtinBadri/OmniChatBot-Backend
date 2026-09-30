package dev.ramtin.aiplatform.admin;

import dev.ramtin.aiplatform.subscription.Subscription;
import dev.ramtin.aiplatform.subscription.SubscriptionStatus;
import dev.ramtin.aiplatform.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<Page<Subscription>> getSubscriptions(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String planId,
            @RequestParam(required = false) SubscriptionStatus status,
            Pageable pageable) {
        Page<Subscription> subscriptions = subscriptionService.getSubscriptions(userId, planId, status, pageable);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<Subscription> getSubscriptionById(@PathVariable String subscriptionId) {
        Subscription subscription = subscriptionService.getSubscriptionById(subscriptionId);
        return ResponseEntity.ok(subscription);
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> cancelSubscription(@PathVariable String subscriptionId) {
        subscriptionService.cancelSubscription(subscriptionId);
        return ResponseEntity.noContent().build();
    }
}