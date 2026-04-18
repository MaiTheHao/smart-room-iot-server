package com.iviet.ivshs.enumeration;

import java.time.Duration;
import java.time.Instant;

import lombok.Getter;

@Getter
public enum TelemetryTimeGroup {
    FIVE_MINUTES(5),
    FIFTEEN_MINUTES(15),
    THIRTY_MINUTES(30),
    HOUR(60),
    THREE_HOURS(180),
    SIX_HOURS(360),
    TWELVE_HOURS(720),
    DAY(1440),
    TWO_DAYS(2880),
    WEEK(10080);

    private final int divisor;

    TelemetryTimeGroup(int divisor) {
        this.divisor = divisor;
    }

    public static int getDivisorForRange(Instant startedAt, Instant endedAt) {
        if (startedAt == null || endedAt == null) {
            return FIVE_MINUTES.getDivisor(); // Default to 5 minutes
        }

        long durationMinutes = Duration.between(startedAt, endedAt).abs().toMinutes();

        if (durationMinutes <= 3 * 60) {
            // <= 3 hours
            return FIVE_MINUTES.getDivisor();
        } else if (durationMinutes <= 12 * 60) {
            // <= 12 hours
            return FIFTEEN_MINUTES.getDivisor();
        } else if (durationMinutes <= 24 * 60) {
            // <= 1 day
            return THIRTY_MINUTES.getDivisor();
        } else if (durationMinutes <= 3 * 24 * 60) {
            // <= 3 days
            return HOUR.getDivisor();
        } else if (durationMinutes <= 7 * 24 * 60) {
            // <= 7 days
            return THREE_HOURS.getDivisor();
        } else if (durationMinutes <= 14 * 24 * 60) {
            // <= 14 days
            return SIX_HOURS.getDivisor();
        } else if (durationMinutes <= 30 * 24 * 60) {
            // <= 1 month
            return TWELVE_HOURS.getDivisor();
        } else if (durationMinutes <= 90 * 24 * 60) {
            // <= 3 months
            return DAY.getDivisor();
        } else if (durationMinutes <= 180 * 24 * 60) {
            // <= 6 months
            return TWO_DAYS.getDivisor();
        } else {
            // > 6 months
            return WEEK.getDivisor();
        }
    }
}
