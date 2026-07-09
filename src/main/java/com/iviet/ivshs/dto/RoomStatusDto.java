package com.iviet.ivshs.dto;

import java.util.List;

import com.iviet.ivshs.dto.EnergyMetricDto;

public record RoomStatusDto(
                RoomDto room,
                Double avgTempC,
                EnergyMetricDto energyMetric,
                List<Object> devices) {
}
