package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.Severity;

import java.time.Instant;
import java.util.List;

public record AlertConfigResponseDto(
        Long id,
        AlertNamespace namespace,
        String alertCode,
        String sourceId,
        String alertName,
        Severity severity,
        List<String> recipientGroupCodes,
        List<String> channels,
        String messageTemplate,
        Integer cooldownMinutes,
        Boolean autoResolve,
        Instant createdAt,
        Instant updatedAt
) {
    public static AlertConfigResponseDto from(AlertConfig config, List<String> recipientGroupCodes, List<String> channels) {
        return new AlertConfigResponseDto(
                config.getId(),
                config.getNamespace(),
                config.getAlertCode(),
                config.getSourceId(),
                config.getAlertName(),
                config.getSeverity(),
                recipientGroupCodes,
                channels,
                config.getMessageTemplate(),
                config.getCooldownMinutes(),
                config.getAutoResolve(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}
