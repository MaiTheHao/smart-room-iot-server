package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseAuditEntity;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Đại diện cho một sự kiện alert được kích hoạt.
 * Mỗi lần Rule khớp điều kiện (và không trong cooldown), một AlertInstance được tạo mới.
 * Vòng đời: ACTIVE → ACKNOWLEDGED → RESOLVED
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alert_instance", indexes = {
    @Index(name = "idx_alert_instance_config_id", columnList = "alert_config_id"),
    @Index(name = "idx_alert_instance_status",      columnList = "status"),
    @Index(name = "idx_alert_instance_status_time", columnList = "status, triggered_at")
})
public class AlertInstance extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_config_id", nullable = false)
    private RuleActionAlert alertConfig;

    /** Title push notification. */
    @Column(name = "title", nullable = false, length = 256)
    private String title;

    /** Body push notification / nội dung mô tả cảnh báo. */
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 50)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AlertStatus status;

    /** Thời điểm UTC khi alert được kích hoạt lần đầu. */
    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    /** Thời điểm user xác nhận. Null nếu chưa được xác nhận. */
    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    /** Client đã xác nhận alert. Null nếu chưa xác nhận. FK → client.id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by")
    private Client acknowledgedBy;

    /** Thời điểm alert được giải quyết. Null nếu chưa resolved. */
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /**
     * Client đã resolve. Null = hệ thống tự động resolve (auto_resolve = true).
     * Nếu có giá trị = user thủ công resolve qua API.
     * FK → client.id (ON DELETE SET NULL)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private Client resolvedBy;

    /**
     * Tập hợp tất cả Client nhận được notification của alert này.
     * Được dùng bởi RBAC: G_USER query join bảng alert_recipient để lấy "My Alerts".
     * Được populate lúc tạo alert từ RuleActionAlert.recipientGroups.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "alert_recipient",
        joinColumns = @JoinColumn(name = "alert_id"),
        inverseJoinColumns = @JoinColumn(name = "client_id")
    )
    @Builder.Default
    private Set<Client> recipients = new HashSet<>();
}
