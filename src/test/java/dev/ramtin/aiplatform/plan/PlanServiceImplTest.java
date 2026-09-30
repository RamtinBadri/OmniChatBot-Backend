package dev.ramtin.aiplatform.plan;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.plan.service.PlanServiceImpl;
import dev.ramtin.aiplatform.subscription.Subscription;
import dev.ramtin.aiplatform.subscription.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanServiceImplTest {
    @Mock
    private PlanRepository planRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    private PlanServiceImpl planService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        planService = new PlanServiceImpl(planRepository, subscriptionRepository);
    }

    @Test
    void shouldCreatePlanSuccessfully() {
        Plan plan = Plan.builder()
                .name("Starter")
                .price(BigDecimal.valueOf(10))
                .duration(30)
                .creditLimit(BigDecimal.valueOf(5))
                .active(true)
                .build();

        when(planRepository.save(plan))
                .thenReturn(plan);

        Plan result = planService.createPlan(plan);

        assertNotNull(result);
        assertEquals("Starter", result.getName());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(planRepository).save(plan);
    }

    @Test
    void shouldRejectPlanWithNegativePrice() {
        Plan plan = Plan.builder()
                .name("Starter")
                .price(BigDecimal.valueOf(-10))
                .duration(30)
                .creditLimit(BigDecimal.valueOf(5))
                .active(true)
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> planService.createPlan(plan)
        );

        verify(planRepository, never()).save(any());
    }

    @Test
    void shouldGetPlanSuccessfully() {
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

        Plan result = planService.getPlanById("plan-1");

        assertEquals("plan-1", result.getId());
        assertEquals("Starter", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenPlanDoesNotExist() {
        when(planRepository.findById("plan-1"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> planService.getPlanById("plan-1")
        );
    }

    @Test
    void shouldDeactivatePlanSuccessfully() {
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

        planService.deactivatePlan("plan-1");

        assertFalse(plan.isActive());
        assertNotNull(plan.getUpdatedAt());

        verify(planRepository).save(plan);
    }

    @Test
    void shouldGetSubscriptionsByPlanSuccessfully() {
        Plan plan = Plan.builder()
                .id("plan-1")
                .name("Starter")
                .price(BigDecimal.TEN)
                .duration(30)
                .creditLimit(BigDecimal.valueOf(5))
                .active(true)
                .build();

        Subscription subscription = Subscription.builder()
                .id("subscription-1")
                .userId("user-1")
                .planId("plan-1")
                .build();

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Subscription> page = new PageImpl<>(List.of(subscription), pageable, 1);

        when(planRepository.findById("plan-1")).thenReturn(Optional.of(plan));

        when(subscriptionRepository.findByPlanId("plan-1", pageable)).thenReturn(page);

        Page<Subscription> result = planService.getSubscriptionsByPlan("plan-1", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("subscription-1", result.getContent().get(0).getId());

        verify(planRepository).findById("plan-1");
        verify(subscriptionRepository).findByPlanId("plan-1", pageable);
    }

    @Test
    void shouldThrowExceptionWhenGettingSubscriptionsForNonExistingPlan() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(planRepository.findById("plan-1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> planService.getSubscriptionsByPlan("plan-1", pageable));

        verify(subscriptionRepository, never()).findByPlanId(anyString(), any());
    }
}