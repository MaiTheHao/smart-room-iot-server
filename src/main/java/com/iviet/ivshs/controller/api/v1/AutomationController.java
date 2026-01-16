package com.iviet.ivshs.controller.api.v1;

import java.util.List;

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
import com.iviet.ivshs.dto.AutomationDto;
import com.iviet.ivshs.dto.CreateAutomationDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAutomationDto;
import com.iviet.ivshs.service.AutomationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/automations")
public class AutomationController {

    private final AutomationService automationService;

    @PostMapping
    public ResponseEntity<ApiResponse<AutomationDto>> createAutomation(
            @RequestBody @Valid CreateAutomationDto request) {
        
        AutomationDto automation = automationService.createAutomation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(automation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AutomationDto>> updateAutomation(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateAutomationDto request) {
        
        AutomationDto automation = automationService.updateAutomation(id, request);
        return ResponseEntity.ok(ApiResponse.ok(automation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAutomation(
            @PathVariable(name = "id") Long id) {
        
        automationService.deleteAutomation(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AutomationDto>> getAutomationById(
            @PathVariable(name = "id") Long id) {
        
        AutomationDto automation = automationService.getAutomationById(id);
        return ResponseEntity.ok(ApiResponse.ok(automation));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<AutomationDto>>> getAllAutomations(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        PaginatedResponse<AutomationDto> automations = automationService.getAllAutomations(page, size);
        return ResponseEntity.ok(ApiResponse.ok(automations));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AutomationDto>>> getAllActiveAutomations() {
        
        List<AutomationDto> automations = automationService.getAllActiveAutomations();
        return ResponseEntity.ok(ApiResponse.ok(automations));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleAutomationStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "isActive") boolean isActive) {
        
        automationService.toggleAutomationStatus(id, isActive);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, null, "Automation status updated successfully"));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ApiResponse<Void>> executeAutomationImmediately(
            @PathVariable(name = "id") Long id) {
        
        automationService.executeAutomationImmediately(id);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, null, "Automation executed successfully"));
    }

    @PostMapping("/reload")
    public ResponseEntity<ApiResponse<Void>> reloadAllAutomations() {
        
        automationService.reloadAllAutomations();
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, null, "All automations reloaded successfully"));
    }
}
