package com.iviet.ivshs.dto;

import java.util.HashSet;

import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.entities.LightLan;
import com.iviet.ivshs.enumeration.ActuatorPower;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record CreateLightDto(
    @NotBlank(message = "Light name is required")
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

    @Min(0) @Max(100)
    Integer level,

    ActuatorPower power,

    @Size(max = 10)
    String langCode
) {
    public Light toEntity() {
        var light = new Light();
        light.setNaturalId(this.naturalId);
        light.setIsActive(this.isActive != null ? this.isActive : false);
        light.setPower(this.power != null ? this.power : ActuatorPower.OFF);
        light.setLevel(this.level != null ? this.level : 0);

        HashSet<LightLan> translations = new HashSet<>();
        var lightLan = new LightLan();
        lightLan.setName(this.name);
        lightLan.setDescription(this.description);
        lightLan.setLangCode(this.langCode);
        lightLan.setOwner(light);
        translations.add(lightLan);
        light.setTranslations(translations);
        
        return light;
    }
}