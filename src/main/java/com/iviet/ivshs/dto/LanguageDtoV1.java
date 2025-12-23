package com.iviet.ivshs.dto;

import lombok.Builder;

@Builder
public record LanguageDtoV1(
    Long id,
    String code,
    String name,
    String description
){}