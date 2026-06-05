package com.iviet.ivshs.service.role;

import com.iviet.ivshs.dto.client.AssignGroupsToClientDto;
import com.iviet.ivshs.dto.client.UnassignGroupsFromClientDto;
import com.iviet.ivshs.dto.role.BatchAddFunctionsToGroupDto;
import com.iviet.ivshs.dto.role.BatchRemoveFunctionsFromGroupDto;
import com.iviet.ivshs.dto.role.ToggleGroupFunctionsDto;
import com.iviet.ivshs.dto.system.BatchOperationResultDto;

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
