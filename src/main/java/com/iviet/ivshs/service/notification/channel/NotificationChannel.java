package com.iviet.ivshs.service.notification.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enum type-safe cho các kênh gửi thông báo.
 * Thay thế hoàn toàn magic string "PUSH", "EMAIL", "SMS".
 *
 * Ưu điểm so với String:
 * - Compile-time safety: typo "PUCH" → compile error thay vì silent failure
 * - IDE autocomplete và refactor hoạt động đúng
 * - Centralized: thêm kênh mới = thêm 1 entry ở đây + tạo 1 @Component Strategy
 *
 * Jackson @JsonValue/@JsonCreator: serialization qua REST API trả về "PUSH" thay vì ordinal.
 * JPA column channels (JSON) trong DB vẫn lưu string: ["PUSH", "EMAIL"].
 */
public enum NotificationChannel {

    PUSH("PUSH"),    // Firebase Cloud Messaging (FCM)
    EMAIL("EMAIL"),  // Email via SMTP / JavaMailSender
    SMS("SMS");      // SMS Gateway (Twilio, VietGuys...)

    private final String value;

    NotificationChannel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Parse từ String (case-insensitive). Dùng khi đọc JSON từ DB column `channels`.
     * Ví dụ: "PUSH" → NotificationChannel.PUSH, "push" → NotificationChannel.PUSH
     *
     * @throws IllegalArgumentException nếu value không tương ứng với bất kỳ channel nào.
     */
    @JsonCreator
    public static NotificationChannel fromValue(String value) {
        return Arrays.stream(values())
                .filter(c -> c.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown notification channel: '" + value + "'. Valid values: " +
                        Arrays.toString(values())));
    }

    /** Parse an toàn, trả về Optional.empty() thay vì throw nếu không tìm thấy. */
    public static Optional<NotificationChannel> tryFromValue(String value) {
        return Arrays.stream(values())
                .filter(c -> c.value.equalsIgnoreCase(value))
                .findFirst();
    }

    @Override
    public String toString() {
        return value;
    }
}
