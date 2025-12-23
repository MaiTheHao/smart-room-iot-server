package com.iviet.ivshs.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateTimeFormatterUtil {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private DateTimeFormatterUtil() {
	}

	public static Instant parseToInstant(String formattedTimestamp) {
		LocalDateTime localDateTime = LocalDateTime.parse(formattedTimestamp, FORMATTER);
		return localDateTime.toInstant(ZoneOffset.UTC);
	}
}
