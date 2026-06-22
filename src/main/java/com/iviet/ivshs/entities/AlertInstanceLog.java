package com.iviet.ivshs.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.shared.enumeration.AlertActionType;
import com.iviet.ivshs.shared.enumeration.AlertActorType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Append-only audit log cho mọi thay đổi trạng thái của alert incident. Polymorphic actor: USER (con người) | SYSTEM
 * (tự động) | EXTERNAL_API. Không kế thừa BaseAuditEntity (tự quản lý created_at).
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alert_instance_log", indexes = {
        @Index(name = "idx_ail_alert_created_at", columnList = "alert_id, created_at") })
public class AlertInstanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private AlertInstance alert;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private AlertActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 50)
    private AlertActorType actorType;

    /**
     * Mã định danh linh hoạt. Nếu actorType = USER: lưu clientId dạng String ("15"). Nếu actorType = SYSTEM: lưu tên
     * process ("RULE_ENGINE").
     */
    @Column(name = "actor_id", nullable = false, length = 256)
    private String actorId;

    /** Nội dung ngắn gọn hiển thị trên timeline UI. */
    @Column(name = "message", nullable = false, length = 512)
    private String message;

    /** JSON payload chứa dữ liệu telemetry vi phạm. Schema-less. */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "payload")
    private JsonNode payload;

    /** Thời điểm chính xác đến mili-giây. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
