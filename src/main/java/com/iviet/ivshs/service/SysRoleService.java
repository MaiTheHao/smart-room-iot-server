package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.*;

public interface SysRoleService {
    
    BatchOperationResultDto addFunctionsToGroup(BatchAddFunctionsToGroupDto dto);

    BatchOperationResultDto removeFunctionsFromGroup(BatchRemoveFunctionsFromGroupDto dto);

    BatchOperationResultDto toggleGroupFunctions(ToggleGroupFunctionsDto dto);

    void addFunctionToGroup(Long groupId, String functionCode);

    void removeFunctionFromGroup(Long groupId, String functionCode);

    BatchOperationResultDto assignGroupsToClient(AssignGroupsToClientDto dto);

    void unassignGroupFromClient(Long clientId, Long groupId);

    void unassignGroupsFromClient(UnassignGroupsFromClientDto dto);

    boolean hasFunction(Long groupId, String functionCode);
}
