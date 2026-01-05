package com.iviet.ivshs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSysFunctionDtoV1 {
    
    @NotBlank(message = "Function code is required")
    @Size(max = 256, message = "Function code must not exceed 256 characters")
    private String functionCode;

    @NotBlank(message = "Function name is required")
    @Size(min = 1, max = 100, message = "Function name must be between 1 and 100 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String langCode;
}
