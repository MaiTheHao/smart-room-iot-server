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
    BatchOperationResultDtoV1 addFunctionsToGroup(BatchAddFunctionsToGroupDtoV1 dto);

    /**
     * Batch remove functions khỏi group
     * @return BatchOperationResultDtoV1 với thông tin kết quả
     */
    BatchOperationResultDtoV1 removeFunctionsFromGroup(BatchRemoveFunctionsFromGroupDtoV1 dto);

    /**
     * Toggle functions cho group (add/remove dựa vào map)
     * @return BatchOperationResultDtoV1 với thông tin kết quả
     */
    BatchOperationResultDtoV1 toggleGroupFunctions(ToggleGroupFunctionsDtoV1 dto);

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
    BatchOperationResultDtoV1 assignGroupsToClient(AssignGroupsToClientDtoV1 dto);

    /**
     * Unassign group khỏi client
     */
    void unassignGroupFromClient(Long clientId, Long groupId);

    /**
     * Kiểm tra xem Group có Function hay không
     */
    boolean hasFunction(Long groupId, String functionCode);
}
