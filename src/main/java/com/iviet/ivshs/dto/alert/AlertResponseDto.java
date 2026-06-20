package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;

import java.time.Instant;

/**
 * Response DTO cho một AlertInstance.
 * Được trả về bởi GET /api/v1/alerts và GET /api/v1/alerts/{id}.
 * Dùng Java record (immutable, tự có constructor, equals, hashCode, toString).
 */
public record AlertResponseDto(
        Long id,
        Long ruleId,
        String ruleName,
        String title,
        String body,
        Severity severity,
        AlertStatus status,
        Instant triggeredAt,
        Instant acknowledgedAt,
        Long acknowledgedById,
        String acknowledgedByUsername,
        Instant resolvedAt,
        Long resolvedById,
        String resolvedByUsername
) {
    /**
     * Static factory từ entity AlertInstance.
     * QUAN TRỌNG: Các relation rule, acknowledgedBy, resolvedBy phải được
     * load trong transaction trước khi gọi hàm này để tránh LazyInitializationException.
     */
    public static AlertResponseDto from(AlertInstance alert) {
        return new AlertResponseDto(
                alert.getId(),
                alert.getAlertConfig().getRule().getId(),
                alert.getAlertConfig().getRule().getName(),
                alert.getTitle(),
                alert.getBody(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getTriggeredAt(),
                alert.getAcknowledgedAt(),
                alert.getAcknowledgedBy() != null ? alert.getAcknowledgedBy().getId()       : null,
                alert.getAcknowledgedBy() != null ? alert.getAcknowledgedBy().getUsername() : null,
                alert.getResolvedAt(),
                alert.getResolvedBy() != null ? alert.getResolvedBy().getId()       : null,
                alert.getResolvedBy() != null ? alert.getResolvedBy().getUsername() : null
        );
    }
}
