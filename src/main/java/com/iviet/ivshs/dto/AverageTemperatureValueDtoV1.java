package com.iviet.ivshs.dto;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import com.iviet.ivshs.util.TimeUtil;
import lombok.Builder;

@Builder
public record AverageTemperatureValueDtoV1(
    Instant timestamp,
    Double avgTempC
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AverageTemperatureValueDtoV1(String formattedTimestamp, Double avgTempC) {
        this(
            Optional.ofNullable(formattedTimestamp)
                    .map(s -> TimeUtil.parseToInstant(s, FORMATTER))
                    .orElseGet(Instant::now),
            avgTempC != null ? avgTempC : 0.0
        );
    }
}