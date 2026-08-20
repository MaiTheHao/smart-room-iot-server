package com.iviet.ivshs.controller.api.v1;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.iviet.ivshs.dto.ActionDto;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.ConditionDto;
import com.iviet.ivshs.dto.CreateActionDto;
import com.iviet.ivshs.dto.CreateConditionDto;
import com.iviet.ivshs.dto.CreateRuleDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.ReplaceActionDto;
import com.iviet.ivshs.dto.ReplaceConditionDto;
import com.iviet.ivshs.dto.RuleDto;
import com.iviet.ivshs.dto.UpdateRuleDto;
import com.iviet.ivshs.dto.UpdateRuleStatusDto;
import com.iviet.ivshs.service.ActionService;
import com.iviet.ivshs.service.ConditionService;
import com.iviet.ivshs.service.RuleService;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController("ruleController")
@RequiredArgsConstructor
@RequestMapping("/v1/rules")
public class RuleController {

  private final RuleService ruleService;
  private final ConditionService conditionService;
  private final ActionService actionService;

  @PostMapping
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<RuleDto>> create(@RequestBody @Valid CreateRuleDto request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(ruleService.create(request)));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<RuleDto>> getById(@PathVariable(name = "id") Long id) {
    return ResponseEntity.ok(ApiResponse.ok(ruleService.getById(id)));
  }

  @GetMapping
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<PaginatedResponse<RuleDto>>> findAll(
      @RequestParam(name = "page", defaultValue = "1") int page,
      @RequestParam(name = "limit", defaultValue = "10") int limit) {
    return ResponseEntity.ok(ApiResponse.ok(ruleService.getAll(page, limit)));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<RuleDto>> update(
      @PathVariable(name = "id") Long id, @RequestBody @Valid UpdateRuleDto request) {
    return ResponseEntity.ok(ApiResponse.ok(ruleService.update(id, request)));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable(name = "id") Long id) {
    ruleService.delete(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Rule deleted successfully"));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<Void>> updateStatus(
      @PathVariable(name = "id") Long id, @RequestBody @Valid UpdateRuleStatusDto request) {
    ruleService.updateActiveStatus(id, request.isActive());
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK, null, "Rule status updated: " + request.isActive()));
  }

  @PostMapping("/reload")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<Void>> reloadAllRules() {
    ruleService.reloadAll();
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK, null, "All rules reloaded in Quartz"));
  }

  @PostMapping("/{id}/execute")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<Void>> executeNow(@PathVariable(name = "id") Long id) {
    ruleService.triggerNow(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK, null, "Rule execution triggered immediately"));
  }

  // --- Sub-Resource Helper Endpoints for Conditions ---

  @GetMapping("/{id}/conditions")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<List<ConditionDto>>> getConditions(
      @PathVariable(name = "id") Long ruleId) {
    return ResponseEntity.ok(
        ApiResponse.ok(conditionService.findByOwner(ConditionOwnerCategory.RULE, ruleId)));
  }

  @PostMapping("/{id}/conditions")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<ConditionDto>> addCondition(
      @PathVariable(name = "id") Long ruleId, @RequestBody @Valid CreateConditionDto request) {
    CreateConditionDto scopedDto = CreateConditionDto.builder()
        .ownerCategory(ConditionOwnerCategory.RULE)
        .ownerId(String.valueOf(ruleId))
        .sourceCategory(request.sourceCategory())
        .sourceTargetId(request.sourceTargetId())
        .sourceTargetType(request.sourceTargetType())
        .property(request.property())
        .operator(request.operator())
        .value(request.value())
        .extraParams(request.extraParams())
        .sortOrder(request.sortOrder())
        .nextLogic(request.nextLogic())
        .build();

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(conditionService.create(scopedDto)));
  }

  @PutMapping("/{id}/conditions")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<List<ConditionDto>>> replaceConditions(
      @PathVariable(name = "id") Long ruleId,
      @RequestBody @Valid List<ReplaceConditionDto> request) {
    return ResponseEntity.ok(
        ApiResponse.ok(
            conditionService.replaceByOwner(ConditionOwnerCategory.RULE, ruleId, request)));
  }

  @GetMapping("/{id}/actions")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<List<ActionDto>>> getActions(
      @PathVariable(name = "id") Long ruleId) {
    return ResponseEntity.ok(
        ApiResponse.ok(actionService.findByOwner(ActionOwnerCategory.RULE, ruleId)));
  }

  @PostMapping("/{id}/actions")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<ActionDto>> addAction(
      @PathVariable(name = "id") Long ruleId, @RequestBody @Valid CreateActionDto request) {
    CreateActionDto scopedDto = CreateActionDto.builder()
        .ownerCategory(ActionOwnerCategory.RULE)
        .ownerId(String.valueOf(ruleId))
        .targetCategory(request.targetCategory())
        .targetId(request.targetId())
        .params(request.params())
        .executionOrder(request.executionOrder())
        .build();

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(actionService.create(scopedDto)));
  }

  @PutMapping("/{id}/actions")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
  public ResponseEntity<ApiResponse<List<ActionDto>>> replaceActions(
      @PathVariable(name = "id") Long ruleId,
      @RequestBody @Valid List<ReplaceActionDto> request) {
    return ResponseEntity.ok(
        ApiResponse.ok(
            actionService.replaceByOwner(ActionOwnerCategory.RULE, ruleId, request)));
  }
}
