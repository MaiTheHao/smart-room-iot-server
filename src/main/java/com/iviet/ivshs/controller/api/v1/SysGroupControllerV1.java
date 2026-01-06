package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.SysGroupServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class SysGroupControllerV1 {

    private final SysGroupServiceV1 groupService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<SysGroupDto>>> getGroups(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(groupService.getList(page, size)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SysGroupDto>>> getAllGroups() {
        return ResponseEntity.ok(ApiResponse.ok(groupService.getAll()));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<SysGroupDto>> getGroupById(
            @PathVariable(name = "groupId") Long groupId) {
        
        return ResponseEntity.ok(ApiResponse.ok(groupService.getById(groupId)));
    }

    @GetMapping("/code/{groupCode}")
    public ResponseEntity<ApiResponse<SysGroupDto>> getGroupByCode(
            @PathVariable(name = "groupCode") String groupCode) {
        
        return ResponseEntity.ok(ApiResponse.ok(groupService.getByCode(groupCode)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SysGroupDto>> createGroup(
            @RequestBody @Valid CreateSysGroupDto request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(groupService.create(request)));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<SysGroupDto>> updateGroup(
            @PathVariable(name = "groupId") Long groupId,
            @RequestBody @Valid UpdateSysGroupDto request) {
        
        return ResponseEntity.ok(ApiResponse.ok(groupService.update(groupId, request)));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable(name = "groupId") Long groupId) {
        
        groupService.delete(groupId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Group deleted successfully"));
    }

    @GetMapping("/{groupId}/functions")
    public ResponseEntity<ApiResponse<List<SysFunctionDto>>> getFunctionsByGroup(
            @PathVariable(name = "groupId") Long groupId) {
        
        return ResponseEntity.ok(ApiResponse.ok(groupService.getFunctionsByGroupId(groupId)));
    }

    @GetMapping("/{groupId}/functions/paginated")
    public ResponseEntity<ApiResponse<PaginatedResponse<SysFunctionDto>>> getFunctionsByGroupPaginated(
            @PathVariable(name = "groupId") Long groupId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(
            groupService.getFunctionsByGroupId(groupId, page, size)
        ));
    }

    @GetMapping("/{groupId}/clients")
    public ResponseEntity<ApiResponse<List<ClientDto>>> getClientsByGroup(
            @PathVariable(name = "groupId") Long groupId) {
        
        return ResponseEntity.ok(ApiResponse.ok(groupService.getClientsByGroupId(groupId)));
    }

    @GetMapping("/{groupId}/clients/paginated")
    public ResponseEntity<ApiResponse<PaginatedResponse<ClientDto>>> getClientsByGroupPaginated(
            @PathVariable(name = "groupId") Long groupId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(
            groupService.getClientsByGroupId(groupId, page, size)
        ));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countGroups() {
        return ResponseEntity.ok(ApiResponse.ok(groupService.count()));
    }

    @GetMapping("/{groupId}/functions/count")
    public ResponseEntity<ApiResponse<Long>> countFunctionsByGroup(
            @PathVariable(name = "groupId") Long groupId) {
        
        return ResponseEntity.ok(ApiResponse.ok(groupService.countFunctionsByGroupId(groupId)));
    }

    @GetMapping("/{groupId}/clients/count")
    public ResponseEntity<ApiResponse<Long>> countClientsByGroup(
            @PathVariable(name = "groupId") Long groupId) {
        
        return ResponseEntity.ok(ApiResponse.ok(groupService.countClientsByGroupId(groupId)));
    }
}
