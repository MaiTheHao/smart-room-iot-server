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
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<SysGroupDtoV1>>> getGroups(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(groupService.getList(page, size)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponseV1<List<SysGroupDtoV1>>> getAllGroups() {
        return ResponseEntity.ok(ApiResponseV1.ok(groupService.getAll()));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponseV1<SysGroupDtoV1>> getGroupById(
            @PathVariable(name = "groupId") Long groupId) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(groupService.getById(groupId)));
    }

    @GetMapping("/code/{groupCode}")
    public ResponseEntity<ApiResponseV1<SysGroupDtoV1>> getGroupByCode(
            @PathVariable(name = "groupCode") String groupCode) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(groupService.getByCode(groupCode)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseV1<SysGroupDtoV1>> createGroup(
            @RequestBody @Valid CreateSysGroupDtoV1 request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseV1.created(groupService.create(request)));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponseV1<SysGroupDtoV1>> updateGroup(
            @PathVariable(name = "groupId") Long groupId,
            @RequestBody @Valid UpdateSysGroupDtoV1 request) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(groupService.update(groupId, request)));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponseV1<Void>> deleteGroup(
            @PathVariable(name = "groupId") Long groupId) {
        
        groupService.delete(groupId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponseV1.success(HttpStatus.NO_CONTENT, null, "Group deleted successfully"));
    }

    @GetMapping("/{groupId}/functions")
    public ResponseEntity<ApiResponseV1<List<SysFunctionDtoV1>>> getFunctionsByGroup(
            @PathVariable(name = "groupId") Long groupId) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(groupService.getFunctionsByGroupId(groupId)));
    }

    @GetMapping("/{groupId}/functions/paginated")
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<SysFunctionDtoV1>>> getFunctionsByGroupPaginated(
            @PathVariable(name = "groupId") Long groupId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(
            groupService.getFunctionsByGroupId(groupId, page, size)
        ));
    }

    @GetMapping("/{groupId}/clients")
    public ResponseEntity<ApiResponseV1<List<ClientDtoV1>>> getClientsByGroup(
            @PathVariable(name = "groupId") Long groupId) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(groupService.getClientsByGroupId(groupId)));
    }

    @GetMapping("/{groupId}/clients/paginated")
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<ClientDtoV1>>> getClientsByGroupPaginated(
            @PathVariable(name = "groupId") Long groupId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(
            groupService.getClientsByGroupId(groupId, page, size)
        ));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponseV1<Long>> countGroups() {
        return ResponseEntity.ok(ApiResponseV1.ok(groupService.count()));
    }

    @GetMapping("/{groupId}/functions/count")
    public ResponseEntity<ApiResponseV1<Long>> countFunctionsByGroup(
            @PathVariable(name = "groupId") Long groupId) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(groupService.countFunctionsByGroupId(groupId)));
    }

    @GetMapping("/{groupId}/clients/count")
    public ResponseEntity<ApiResponseV1<Long>> countClientsByGroup(
            @PathVariable(name = "groupId") Long groupId) {
        
        return ResponseEntity.ok(ApiResponseV1.ok(groupService.countClientsByGroupId(groupId)));
    }
}
