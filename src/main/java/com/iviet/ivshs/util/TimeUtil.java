package com.iviet.ivshs.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private TimeUtil() {
	}

	public static String getCurrentDateTimeNow() {
		return LocalDateTime.now().format(DATE_TIME_FORMATTER);
	}

	public static Instant parseToInstant(String formattedTimestamp, DateTimeFormatter formatter) {
		if (formattedTimestamp == null || formattedTimestamp.trim().isEmpty()) {
			return null;
		}
		try {
			LocalDateTime localDateTime = LocalDateTime.parse(formattedTimestamp, formatter);
			return localDateTime.toInstant(ZoneOffset.UTC);
		} catch (Exception e) {
			System.err.println("Error parsing timestamp '" + formattedTimestamp + "': " + e.getMessage());
			return null;
		}
	}

	public static String format(Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).format(DATE_TIME_FORMATTER);
	}
}
