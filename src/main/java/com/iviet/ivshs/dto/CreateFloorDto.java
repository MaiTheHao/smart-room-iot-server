package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Floor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateFloorDto(
    @NotBlank(message = "Floor name is required")
    @Size(min = 1, max = 100, message = "Floor name must be between 1 and 100 characters")
    String name,

    @NotBlank(message = "Floor code is required")
    @Size(max = 256, message = "Floor code must not exceed 256 characters")
    String code,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description,
    
    @NotNull(message = "Floor level is required")
    Integer level,

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    String langCode
) {
    public static Floor toEntity(CreateFloorDto dto) {
        Floor floor = new Floor();
        floor.setCode(dto.code());
        floor.setLevel(dto.level());
        return floor;
    }
}