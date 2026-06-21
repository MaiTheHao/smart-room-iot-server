package com.iviet.ivshs.shared.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum NotificationChannel {

    PUSH("PUSH"), // Firebase Cloud Messaging (FCM)
    EMAIL("EMAIL"), // Email via SMTP / JavaMailSender
    SMS("SMS"); // SMS Gateway (Twilio, VietGuys...)

    private final String value;

    private static final Map<String, NotificationChannel> LOOKUP_MAP;

    private static final String VALID_VALUES;

    static {
        LOOKUP_MAP = Stream.of(values()).collect(
                Collectors.toUnmodifiableMap(channel -> channel.value.toLowerCase(Locale.ROOT), channel -> channel));

        VALID_VALUES = Stream.of(values()).map(NotificationChannel::getValue)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    NotificationChannel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NotificationChannel fromValue(String value) {
        NotificationChannel channel = null;
        if (value != null) {
            channel = LOOKUP_MAP.get(value.toLowerCase(Locale.ROOT));
        }

        if (channel == null) {
            throw new IllegalArgumentException(
                    "Unknown notification channel: '" + value + "'. Valid values: " + VALID_VALUES);
        }
        return channel;
    }

    public static Optional<NotificationChannel> tryFromValue(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(LOOKUP_MAP.get(value.toLowerCase(Locale.ROOT)));
    }

    @Override
    public String toString() {
        return value;
    }
}
