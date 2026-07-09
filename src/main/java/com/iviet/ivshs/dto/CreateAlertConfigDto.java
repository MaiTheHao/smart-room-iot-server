package com.iviet.ivshs.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iviet.ivshs.entities.AlertConfig;
import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateAlertConfigDto(Long id,

    @NotNull(message = "Namespace is required") AlertNamespace namespace,

    @NotBlank(message = "Alert code is required") String alertCode,

    @NotBlank(message = "Source ID is required") String sourceId,

    @NotBlank(message = "Alert name is required") String alertName,

    @NotNull(message = "Severity is required") Severity severity,

    List<String> recipientGroupCodes,

    List<String> channels,

    @NotBlank(message = "Message template is required") String messageTemplate,

    @NotNull @Min(0) Integer cooldownMinutes

) {
  public AlertConfig toEntity(ObjectMapper objectMapper) {
    return AlertConfig.builder().namespace(namespace()).alertCode(alertCode()).sourceId(sourceId())
        .alertName(alertName()).severity(severity()).channels(objectMapper.valueToTree(channels()))
        .messageTemplate(messageTemplate()).cooldownMinutes(cooldownMinutes()).build();
  }
}
