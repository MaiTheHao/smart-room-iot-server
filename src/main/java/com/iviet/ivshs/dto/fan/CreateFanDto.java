package com.iviet.ivshs.dto.fan;

import java.util.HashSet;

import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanLan;
import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;
import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateFanDto(
        @NotBlank(message = "Device name is required") @Size(min = 1, max = 100) String name,

        @NotBlank(message = "Natural ID is required") String naturalId,

        @Size(max = 255) String description,

        Boolean isActive,

        @NotNull(message = "Room ID is required") Long roomId,

        Long deviceControlId,

        @Size(max = 10) String langCode,

        ActuatorPower power,

        @NotNull(message = "Fan type is required") DeviceSpecificType type,

        @Min(value = 0, message = "Duration must be positive") Integer duration,

        @Min(value = 1, message = "Speed must be at least 1") @Max(value = 5, message = "Speed must be at most 5") Integer speed,

        ActuatorMode mode,

        ActuatorSwing swing,

        ActuatorPower light) {

    public Fan toEntity() {
        var fan = new Fan();
        fan.setNaturalId(this.naturalId);
        fan.setIsActive(this.isActive != null ? this.isActive : false);
        fan.setPower(this.power != null ? this.power : ActuatorPower.OFF);
        if (this.type != null) {
            fan.setSpecificType(this.type);
        }
        fan.setDuration(this.duration);
        fan.setSpeed(this.speed != null ? this.speed : 1);
        fan.setMode(this.mode != null ? this.mode : ActuatorMode.NORMAL);
        fan.setSwing(this.swing != null ? this.swing : ActuatorSwing.OFF);
        fan.setLight(this.light != null ? this.light : ActuatorPower.OFF);

        HashSet<FanLan> translations = new HashSet<>();
        var lan = new FanLan();
        lan.setName(this.name);
        lan.setDescription(this.description);
        lan.setLangCode(this.langCode);
        lan.setOwner(fan);
        translations.add(lan);
        fan.setTranslations(translations);

        return fan;
    }
}
