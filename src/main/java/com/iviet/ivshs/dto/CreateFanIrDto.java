package com.iviet.ivshs.dto;

import java.util.HashSet;

import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanIr;
import com.iviet.ivshs.entities.FanLan;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorState;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.FanType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFanIrDto(
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
    FanType type,

    @NotNull(message = "Mode is required for IR fan")
    ActuatorMode mode,

    @NotNull(message = "Speed is required for IR fan")
    @Min(value = 0, message = "Speed must be at least 0")
    @Max(value = 9999, message = "Speed must be at most 9999")
    Integer speed,

    @NotNull(message = "Swing is required for IR fan")
    ActuatorSwing swing,

    @NotNull(message = "Light is required for IR fan")
    ActuatorState light
) implements CreateFanDto {

    @Override
    public Fan toEntity() {
        FanIr fanIr = new FanIr();
        fanIr.setMode(mode);
        fanIr.setSpeed(speed);
        fanIr.setSwing(swing);
        fanIr.setLight(light);

        fanIr.setNaturalId(naturalId);
        fanIr.setIsActive(isActive != null ? isActive : false);
        fanIr.setPower(power != null ? power : ActuatorPower.OFF);

        HashSet<FanLan> translations = new HashSet<>();
        var fanLan = new FanLan();
        fanLan.setName(name);
        fanLan.setDescription(description);
        fanLan.setLangCode(langCode);
        fanLan.setOwner(fanIr);
        translations.add(fanLan);
        fanIr.setTranslations(translations);

        return fanIr;
    }
}
