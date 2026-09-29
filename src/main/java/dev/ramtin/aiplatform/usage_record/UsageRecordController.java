package dev.ramtin.aiplatform.usage_record;

import dev.ramtin.aiplatform.security.CustomUserDetails;
import dev.ramtin.aiplatform.usage_record.dto.UsageSummary;
import dev.ramtin.aiplatform.usage_record.service.UsageRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageRecordController {
    private final UsageRecordService usageRecordService;

    @GetMapping
    public ResponseEntity<Page<UsageRecord>> getUsageHistory(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();
        Page<UsageRecord> usageHistory = usageRecordService.getUsageHistory(userId, pageable);

        return ResponseEntity.ok(usageHistory);
    }

    @GetMapping("/summary")
    public ResponseEntity<UsageSummary> getUsageSummary() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String userId = userDetails.getId();

        UsageSummary usageSummary = usageRecordService.getUsageSummary(userId);
        return ResponseEntity.ok(usageSummary);
    }
}