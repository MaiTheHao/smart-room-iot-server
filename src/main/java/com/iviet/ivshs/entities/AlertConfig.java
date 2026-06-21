package com.iviet.ivshs.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.base.BaseAuditEntity;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Cấu hình Alert đa hình (Polymorphic Alert Config).
 * Thay thế hoàn toàn RuleActionAlert.
 * Không có FK cứng vào Rule hay bất kỳ entity cụ thể nào.
 * Ba trường (namespace + alertCode + sourceId) là composite unique key.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "alert_config", uniqueConstraints = {
    @UniqueConstraint(
        name = "idx_alert_config_polymorphic",
        columnNames = {"namespace", "alert_code", "source_id"}
    )
})
public class AlertConfig extends BaseAuditEntity {

    /**
     * Phân vùng nghiệp vụ: RULE, GATEWAY, SYSTEM.
     * Giúp UI gom nhóm và lọc hiển thị.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "namespace", nullable = false, length = 50)
    private AlertNamespace namespace;

    /**
     * Mã lỗi cụ thể trong namespace.
     * Ví dụ: SENSOR_VIOLATION (namespace RULE), OFFLINE (namespace GATEWAY).
     */
    @Column(name = "alert_code", nullable = false, length = 100)
    private String alertCode;

    /**
     * ID dạng String của entity phát sinh lỗi.
     * Nếu là Rule ID 4 → "4". Nếu là Gateway ID 20 → "20".
     * Không dùng FK vật lý để tránh coupling.
     */
    @Column(name = "source_id", nullable = false, length = 256)
    private String sourceId;

    /** Tên hiển thị cảnh báo — dùng làm title trong push notification. */
    @Column(name = "alert_name", nullable = false, length = 256)
    private String alertName;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 50)
    private Severity severity;

    /**
     * Mảng JSON các kênh gửi thông báo.
     * Ví dụ: ["PUSH", "EMAIL"]
     */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "channels")
    private JsonNode channels;

    /**
     * Template nội dung thông báo.
     * Hỗ trợ dynamic interpolation: "Nhiệt độ {{value}}°C vượt ngưỡng {{threshold}}°C"
     */
    @Column(name = "message_template", nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;

    /**
     * Thời gian tối thiểu (phút) giữa 2 lần kích hoạt.
     * 0 = không có cooldown.
     */
    @Column(name = "cooldown_minutes", nullable = false)
    @Builder.Default
    private Integer cooldownMinutes = 0;
}
