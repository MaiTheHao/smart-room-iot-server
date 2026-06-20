package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record SaveRuleActionAlertDto(
        Long id,
        @NotNull(message = "Rule ID is required") Long ruleId,
        @NotBlank(message = "Alert name is required") String alertName,
        @NotNull(message = "Severity is required") Severity severity,
        @NotNull(message = "Recipient groups are required") List<String> recipientGroups,
        @NotNull(message = "Channels are required") List<String> channels,
        @NotBlank(message = "Message template is required") String messageTemplate,
        @NotNull(message = "Cooldown minutes is required") @Min(value = 0, message = "Cooldown minutes must be at least 0") Integer cooldownMinutes,
        @NotNull(message = "Auto resolve flag is required") Boolean autoResolve
) {
}
