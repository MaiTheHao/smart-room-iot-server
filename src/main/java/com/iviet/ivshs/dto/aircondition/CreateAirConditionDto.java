package com.iviet.ivshs.dto.aircondition;

import java.util.HashSet;

import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.AirConditionLan;
import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateAirConditionDto(
        @NotBlank(message = "Device name is required") @Size(min = 1, max = 100) String name,

        @NotBlank(message = "Natural ID is required") String naturalId,

        @Size(max = 255) String description,

        Boolean isActive,

        @NotNull(message = "Room ID is required") Long roomId,

        Long deviceControlId,

        @Size(max = 10) String langCode,

        ActuatorPower power,

        Integer duration,

        @Min(16) @Max(32) Integer temperature,

        ActuatorMode mode,

        @Min(1) @Max(5) Integer fanSpeed,

        ActuatorSwing swing) {
    public AirCondition toEntity() {
        var ac = new AirCondition();
        ac.setNaturalId(this.naturalId);
        ac.setIsActive(this.isActive != null ? this.isActive : false);
        ac.setPower(this.power != null ? this.power : ActuatorPower.OFF);
        ac.setDuration(this.duration);
        ac.setTemperature(this.temperature != null ? this.temperature : 24);
        ac.setMode(this.mode != null ? this.mode : ActuatorMode.AUTO);
        ac.setFanSpeed(this.fanSpeed != null ? this.fanSpeed : 1);
        ac.setSwing(this.swing != null ? this.swing : ActuatorSwing.OFF);

        HashSet<AirConditionLan> translations = new HashSet<>();
        var acLan = new AirConditionLan();
        acLan.setName(this.name);
        acLan.setDescription(this.description);
        acLan.setLangCode(this.langCode);
        acLan.setOwner(ac);
        translations.add(acLan);
        ac.setTranslations(translations);

        return ac;
    }
}