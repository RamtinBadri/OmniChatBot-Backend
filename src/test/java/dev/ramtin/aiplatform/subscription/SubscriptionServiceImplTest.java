package dev.ramtin.aiplatform.subscription;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.plan.Plan;
import dev.ramtin.aiplatform.plan.PlanRepository;
import dev.ramtin.aiplatform.subscription.service.SubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionServiceImplTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private PlanRepository planRepository;
    private SubscriptionServiceImpl subscriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscriptionService = new SubscriptionServiceImpl(subscriptionRepository, planRepository);
    }

    @Test
    void shouldCreateSubscriptionSuccessfully() {
        Plan plan = Plan.builder()
                .id("plan-1")
                .name("Starter")
                .price(BigDecimal.TEN)
                .duration(30)
                .creditLimit(BigDecimal.valueOf(5))
                .active(true)
                .build();

        when(planRepository.findById("plan-1"))
                .thenReturn(Optional.of(plan));

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime before = LocalDateTime.now();

        Subscription result =
                subscriptionService.createSubscription(
                        "user-1",
                        "plan-1"
                );

        LocalDateTime after = LocalDateTime.now();

        assertNotNull(result);

        assertEquals("user-1", result.getUserId());
        assertEquals("plan-1", result.getPlanId());

        assertEquals(
                BigDecimal.TEN,
                result.getPricePaid()
        );

        assertEquals(
                BigDecimal.valueOf(5),
                result.getRemainingCredit()
        );

        assertEquals(
                SubscriptionStatus.ACTIVE,
                result.getStatus()
        );

        assertNotNull(result.getStartDate());
        assertNotNull(result.getEndDate());

        assertTrue(
                !result.getStartDate().isBefore(before)
                        && !result.getStartDate().isAfter(after)
        );

        assertEquals(
                result.getStartDate().plusDays(30),
                result.getEndDate()
        );

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void shouldRejectSubscriptionForInactivePlan() {
        Plan plan = Plan.builder()
                .id("plan-1")
                .name("Starter")
                .price(BigDecimal.TEN)
                .duration(30)
                .creditLimit(BigDecimal.valueOf(5))
                .active(false)
                .build();

        when(planRepository.findById("plan-1"))
                .thenReturn(Optional.of(plan));

        assertThrows(
                IllegalStateException.class,
                () -> subscriptionService.createSubscription(
                        "user-1",
                        "plan-1"
                )
        );

        verify(subscriptionRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenPlanDoesNotExist() {
        when(planRepository.findById("plan-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> subscriptionService.createSubscription(
                        "user-1",
                        "plan-1"
                )
        );

        verify(subscriptionRepository, never())
                .save(any());
    }

    @Test
    void shouldGetSubscriptionSuccessfully() {
        Subscription subscription = Subscription.builder()
                .id("sub-1")
                .userId("user-1")
                .planId("plan-1")
                .status(SubscriptionStatus.ACTIVE)
                .remainingCredit(BigDecimal.valueOf(5))
                .build();

        when(subscriptionRepository.findById("sub-1"))
                .thenReturn(Optional.of(subscription));

        Subscription result =
                subscriptionService.getSubscriptionById("sub-1");

        assertEquals("sub-1", result.getId());
        assertEquals("user-1", result.getUserId());
    }

    @Test
    void shouldThrowExceptionWhenSubscriptionDoesNotExist() {
        when(subscriptionRepository.findById("sub-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> subscriptionService.getSubscriptionById("sub-1")
        );
    }

    @Test
    void shouldCancelSubscriptionSuccessfully() {
        Subscription subscription = Subscription.builder()
                .id("sub-1")
                .userId("user-1")
                .planId("plan-1")
                .status(SubscriptionStatus.ACTIVE)
                .remainingCredit(BigDecimal.valueOf(5))
                .build();

        when(subscriptionRepository.findById("sub-1")).thenReturn(Optional.of(subscription));

        subscriptionService.cancelSubscription("sub-1");

        assertEquals(
                SubscriptionStatus.CANCELLED,
                subscription.getStatus()
        );

        verify(subscriptionRepository).save(subscription);
    }
}