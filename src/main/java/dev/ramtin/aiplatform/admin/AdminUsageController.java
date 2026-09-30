package dev.ramtin.aiplatform.admin;

import dev.ramtin.aiplatform.usage_record.UsageRecord;
import dev.ramtin.aiplatform.usage_record.service.UsageRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/usage")
@RequiredArgsConstructor
public class AdminUsageController {
    private final UsageRecordService usageRecordService;

    @GetMapping
    public ResponseEntity<Page<UsageRecord>> getUsageRecords(@RequestParam(required = false) String userId,
            @RequestParam(required = false) String model, Pageable pageable) {
        Page<UsageRecord> usageRecords = usageRecordService.getUsageRecords(userId, model, pageable);
        return ResponseEntity.ok(usageRecords);
    }
}