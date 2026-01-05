package com.iviet.ivshs.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSysFunctionDtoV1 {
    
    @Size(min = 1, max = 100, message = "Function name must be between 1 and 100 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String langCode;
}
