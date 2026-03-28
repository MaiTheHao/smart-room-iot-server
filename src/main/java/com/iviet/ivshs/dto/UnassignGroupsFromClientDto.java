package com.iviet.ivshs.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnassignGroupsFromClientDto {
    
    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotEmpty(message = "Group IDs list cannot be empty")
    private List<Long> groupIds;
}
