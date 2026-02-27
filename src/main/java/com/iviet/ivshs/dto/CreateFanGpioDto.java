package com.iviet.ivshs.dto;

import java.util.HashSet;

import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanGpio;
import com.iviet.ivshs.entities.FanLan;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.FanType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFanGpioDto(
    @NotBlank(message = "Device name is required")
    @Size(min = 1, max = 100)
    String name,

    @NotBlank(message = "Natural ID is required")
    String naturalId,

    @Size(max = 255)
    String description,

    Boolean isActive,

    @NotNull(message = "Room ID is required")
    Long roomId,

    Long deviceControlId,

    @Size(max = 10)
    String langCode,

    ActuatorPower power,

    @NotNull(message = "Fan type is required")
    FanType type
) implements CreateFanDto {

    @Override
    public Fan toEntity() {
        FanGpio fanGpio = new FanGpio();

        fanGpio.setNaturalId(naturalId);
        fanGpio.setIsActive(isActive != null ? isActive : false);
        fanGpio.setPower(power != null ? power : ActuatorPower.OFF);

        HashSet<FanLan> translations = new HashSet<>();
        var fanLan = new FanLan();
        fanLan.setName(name);
        fanLan.setDescription(description);
        fanLan.setLangCode(langCode);
        fanLan.setOwner(fanGpio);
        translations.add(fanLan);
        fanGpio.setTranslations(translations);

        return fanGpio;
    }
}
