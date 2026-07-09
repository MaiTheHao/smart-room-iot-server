package com.iviet.ivshs.controller.api.v1;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.AutomationActionDto;
import com.iviet.ivshs.dto.AutomationDto;
import com.iviet.ivshs.dto.CreateAutomationActionDto;
import com.iviet.ivshs.dto.CreateAutomationDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAutomationActionDto;
import com.iviet.ivshs.dto.UpdateAutomationDto;
import com.iviet.ivshs.service.AutomationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/automations")
public class AutomationController {

    private final AutomationService automationService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<AutomationDto>> create(@RequestBody
    @Valid
    CreateAutomationDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(automationService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<AutomationDto>> update(@PathVariable(name = "id")
    Long id, @RequestBody
    @Valid
    UpdateAutomationDto request) {
        return ResponseEntity.ok(ApiResponse.ok(automationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable(name = "id")
    Long id) {
        automationService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Automation deleted successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<AutomationDto>> getById(@PathVariable(name = "id")
    Long id) {
        return ResponseEntity.ok(ApiResponse.ok(automationService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<PaginatedResponse<AutomationDto>>> getAll(@RequestParam(name = "page", defaultValue = "0")
    int page, @RequestParam(name = "size", defaultValue = "20")
    int size) {
        return ResponseEntity.ok(ApiResponse.ok(automationService.getAll(page, size)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<List<AutomationDto>>> getAllActive() {
        return ResponseEntity.ok(ApiResponse.ok(automationService.getAllActive()));
    }

    @GetMapping("/{id}/actions")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<List<AutomationActionDto>>> getActions(@PathVariable(name = "id")
    Long automationId) {
        return ResponseEntity.ok(ApiResponse.ok(automationService.getActions(automationId)));
    }

    @PostMapping("/{id}/actions")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<AutomationActionDto>> addAction(@PathVariable(name = "id")
    Long automationId, @RequestBody
    @Valid
    CreateAutomationActionDto request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(automationService.addAction(automationId, request)));
    }

    @PutMapping("/actions/{actionId}")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<AutomationActionDto>> updateAction(@PathVariable(name = "actionId")
    Long actionId, @RequestBody
    @Valid
    UpdateAutomationActionDto request) {
        return ResponseEntity.ok(ApiResponse.ok(automationService.updateAction(actionId, request)));
    }

    @DeleteMapping("/actions/{actionId}")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<Void>> removeAction(@PathVariable(name = "actionId")
    Long actionId) {
        automationService.removeAction(actionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Action removed successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable(name = "id")
    Long id, @RequestParam(name = "isActive")
    boolean isActive) {
        automationService.toggleIsActive(id, isActive);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, null, "Automation status updated: " + isActive));
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<Void>> executeImmediately(@PathVariable(name = "id")
    Long id) {
        automationService.triggerNow(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, null, "Automation triggered manually"));
    }

    @PostMapping("/reload-job")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public ResponseEntity<ApiResponse<Void>> reloadSystemJobs() {
        automationService.reloadAll();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, null, "System Quartz Jobs reloaded"));
    }
}
