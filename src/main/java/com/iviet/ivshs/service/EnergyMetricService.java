package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.enumeration.EnergyMetricCategory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EnergyMetricService {

    /**
     * Get history of energy metrics for a device.
     * Max range: 1 year.
     *
     * @param category LIGHT, FAN, AIR_CONDITION
     * @param targetId Device ID (lightId, fanId, acId)
     */
    List<EnergyMetricDto> getHistory(EnergyMetricCategory category, Long targetId, Instant from, Instant to);

    /**
     * Get the most recent energy metric for a device.
     * Only supported for LIGHT, FAN, AIR_CONDITION.
     *
     * @param category LIGHT, FAN, or AIR_CONDITION
     * @param targetId Device ID
     */
    Optional<EnergyMetricDto> getNewest(EnergyMetricCategory category, Long targetId);

    /**
     * Fetch energy telemetry from all gateway RSPI clients for all active LIGHT/FAN/AC devices
     * and persist to energy_metrics table. ROOM category is skipped for now.
     * Called every 5 minutes by the Quartz scheduler.
     */
    void fetchFromGateways();

    /**
     * Reset the PZEM-004T energy registers on all gateways via POST.
     * If the response is not 2xx, retries exactly once.
     * Called daily at 00:00 by the Quartz scheduler.
     */
    void resetGateways();
}
