package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Language;
import lombok.Builder;

@Builder
public record LanguageDto(
    Long id,
    String code,
    String name,
    String description
) {
    public static LanguageDto from(Language entity) {
        if (entity == null) {
            return null;
        }
        return new LanguageDto(
            entity.getId(),
            entity.getCode(),
            entity.getName(),
            entity.getDescription()
        );
    }
}