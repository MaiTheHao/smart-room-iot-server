package com.iviet.ivshs.dto;

import java.util.HashSet;

import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanGpio;
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
import lombok.Builder;

@Builder
public record CreateFanDto(
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

    ActuatorMode mode,

    @Min(1) @Max(5)
    Integer speed,

    ActuatorSwing swing,

    ActuatorState light
) {
    public Fan toEntity() {
        Fan fan;
        if (this.type == FanType.IR) {
            FanIr fanIr = new FanIr();
            fanIr.setMode(this.mode != null ? this.mode : ActuatorMode.AUTO);
            fanIr.setSpeed(this.speed != null ? this.speed : 1);
            fanIr.setSwing(this.swing != null ? this.swing : ActuatorSwing.OFF);
            fanIr.setLight(this.light != null ? this.light : ActuatorState.OFF);
            fan = fanIr;
        } else {
            fan = new FanGpio();
        }

        fan.setNaturalId(this.naturalId);
        fan.setIsActive(this.isActive != null ? this.isActive : false);
        fan.setPower(this.power != null ? this.power : ActuatorPower.OFF);

        HashSet<FanLan> translations = new HashSet<>();
        var fanLan = new FanLan();
        fanLan.setName(this.name);
        fanLan.setDescription(this.description);
        fanLan.setLangCode(this.langCode);
        fanLan.setOwner(fan);
        translations.add(fanLan);
        fan.setTranslations(translations);
        
        return fan;
    }
}
