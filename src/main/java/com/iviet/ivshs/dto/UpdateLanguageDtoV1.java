package com.iviet.ivshs.dto;

import lombok.Builder;
import jakarta.validation.constraints.Size;

@Builder
public record UpdateLanguageDtoV1(
    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    String code,

    @Size(min = 1, max = 100, message = "Language name must be between 1 and 100 characters")
    String name,

    @Size(max = 255, message = "Description must not exceed 255 characters")
    String description
){}
