package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;

import java.time.Instant;

/**
 * Response DTO cho một AlertInstance (sự cố alert). Trả về bởi GET /api/v1/alerts và GET /api/v1/alerts/{id}.
 */
public record AlertInstanceDto(

    Long id,

    Long alertConfigId,

    String alertConfigName,

    String namespace,

    String sourceId,

    String title,

    String body,

    Severity severity,

    AlertStatus status,

    Integer triggerCount,

    Instant triggeredAt,

    Instant acknowledgedAt,

    Long acknowledgedById,

    String acknowledgedByUsername,

    Instant resolvedAt,

    Long resolvedById,

    String resolvedByUsername

) {
  public static AlertInstanceDto from(AlertInstance alert) {
    return new AlertInstanceDto(alert.getId(), alert.getAlertConfig().getId(), alert.getAlertConfig().getAlertName(),
        alert.getAlertConfig().getNamespace().name(), alert.getAlertConfig().getSourceId(), alert.getTitle(),
        alert.getBody(), alert.getSeverity(), alert.getStatus(), alert.getTriggerCount(), alert.getTriggeredAt(),
        alert.getAcknowledgedAt(), alert.getAcknowledgedBy() != null ? alert.getAcknowledgedBy().getId() : null,
        alert.getAcknowledgedBy() != null ? alert.getAcknowledgedBy().getUsername() : null, alert.getResolvedAt(),
        alert.getResolvedBy() != null ? alert.getResolvedBy().getId() : null,
        alert.getResolvedBy() != null ? alert.getResolvedBy().getUsername() : null);
  }
}
