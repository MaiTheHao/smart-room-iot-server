package com.iviet.ivshs.dto.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record UpdateAlertConfigDto(
    @NotBlank(message = "Alert name is required") String alertName,

    @NotNull(message = "Severity is required") Severity severity,

    @NotEmpty(message = "Recipient groups are required and must not be empty") List<String> recipientGroupCodes,

    @NotEmpty(message = "Channels are required and must not be empty") List<String> channels,

    @NotBlank(message = "Message template is required") String messageTemplate,

    @NotNull @Min(0) Integer cooldownMinutes
) {
    public void updateEntity(AlertConfig config, ObjectMapper objectMapper) {
        config.setAlertName(alertName());
        config.setSeverity(severity());
        config.setChannels(objectMapper.valueToTree(channels()));
        config.setMessageTemplate(messageTemplate());
        config.setCooldownMinutes(cooldownMinutes());
    }
}
