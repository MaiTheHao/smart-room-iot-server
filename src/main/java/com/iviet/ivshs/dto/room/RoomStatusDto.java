package com.iviet.ivshs.dto.room;

import java.util.List;

import com.iviet.ivshs.dto.metric.EnergyMetricDto;

public record RoomStatusDto(
                RoomDto room,
                Double avgTempC,
                EnergyMetricDto energyMetric,
                List<Object> devices) {
}
