package com.iviet.ivshs.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO để batch remove functions khỏi group
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchRemoveFunctionsFromGroupDto {
    
    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotEmpty(message = "Function codes list cannot be empty")
    private List<String> functionCodes;
}
