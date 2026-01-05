package com.iviet.ivshs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO để toggle (add/remove) functions cho group
 * Key: functionCode
 * Value: true = add, false = remove
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleGroupFunctionsDtoV1 {
    
    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Function toggles map is required")
    private Map<String, Boolean> functionToggles; 
}
