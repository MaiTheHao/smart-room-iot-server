package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Language;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Builder
public record CreateLanguageDto(
    @NotBlank(message = "Language code is required")
    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    String code,

    @NotBlank(message = "Language name is required")
    @Size(min = 1, max = 100, message = "Language name must be between 1 and 100 characters")
    String name,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description
) {
    public Language toEntity() {
        Language language = new Language();
        language.setCode(this.code);
        language.setName(this.name);
        language.setDescription(this.description);
        return language;
    }
}