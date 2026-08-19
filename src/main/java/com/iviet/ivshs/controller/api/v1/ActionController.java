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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.ActionDto;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.CreateActionDto;
import com.iviet.ivshs.dto.UpdateActionDto;
import com.iviet.ivshs.service.ActionService;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController("actionController")
@RequiredArgsConstructor
@RequestMapping("/v1/actions")
public class ActionController {

  private final ActionService actionService;

  @PostMapping
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<ActionDto>> create(
      @RequestBody @Valid CreateActionDto request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(actionService.create(request)));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<ActionDto>> getById(@PathVariable(name = "id") Long id) {
    return ResponseEntity.ok(ApiResponse.ok(actionService.getById(id)));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<ActionDto>> update(
      @PathVariable(name = "id") Long id, @RequestBody @Valid UpdateActionDto request) {
    return ResponseEntity.ok(ApiResponse.ok(actionService.update(id, request)));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable(name = "id") Long id) {
    actionService.delete(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Action deleted successfully"));
  }

  @GetMapping
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<List<ActionDto>>> findByCriteria(
      @RequestParam(name = "ownerCategory", required = false) ActionOwnerCategory ownerCategory,
      @RequestParam(name = "ownerId", required = false) String ownerId,
      @RequestParam(name = "targetCategory", required = false) DeviceCategory targetCategory,
      @RequestParam(name = "targetId", required = false) String targetId) {

    if (ownerCategory != null && ownerId != null) {
      return ResponseEntity.ok(ApiResponse.ok(actionService.findByOwner(ownerCategory, ownerId)));
    }

    if (targetCategory != null && targetId != null) {
      return ResponseEntity.ok(
          ApiResponse.ok(actionService.findByTarget(targetCategory, targetId)));
    }

    throw new BadRequestException(
        "Either (ownerCategory, ownerId) or (targetCategory, targetId) must be provided");
  }

  @DeleteMapping("/by-owner")
  @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE', 'F_MANAGE_AUTOMATION')")
  public ResponseEntity<ApiResponse<Integer>> deleteByOwner(
      @RequestParam(name = "ownerCategory") ActionOwnerCategory ownerCategory,
      @RequestParam(name = "ownerId") String ownerId) {
    int deletedCount = actionService.deleteByOwner(ownerCategory, ownerId);
    return ResponseEntity.ok(ApiResponse.ok(deletedCount));
  }
}
