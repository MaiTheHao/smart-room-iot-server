package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.ConditionDto;
import com.iviet.ivshs.dto.CreateConditionDto;
import com.iviet.ivshs.dto.UpdateConditionDto;
import com.iviet.ivshs.service.ConditionService;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("conditionController")
@RequiredArgsConstructor
@RequestMapping("/v1/conditions")
public class ConditionController {

  private final ConditionService conditionService;

  @PostMapping
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<ConditionDto>> create(
      @RequestBody @Valid CreateConditionDto request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(conditionService.create(request)));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<ConditionDto>> getById(@PathVariable(name = "id") Long id) {
    return ResponseEntity.ok(ApiResponse.ok(conditionService.getById(id)));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<ConditionDto>> update(
      @PathVariable(name = "id") Long id, @RequestBody @Valid UpdateConditionDto request) {
    return ResponseEntity.ok(ApiResponse.ok(conditionService.update(id, request)));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable(name = "id") Long id) {
    conditionService.delete(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Condition deleted successfully"));
  }

  @GetMapping
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<List<ConditionDto>>> findByCriteria(
      @RequestParam(name = "ownerCategory", required = false) ConditionOwnerCategory ownerCategory,
      @RequestParam(name = "ownerId", required = false) String ownerId,
      @RequestParam(name = "sourceCategory", required = false) ConditionDataSource sourceCategory,
      @RequestParam(name = "sourceTargetId", required = false) String sourceTargetId,
      @RequestParam(name = "sourceTargetType", required = false) DeviceCategory sourceTargetType) {

    if (ownerCategory != null && ownerId != null) {
      return ResponseEntity.ok(
          ApiResponse.ok(conditionService.findByOwner(ownerCategory, ownerId)));
    }

    if (sourceCategory != null && sourceTargetId != null) {
      if (sourceTargetType != null) {
        return ResponseEntity.ok(ApiResponse.ok(conditionService.findBySourceAndType(
            sourceCategory, sourceTargetId, sourceTargetType)));
      }
      return ResponseEntity.ok(
          ApiResponse.ok(conditionService.findBySource(sourceCategory, sourceTargetId)));
    }

    throw new BadRequestException(
        "Either (ownerCategory, ownerId) or (sourceCategory, sourceTargetId) must be provided");
  }

  @DeleteMapping("/by-owner")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<Integer>> deleteByOwner(
      @RequestParam(name = "ownerCategory") ConditionOwnerCategory ownerCategory,
      @RequestParam(name = "ownerId") String ownerId) {
    int deletedCount = conditionService.deleteByOwner(ownerCategory, ownerId);
    return ResponseEntity.ok(ApiResponse.ok(deletedCount));
  }
}
