package com.iviet.ivshs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response cho batch operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResultDtoV1 {
    
    private int successCount;
    private int failedCount;
    private int skippedCount;
    private String message;
}
