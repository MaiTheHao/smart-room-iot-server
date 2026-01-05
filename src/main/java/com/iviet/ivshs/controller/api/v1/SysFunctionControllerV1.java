package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.service.SysFunctionServiceV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/functions")
public class SysFunctionControllerV1 {

    private final SysFunctionServiceV1 functionService;

    @GetMapping
    public ResponseEntity<ApiResponseV1<PaginatedResponseV1<SysFunctionDtoV1>>> getFunctions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponseV1.ok(functionService.getList(page, size)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponseV1<List<SysFunctionDtoV1>>> getAllFunctions() {
        return ResponseEntity.ok(ApiResponseV1.ok(functionService.getAll()));
    }

    @GetMapping("/{functionId}")
    public ResponseEntity<ApiResponseV1<SysFunctionDtoV1>> getFunctionById(
            @PathVariable(name = "functionId") Long functionId) {
        return ResponseEntity.ok(ApiResponseV1.ok(functionService.getById(functionId)));
    }

    @GetMapping("/code/{functionCode}")
    public ResponseEntity<ApiResponseV1<SysFunctionDtoV1>> getFunctionByCode(
            @PathVariable(name = "functionCode") String functionCode) {
        return ResponseEntity.ok(ApiResponseV1.ok(functionService.getByCode(functionCode)));
    }

    @GetMapping("/with-group-status/{groupId}")
    public ResponseEntity<ApiResponseV1<List<SysFunctionWithGroupStatusDtoV1>>> getFunctionsWithGroupStatus(
            @PathVariable(name = "groupId") Long groupId) {
        return ResponseEntity.ok(ApiResponseV1.ok(functionService.getAllWithGroupStatus(groupId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponseV1<SysFunctionDtoV1>> createFunction(
            @RequestBody @Valid CreateSysFunctionDtoV1 request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseV1.created(functionService.create(request)));
    }

    @PutMapping("/{functionId}")
    public ResponseEntity<ApiResponseV1<SysFunctionDtoV1>> updateFunction(
            @PathVariable(name = "functionId") Long functionId,
            @RequestBody @Valid UpdateSysFunctionDtoV1 request) {
        return ResponseEntity.ok(ApiResponseV1.ok(functionService.update(functionId, request)));
    }

    @DeleteMapping("/{functionId}")
    public ResponseEntity<ApiResponseV1<Void>> deleteFunction(
            @PathVariable(name = "functionId") Long functionId) {
        functionService.delete(functionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponseV1.success(HttpStatus.NO_CONTENT, null, "Function deleted successfully"));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponseV1<Long>> countFunctions() {
        return ResponseEntity.ok(ApiResponseV1.ok(functionService.count()));
    }
}
