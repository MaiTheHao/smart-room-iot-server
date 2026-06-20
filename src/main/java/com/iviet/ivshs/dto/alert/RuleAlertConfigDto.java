package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

/**
 * DTO input cho cấu hình alert của một Rule.
 * Không chứa {@code ruleId} vì ruleId được suy ra từ context (path variable hoặc parent Rule).
 * Dùng khi lồng trong {@link com.iviet.ivshs.dto.rule.CreateRuleDto} /
 * {@link com.iviet.ivshs.dto.rule.UpdateRuleDto}.
 *
 * <p>Cơ chế UPSERT:
 * <ul>
 *   <li>id == null → tạo mới</li>
 *   <li>id != null → cập nhật bản ghi hiện có</li>
 *   <li>Bản ghi trong DB không có trong danh sách → bị xóa (orphan removal)</li>
 * </ul>
 */
@Builder
public record RuleAlertConfigDto(
        Long id,

        @NotBlank(message = "Alert name is required")
        String alertName,

        @NotNull(message = "Severity is required")
        Severity severity,

        @NotNull(message = "Recipient groups are required")
        List<String> recipientGroups,

        @NotNull(message = "Channels are required")
        List<String> channels,

        @NotBlank(message = "Message template is required")
        String messageTemplate,

        @NotNull(message = "Cooldown minutes is required")
        @Min(value = 0, message = "Cooldown minutes must be at least 0")
        Integer cooldownMinutes,

        @NotNull(message = "Auto resolve flag is required")
        Boolean autoResolve
) {
}
