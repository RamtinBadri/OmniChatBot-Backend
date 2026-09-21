package dev.ramtin.aiplatform.plan;

import dev.ramtin.aiplatform.exception.ResourceNotFoundException;
import dev.ramtin.aiplatform.plan.service.PlanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanServiceImplTest {
    @Mock
    private PlanRepository planRepository;
    private PlanServiceImpl planService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        planService = new PlanServiceImpl(planRepository);
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
}