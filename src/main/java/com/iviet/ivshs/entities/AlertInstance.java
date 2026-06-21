package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseAuditEntity;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Đại diện cho một sự kiện alert được kích hoạt (Alert Incident/Event). Tên bảng: alert_instance. Vòng đời: ACTIVE → ACKNOWLEDGED → RESOLVED.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alert_instance", indexes = {
        @Index(name = "idx_alert_instance_config_id", columnList = "alert_config_id"),
        @Index(name = "idx_alert_instance_status", columnList = "status"),
        @Index(name = "idx_alert_instance_status_time", columnList = "status, triggered_at"),
        @Index(name = "idx_alert_instance_triggered_at", columnList = "triggered_at") })
public class AlertInstance extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_config_id", nullable = false)
    private AlertConfig alertConfig;

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 50)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AlertStatus status;

    /** Thời điểm alert được kích hoạt lần đầu. */
    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    /**
     * Số lần Rule/nguồn match lại trong thời gian cooldown. Mỗi lần RE_TRIGGERED tăng 1. Bắt đầu = 1 (lần kích hoạt
     * đầu).
     */
    @Column(name = "trigger_count", nullable = false)
    @Builder.Default
    private Integer triggerCount = 1;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by")
    private Client acknowledgedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /**
     * Client đã resolve thủ công. NULL = hệ thống tự động resolve.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private Client resolvedBy;
}
