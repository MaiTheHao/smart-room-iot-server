package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.*;

/**
 * Service quản lý SysRole - mapping giữa Group và Function
 */
public interface SysRoleServiceV1 {
    
    /**
     * Batch add functions vào group
     * @return BatchOperationResultDtoV1 với thông tin kết quả
     */
    BatchOperationResultDto addFunctionsToGroup(BatchAddFunctionsToGroupDto dto);

    /**
     * Batch remove functions khỏi group
     * @return BatchOperationResultDtoV1 với thông tin kết quả
     */
    BatchOperationResultDto removeFunctionsFromGroup(BatchRemoveFunctionsFromGroupDto dto);

    /**
     * Toggle functions cho group (add/remove dựa vào map)
     * @return BatchOperationResultDtoV1 với thông tin kết quả
     */
    BatchOperationResultDto toggleGroupFunctions(ToggleGroupFunctionsDto dto);

    /**
     * Add một function vào group
     */
    void addFunctionToGroup(Long groupId, String functionCode);

    /**
     * Remove một function khỏi group
     */
    void removeFunctionFromGroup(Long groupId, String functionCode);

    /**
     * Assign groups cho client
     * @return BatchOperationResultDtoV1 với thông tin kết quả
     */
    BatchOperationResultDto assignGroupsToClient(AssignGroupsToClientDto dto);

    /**
     * Unassign group khỏi client
     */
    void unassignGroupFromClient(Long clientId, Long groupId);

    /**
     * Kiểm tra xem Group có Function hay không
     */
    boolean hasFunction(Long groupId, String functionCode);
}
