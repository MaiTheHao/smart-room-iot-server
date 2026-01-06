package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record LanguageDto(
    Long id,
    String code,
    String name,
    String description
){}