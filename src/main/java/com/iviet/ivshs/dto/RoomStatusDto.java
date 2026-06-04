package com.iviet.ivshs.dto;

import java.util.List;

public record RoomStatusDto(
    RoomDto room,
    Double avgTempC,
    EnergyMetricDto energyMetric,
    List<Object> devices
) {}
