package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.EnergyMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Client-facing API for energy metric history and newest data.
 * Implements Interface 4.2 from doc/others/tasks.md.
 *
 * <p>Supported device domains and their category mapping:
 * <ul>
 *   <li>{@code lights}          → {@code LIGHT}
 *   <li>{@code fans}            → {@code FAN}
 *   <li>{@code air-conditions}  → {@code AC}
 * </ul>
 *
 * <p>ROOM category is NOT served here — use the existing
 * {@link PowerConsumptionValueController} for room-level data.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EnergyMetricController {

    private static final Map<String, String> DOMAIN_TO_CATEGORY = Map.of(
        "lights",          "LIGHT",
        "fans",            "FAN",
        "air-conditions",  "AC"
    );

    private final EnergyMetricService energyMetricService;

    /**
     * GET /api/v1/{device-domain}/{deviceId}/power-consumption
     *
     * <p>Mode 1 — History: ?from=&to=  (max 1 year range)
     * <p>Mode 2 — Newest: ?newest=true
     */
    @GetMapping("/{device-domain}/{deviceId}/power-consumption")
    public ResponseEntity<ApiResponse<?>> getPowerConsumption(
        @PathVariable("device-domain") String deviceDomain,
        @PathVariable("deviceId") Long deviceId,
        @RequestParam(name = "from", required = false) Instant from,
        @RequestParam(name = "to",   required = false) Instant to,
        @RequestParam(name = "newest", required = false, defaultValue = "false") boolean newest
    ) {
        String category = resolveCategory(deviceDomain);

        if (newest) {
            EnergyMetricDto result = energyMetricService.getNewest(category, deviceId)
                .orElseThrow(() -> new NotFoundException(
                    "No energy metric found for " + deviceDomain + " id=" + deviceId));
            return ResponseEntity.ok(ApiResponse.ok(result));
        }

        if (from == null || to == null) {
            throw new BadRequestException("Either 'newest=true' or both 'from' and 'to' parameters are required");
        }

        List<EnergyMetricDto> result = energyMetricService.getHistory(category, deviceId, from, to);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    private String resolveCategory(String deviceDomain) {
        String category = DOMAIN_TO_CATEGORY.get(deviceDomain);
        if (category == null) {
            throw new BadRequestException(
                "Unsupported device domain: '" + deviceDomain + "'. Supported: lights, fans, air-conditions");
        }
        return category;
    }
}
