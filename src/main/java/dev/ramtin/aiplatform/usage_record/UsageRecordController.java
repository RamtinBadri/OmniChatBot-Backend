package dev.ramtin.aiplatform.usage_record;

import dev.ramtin.aiplatform.security.CustomUserDetails;
import dev.ramtin.aiplatform.usage_record.service.UsageRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageRecordController {
    private final UsageRecordService usageRecordService;

    @GetMapping
    public Page<UsageRecord> getUsageHistory(@AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        return usageRecordService.getUsageHistory(userDetails.getUserId(), pageable);
    }
}