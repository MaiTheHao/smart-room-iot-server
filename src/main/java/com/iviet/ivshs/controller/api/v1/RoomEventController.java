package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.ActionDto;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.ConditionDto;
import com.iviet.ivshs.dto.CreateActionDto;
import com.iviet.ivshs.dto.CreateConditionDto;
import com.iviet.ivshs.dto.CreateRoomEventConfigDto;
import com.iviet.ivshs.dto.ReplaceActionDto;
import com.iviet.ivshs.dto.ReplaceConditionDto;
import com.iviet.ivshs.dto.RoomEventConfigDto;
import com.iviet.ivshs.dto.UpdateRoomEventConfigDto;
import com.iviet.ivshs.service.ActionService;
import com.iviet.ivshs.service.ConditionService;
import com.iviet.ivshs.service.RoomEventConfigService;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rooms/{roomId}/events")
@PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ROOM')")
public class RoomEventController {

  private final RoomEventConfigService roomEventConfigService;
  private final ConditionService conditionService;
  private final ActionService actionService;

  // --- CRUD Config ---

  @PostMapping
  public ResponseEntity<ApiResponse<RoomEventConfigDto>> create(
      @PathVariable(name = "roomId") Long roomId,
      @RequestBody @Valid CreateRoomEventConfigDto request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(roomEventConfigService.create(roomId, request)));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<RoomEventConfigDto>>> getAll(
      @PathVariable(name = "roomId") Long roomId) {
    return ResponseEntity.ok(ApiResponse.ok(roomEventConfigService.getAllByRoomId(roomId)));
  }

  @GetMapping("/{configId}")
  public ResponseEntity<ApiResponse<RoomEventConfigDto>> getById(
      @PathVariable(name = "roomId") Long roomId,
      @PathVariable(name = "configId") Long configId) {
    return ResponseEntity.ok(ApiResponse.ok(roomEventConfigService.getById(roomId, configId)));
  }

  @PutMapping("/{configId}")
  public ResponseEntity<ApiResponse<RoomEventConfigDto>> update(
      @PathVariable(name = "roomId") Long roomId,
      @PathVariable(name = "configId") Long configId,
      @RequestBody @Valid UpdateRoomEventConfigDto request) {
    return ResponseEntity.ok(ApiResponse.ok(roomEventConfigService.update(roomId, configId, request)));
  }

  @DeleteMapping("/{configId}")
  public ResponseEntity<ApiResponse<Void>> delete(
      @PathVariable(name = "roomId") Long roomId,
      @PathVariable(name = "configId") Long configId) {
    roomEventConfigService.delete(roomId, configId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Room event config deleted successfully"));
  }

  // --- Condition Helpers ---

  @GetMapping("/{configId}/conditions")
  public ResponseEntity<ApiResponse<List<ConditionDto>>> getConditions(
      @PathVariable(name = "roomId") Long roomId,
      @PathVariable(name = "configId") Long configId) {
    roomEventConfigService.getById(roomId, configId);
    return ResponseEntity.ok(
        ApiResponse.ok(conditionService.findByOwner(ConditionOwnerCategory.ROOM_EVENT, configId)));
  }

  @PostMapping("/{configId}/conditions")
  public ResponseEntity<ApiResponse<ConditionDto>> addCondition(
      @PathVariable(name = "roomId") Long roomId,
      @PathVariable(name = "configId") Long configId,
      @RequestBody @Valid CreateConditionDto request) {
    roomEventConfigService.getById(roomId, configId);
    CreateConditionDto scopedDto = request.withOwner(ConditionOwnerCategory.ROOM_EVENT, configId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(conditionService.create(scopedDto)));
  }

  @PutMapping("/{configId}/conditions")
  public ResponseEntity<ApiResponse<List<ConditionDto>>> replaceConditions(
      @PathVariable(name = "roomId") Long roomId,
      @PathVariable(name = "configId") Long configId,
      @RequestBody @Valid List<ReplaceConditionDto> request) {
    roomEventConfigService.getById(roomId, configId);
    return ResponseEntity.ok(
        ApiResponse.ok(conditionService.replaceByOwner(ConditionOwnerCategory.ROOM_EVENT, configId, request)));
  }

  // --- Action Helpers ---

  @GetMapping("/{configId}/actions")
  public ResponseEntity<ApiResponse<List<ActionDto>>> getActions(
      @PathVariable(name = "roomId") Long roomId,
      @PathVariable(name = "configId") Long configId) {
    roomEventConfigService.getById(roomId, configId);
    return ResponseEntity.ok(
        ApiResponse.ok(actionService.findByOwner(ActionOwnerCategory.ROOM_EVENT, configId)));
  }

  @PostMapping("/{configId}/actions")
  public ResponseEntity<ApiResponse<ActionDto>> addAction(
      @PathVariable(name = "roomId") Long roomId,
      @PathVariable(name = "configId") Long configId,
      @RequestBody @Valid CreateActionDto request) {
    roomEventConfigService.getById(roomId, configId);
    CreateActionDto scopedDto = request.withOwner(ActionOwnerCategory.ROOM_EVENT, configId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(actionService.create(scopedDto)));
  }

  @PutMapping("/{configId}/actions")
  public ResponseEntity<ApiResponse<List<ActionDto>>> replaceActions(
      @PathVariable(name = "roomId") Long roomId,
      @PathVariable(name = "configId") Long configId,
      @RequestBody @Valid List<ReplaceActionDto> request) {
    roomEventConfigService.getById(roomId, configId);
    return ResponseEntity.ok(
        ApiResponse.ok(actionService.replaceByOwner(ActionOwnerCategory.ROOM_EVENT, configId, request)));
  }
}
