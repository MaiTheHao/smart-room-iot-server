package com.iviet.ivshs.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private TimeUtil() {
	}

	/**
	 * Get current date time
	 * @return String
	 */
	public static String getCurrentDateTimeNow() {
		return LocalDateTime.now().format(DATE_TIME_FORMATTER);
	}

	/**
	 * Parse formatted timestamp string to Instant with custom formatter
	 * @param formattedTimestamp the formatted timestamp string
	 * @param formatter the DateTimeFormatter to use
	 * @return Instant, or null if formattedTimestamp is null or invalid
	 */
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

	/**
	 * Format Instant to string using default formatter
	 * @param instant Instant to format
	 * @return formatted string
	 */
	public static String format(Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).format(DATE_TIME_FORMATTER);
	}
}
