package com.iviet.ivshs.dto;

import java.time.Instant;
import java.util.List;

import com.iviet.ivshs.entities.Automation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomationDto {

    private Long id;
    private String name;
    private String cronExpression;
    private Boolean isActive;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public static AutomationDto fromEntity(Automation entity) {
        if (entity == null) return null;
        AutomationDto dto = new AutomationDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCronExpression(entity.getCronExpression());
        dto.setIsActive(entity.getIsActive());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public static List<AutomationDto> fromEntities(List<Automation> entities) {
        if (entities == null) return List.of();
        return entities.stream()
            .map(AutomationDto::fromEntity)
            .toList();
    }

    public static Automation toEntity(AutomationDto dto) {
        if (dto == null) return null;
        Automation automation = new Automation();
        automation.setId(dto.getId());
        automation.setName(dto.getName());
        automation.setCronExpression(dto.getCronExpression());
        automation.setIsActive(dto.getIsActive());
        automation.setDescription(dto.getDescription());
        return automation;
    }
}
