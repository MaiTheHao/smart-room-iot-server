package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.shared.enumeration.AlertNamespace;
import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

/**
 * DTO input cho CRUD AlertConfig. Dùng bởi POST /api/v1/alert-configs và PUT /api/v1/alert-configs/{id}.
 */
@Builder
public record CreateAlertConfigDto(Long id,

                @NotNull(message = "Namespace is required") AlertNamespace namespace,

                @NotBlank(message = "Alert code is required") String alertCode,

                @NotBlank(message = "Source ID is required") String sourceId,

                @NotBlank(message = "Alert name is required") String alertName,

                @NotNull(message = "Severity is required") Severity severity,

                @NotNull(message = "Recipient groups are required") List<String> recipientGroupCodes,

                @NotNull(message = "Channels are required") List<String> channels,

                @NotBlank(message = "Message template is required") String messageTemplate,

                @NotNull @Min(0) Integer cooldownMinutes) {
}
