package com.iviet.ivshs.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.base.BaseAuditEntity;
import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Cấu hình alert cho một Rule cụ thể (quan hệ 1:1 với Rule).
 * Nếu bản ghi này tồn tại cho một rule_id, hệ thống sẽ bật cơ chế alert khi Rule khớp.
 * Khi Rule bị xóa → bản ghi này bị xóa theo (ON DELETE CASCADE từ DB + CascadeType.ALL từ Rule.java).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rule_action_alert", indexes = {
    @Index(name = "idx_rule_action_alert_rule_id", columnList = "rule_id")
})
public class RuleActionAlert extends BaseAuditEntity {

    /**
     * Rule mà config này thuộc về.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    /** Tên hiển thị của cảnh báo — dùng làm title trong push notification. */
    @Column(name = "alert_name", nullable = false, length = 256)
    private String alertName;

    /** Mức độ nghiêm trọng. Lưu dạng STRING ("INFO", "WARNING", "CRITICAL"). */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 50)
    private Severity severity;

    /**
     * Mảng JSON các group code nhận alert.
     * Ví dụ: ["G_ADMIN", "G_MAINTENANCE"]
     * JsonNodeConverter @autoApply = true — không cần @Convert thủ công.
     */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "recipient_groups")
    private JsonNode recipientGroups;

    /**
     * Mảng JSON các kênh gửi thông báo.
     * Ví dụ: ["PUSH", "EMAIL"]
     * JsonNodeConverter @autoApply = true — không cần @Convert thủ công.
     */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "channels")
    private JsonNode channels;

    /**
     * Template nội dung thông báo.
     * Ví dụ: "Nhiệt độ phòng 101 đạt 42°C, vượt ngưỡng cho phép 35°C."
     */
    @Column(name = "message_template", nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;

    /**
     * Thời gian tối thiểu (phút) giữa 2 lần kích hoạt alert liên tiếp cùng Rule.
     * 0 = không có cooldown (kích hoạt mỗi lần Rule match).
     */
    @Column(name = "cooldown_minutes", nullable = false)
    private Integer cooldownMinutes = 0;

    /**
     * Nếu true: khi điều kiện Rule không còn thỏa mãn, hệ thống tự động
     * chuyển alert đang ACTIVE/ACKNOWLEDGED sang trạng thái RESOLVED.
     */
    @Column(name = "auto_resolve", nullable = false)
    private Boolean autoResolve = false;
}
