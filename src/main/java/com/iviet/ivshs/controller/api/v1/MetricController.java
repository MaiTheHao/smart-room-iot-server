package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.enumeration.MetricDomain;
import com.iviet.ivshs.service.MetricOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricController {

    private final MetricOrchestratorService orchestrator;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getMetrics(
            @RequestParam(name = "domain") MetricDomain domain,
            @RequestParam(name = "category") DeviceCategory category,
            @RequestParam(name = "targetId") Long targetId,
            @RequestParam(name = "latest", defaultValue = "false") boolean latest,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to
    ) {
        if (latest) {
            Object result = orchestrator.getLatest(domain, category, targetId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.ok(result));
        } else {
            List<?> result = orchestrator.getHistory(domain, category, targetId, from, to);
            return ResponseEntity.ok(ApiResponse.ok(result));
        }
    }
}
