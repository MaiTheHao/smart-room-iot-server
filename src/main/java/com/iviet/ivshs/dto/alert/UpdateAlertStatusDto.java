package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.shared.enumeration.AlertStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAlertStatusDto(
    @NotNull(message = "Status is required")
    AlertStatus status
) {}
