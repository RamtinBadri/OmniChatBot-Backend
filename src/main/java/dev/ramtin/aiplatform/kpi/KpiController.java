package dev.ramtin.aiplatform.kpi;

import dev.ramtin.aiplatform.kpi.dto.KpiSummaryDTO;
import dev.ramtin.aiplatform.kpi.dto.KpiUsageDTO;
import dev.ramtin.aiplatform.kpi.service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/kpi")
@RequiredArgsConstructor
public class KpiController {
    private final KpiService kpiService;

    @GetMapping("/summary")
    public ResponseEntity<KpiSummaryDTO> getSummary() {
        return ResponseEntity.ok(kpiService.getSummary());
    }

    @GetMapping("/usage")
    public ResponseEntity<List<KpiUsageDTO>> getUsage(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();
        return ResponseEntity.ok(kpiService.getUsage(fromDateTime, toDateTime));
    }
}