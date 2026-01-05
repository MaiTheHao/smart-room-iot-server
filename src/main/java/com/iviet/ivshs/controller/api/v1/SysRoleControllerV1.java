package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.SysRoleServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller quản lý Roles (mapping Group-Function)
 * Endpoint: /api/v1/roles
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
public class SysRoleControllerV1 {

    private final SysRoleServiceV1 roleService;

    /**
     * POST /api/v1/roles/groups/functions/batch-add
     * Batch add functions vào group
     */
    @PostMapping("/groups/functions/batch-add")
    public ResponseEntity<ApiResponseV1<BatchOperationResultDtoV1>> batchAddFunctionsToGroup(
            @RequestBody @Valid BatchAddFunctionsToGroupDtoV1 request) {
        
        BatchOperationResultDtoV1 result = roleService.addFunctionsToGroup(request);
        return ResponseEntity.ok(ApiResponseV1.ok(result));
    }

    /**
     * POST /api/v1/roles/groups/functions/batch-remove
     * Batch remove functions khỏi group
     */
    @PostMapping("/groups/functions/batch-remove")
    public ResponseEntity<ApiResponseV1<BatchOperationResultDtoV1>> batchRemoveFunctionsFromGroup(
            @RequestBody @Valid BatchRemoveFunctionsFromGroupDtoV1 request) {
        
        BatchOperationResultDtoV1 result = roleService.removeFunctionsFromGroup(request);
        return ResponseEntity.ok(ApiResponseV1.ok(result));
    }

    /**
     * POST /api/v1/roles/groups/functions/toggle
     * Toggle functions cho group (add/remove dựa vào map)
     */
    @PostMapping("/groups/functions/toggle")
    public ResponseEntity<ApiResponseV1<BatchOperationResultDtoV1>> toggleGroupFunctions(
            @RequestBody @Valid ToggleGroupFunctionsDtoV1 request) {
        
        BatchOperationResultDtoV1 result = roleService.toggleGroupFunctions(request);
        return ResponseEntity.ok(ApiResponseV1.ok(result));
    }

    /**
     * POST /api/v1/roles/groups/{groupId}/functions/{functionCode}
     * Add một function vào group
     */
    @PostMapping("/groups/{groupId}/functions/{functionCode}")
    public ResponseEntity<ApiResponseV1<Void>> addFunctionToGroup(
            @PathVariable(name = "groupId") Long groupId,
            @PathVariable(name = "functionCode") String functionCode) {
        
        roleService.addFunctionToGroup(groupId, functionCode);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseV1.success(HttpStatus.CREATED, null, "Function added to group successfully"));
    }

    /**
     * DELETE /api/v1/roles/groups/{groupId}/functions/{functionCode}
     * Remove một function khỏi group
     */
    @DeleteMapping("/groups/{groupId}/functions/{functionCode}")
    public ResponseEntity<ApiResponseV1<Void>> removeFunctionFromGroup(
            @PathVariable(name = "groupId") Long groupId,
            @PathVariable(name = "functionCode") String functionCode) {
        
        roleService.removeFunctionFromGroup(groupId, functionCode);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponseV1.success(HttpStatus.NO_CONTENT, null, "Function removed from group successfully"));
    }

    /**
     * POST /api/v1/roles/clients/groups/assign
     * Assign groups cho client
     */
    @PostMapping("/clients/groups/assign")
    public ResponseEntity<ApiResponseV1<BatchOperationResultDtoV1>> assignGroupsToClient(
            @RequestBody @Valid AssignGroupsToClientDtoV1 request) {
        
        BatchOperationResultDtoV1 result = roleService.assignGroupsToClient(request);
        return ResponseEntity.ok(ApiResponseV1.ok(result));
    }

    /**
     * DELETE /api/v1/roles/clients/{clientId}/groups/{groupId}
     * Unassign group khỏi client
     */
    @DeleteMapping("/clients/{clientId}/groups/{groupId}")
    public ResponseEntity<ApiResponseV1<Void>> unassignGroupFromClient(
            @PathVariable(name = "clientId") Long clientId,
            @PathVariable(name = "groupId") Long groupId) {
        
        roleService.unassignGroupFromClient(clientId, groupId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponseV1.success(HttpStatus.NO_CONTENT, null, "Group unassigned from client successfully"));
    }

    /**
     * GET /api/v1/roles/groups/{groupId}/functions/{functionCode}/check
     * Kiểm tra xem Group có Function hay không
     */
    @GetMapping("/groups/{groupId}/functions/{functionCode}/check")
    public ResponseEntity<ApiResponseV1<Boolean>> checkGroupHasFunction(
            @PathVariable(name = "groupId") Long groupId,
            @PathVariable(name = "functionCode") String functionCode) {
        
        boolean hasFunction = roleService.hasFunction(groupId, functionCode);
        return ResponseEntity.ok(ApiResponseV1.ok(hasFunction));
    }
}
