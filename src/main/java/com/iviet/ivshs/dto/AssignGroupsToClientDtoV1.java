package com.iviet.ivshs.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO để assign groups cho client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignGroupsToClientDtoV1 {
    
    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotEmpty(message = "Group IDs list cannot be empty")
    private List<Long> groupIds;
}
