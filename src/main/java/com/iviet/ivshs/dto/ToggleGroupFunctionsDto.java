package com.iviet.ivshs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleGroupFunctionsDto {
    
    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Function toggles map is required")
    private Map<String, Boolean> functionToggles; 
}
