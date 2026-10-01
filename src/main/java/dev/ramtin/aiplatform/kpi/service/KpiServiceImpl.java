package dev.ramtin.aiplatform.kpi.service;

import dev.ramtin.aiplatform.ai.request.AIRequestStatus;
import dev.ramtin.aiplatform.ai.request.repository.AIRequestRepository;
import dev.ramtin.aiplatform.kpi.dto.KpiSummaryDTO;
import dev.ramtin.aiplatform.kpi.dto.KpiUsageDTO;
import dev.ramtin.aiplatform.kpi.dto.TransactionKpiSummary;
import dev.ramtin.aiplatform.kpi.dto.UsageKpiSummary;
import dev.ramtin.aiplatform.subscription.repository.SubscriptionRepository;
import dev.ramtin.aiplatform.transaction.repository.TransactionRepository;
import dev.ramtin.aiplatform.usage_record.repository.UsageRecordRepository;
import dev.ramtin.aiplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KpiServiceImpl implements KpiService {
    private final UserRepository userRepository;
    private final AIRequestRepository aiRequestRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public KpiSummaryDTO getSummary() {
        long totalUsers = userRepository.countUsers();
        long activeUsers = userRepository.countActiveUsers();
        long subscribedUsers = subscriptionRepository.countActiveSubscribers();
        long totalRequests = aiRequestRepository.count();
        long successfulRequests = aiRequestRepository.countByStatus(AIRequestStatus.SUCCESS);

        long failedRequests = aiRequestRepository.countByStatusIn(
                        List.of(
                                AIRequestStatus.FAILED,
                                AIRequestStatus.TIMEOUT,
                                AIRequestStatus.RATE_LIMITED,
                                AIRequestStatus.PROVIDER_ERROR
                        )
                );

        UsageKpiSummary usage = usageRecordRepository.getUsageKpiSummary();
        TransactionKpiSummary transaction = transactionRepository.getTransactionKpiSummary();
        BigDecimal totalRevenue = transaction.getSubscriptionRevenue().add(transaction.getCreditPurchaseRevenue());

        return KpiSummaryDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .subscribedUsers(subscribedUsers)

                .totalRequests(totalRequests)
                .successfulRequests(successfulRequests)
                .failedRequests(failedRequests)

                .totalInputTokens(usage.getTotalInputTokens())
                .totalOutputTokens(usage.getTotalOutputTokens())
                .totalTokens(usage.getTotalTokens())

                .totalProviderCost(usage.getTotalProviderCost())
                .totalChargedAmount(usage.getTotalChargedAmount())

                .subscriptionRevenue(transaction.getSubscriptionRevenue())
                .creditPurchaseRevenue(transaction.getCreditPurchaseRevenue())
                .totalRevenue(totalRevenue)

                .build();
    }

    @Override
    public List<KpiUsageDTO> getUsage(LocalDateTime from, LocalDateTime to) {
        List<KpiUsageDTO> requestUsage = aiRequestRepository.getDailyKpiUsage(from, to);
        List<KpiUsageDTO> recordUsage = usageRecordRepository.getDailyKpiUsage(from, to);

        Map<LocalDate, KpiUsageDTO> usageByDate = new HashMap<>();

        for (KpiUsageDTO usage : requestUsage) {
            usageByDate.put(usage.getDate(), usage);
        }

        for (KpiUsageDTO usage : recordUsage) {
            KpiUsageDTO existing = usageByDate.get(usage.getDate());
            if (existing == null) {
                existing = KpiUsageDTO.builder()
                        .date(usage.getDate())
                        .requests(0)
                        .successfulRequests(0)
                        .failedRequests(0)
                        .build();

                usageByDate.put(usage.getDate(), existing);
            }
            existing.setInputTokens(usage.getInputTokens());
            existing.setOutputTokens(usage.getOutputTokens());
            existing.setTotalTokens(usage.getTotalTokens());
            existing.setProviderCost(usage.getProviderCost());
            existing.setChargedAmount(usage.getChargedAmount());
        }
        List<KpiUsageDTO> result = new ArrayList<>(usageByDate.values());
        result.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        return result;
    }
}