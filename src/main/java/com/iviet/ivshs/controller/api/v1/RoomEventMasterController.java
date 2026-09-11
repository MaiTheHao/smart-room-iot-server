package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dao.RoomEventDao;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.RoomEventCodeDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/room-events")
@PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ROOM')")
public class RoomEventMasterController {

  private final RoomEventDao roomEventDao;

  @GetMapping("/codes")
  public ResponseEntity<ApiResponse<List<RoomEventCodeDto>>> getCodes() {
    List<RoomEventCodeDto> list =
        roomEventDao.findAll().stream().map(RoomEventCodeDto::fromEntity).toList();
    return ResponseEntity.ok(ApiResponse.ok(list));
  }
}
