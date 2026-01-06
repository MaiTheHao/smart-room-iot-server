package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.SysFunctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/functions")
public class SysFunctionController {

    private final SysFunctionService functionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<SysFunctionDto>>> getFunctions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(functionService.getList(page, size)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SysFunctionDto>>> getAllFunctions() {
        return ResponseEntity.ok(ApiResponse.ok(functionService.getAll()));
    }

    @GetMapping("/{functionId}")
    public ResponseEntity<ApiResponse<SysFunctionDto>> getFunctionById(
            @PathVariable(name = "functionId") Long functionId) {
        return ResponseEntity.ok(ApiResponse.ok(functionService.getById(functionId)));
    }

    @GetMapping("/code/{functionCode}")
    public ResponseEntity<ApiResponse<SysFunctionDto>> getFunctionByCode(
            @PathVariable(name = "functionCode") String functionCode) {
        return ResponseEntity.ok(ApiResponse.ok(functionService.getByCode(functionCode)));
    }

    @GetMapping("/with-group-status/{groupId}")
    public ResponseEntity<ApiResponse<List<SysFunctionWithGroupStatusDto>>> getFunctionsWithGroupStatus(
            @PathVariable(name = "groupId") Long groupId) {
        return ResponseEntity.ok(ApiResponse.ok(functionService.getAllWithGroupStatus(groupId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SysFunctionDto>> createFunction(
            @RequestBody @Valid CreateSysFunctionDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(functionService.create(request)));
    }

    @PutMapping("/{functionId}")
    public ResponseEntity<ApiResponse<SysFunctionDto>> updateFunction(
            @PathVariable(name = "functionId") Long functionId,
            @RequestBody @Valid UpdateSysFunctionDto request) {
        return ResponseEntity.ok(ApiResponse.ok(functionService.update(functionId, request)));
    }

    @DeleteMapping("/{functionId}")
    public ResponseEntity<ApiResponse<Void>> deleteFunction(
            @PathVariable(name = "functionId") Long functionId) {
        functionService.delete(functionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Function deleted successfully"));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countFunctions() {
        return ResponseEntity.ok(ApiResponse.ok(functionService.count()));
    }
}
