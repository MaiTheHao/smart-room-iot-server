package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.SysRoleServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
public class SysRoleControllerV1 {

    private final SysRoleServiceV1 roleService;

    @PostMapping("/groups/functions/batch-add")
    public ResponseEntity<ApiResponse<BatchOperationResultDto>> batchAddFunctionsToGroup(
            @RequestBody @Valid BatchAddFunctionsToGroupDto request) {
        BatchOperationResultDto result = roleService.addFunctionsToGroup(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/groups/functions/batch-remove")
    public ResponseEntity<ApiResponse<BatchOperationResultDto>> batchRemoveFunctionsFromGroup(
            @RequestBody @Valid BatchRemoveFunctionsFromGroupDto request) {
        BatchOperationResultDto result = roleService.removeFunctionsFromGroup(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/groups/functions/toggle")
    public ResponseEntity<ApiResponse<BatchOperationResultDto>> toggleGroupFunctions(
            @RequestBody @Valid ToggleGroupFunctionsDto request) {
        BatchOperationResultDto result = roleService.toggleGroupFunctions(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/groups/{groupId}/functions/{functionCode}")
    public ResponseEntity<ApiResponse<Void>> addFunctionToGroup(
            @PathVariable(name = "groupId") Long groupId,
            @PathVariable(name = "functionCode") String functionCode) {
        roleService.addFunctionToGroup(groupId, functionCode);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, null, "Function added to group successfully"));
    }

    @DeleteMapping("/groups/{groupId}/functions/{functionCode}")
    public ResponseEntity<ApiResponse<Void>> removeFunctionFromGroup(
            @PathVariable(name = "groupId") Long groupId,
            @PathVariable(name = "functionCode") String functionCode) {
        roleService.removeFunctionFromGroup(groupId, functionCode);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Function removed from group successfully"));
    }

    @PostMapping("/clients/groups/assign")
    public ResponseEntity<ApiResponse<BatchOperationResultDto>> assignGroupsToClient(
            @RequestBody @Valid AssignGroupsToClientDto request) {
        BatchOperationResultDto result = roleService.assignGroupsToClient(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @DeleteMapping("/clients/{clientId}/groups/{groupId}")
    public ResponseEntity<ApiResponse<Void>> unassignGroupFromClient(
            @PathVariable(name = "clientId") Long clientId,
            @PathVariable(name = "groupId") Long groupId) {
        roleService.unassignGroupFromClient(clientId, groupId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Group unassigned from client successfully"));
    }

    @GetMapping("/groups/{groupId}/functions/{functionCode}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkGroupHasFunction(
            @PathVariable(name = "groupId") Long groupId,
            @PathVariable(name = "functionCode") String functionCode) {
        boolean hasFunction = roleService.hasFunction(groupId, functionCode);
        return ResponseEntity.ok(ApiResponse.ok(hasFunction));
    }
}
