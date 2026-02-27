package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.FanDto;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.CreateFanDto;
import com.iviet.ivshs.dto.FanControlRequestBody;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateFanDto;
import com.iviet.ivshs.service.FanControlService;
import com.iviet.ivshs.service.FanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FanController {

    private final FanService fanService;
    private final FanControlService fanControlService;

    @GetMapping("/fans")
    public ResponseEntity<ApiResponse<PaginatedResponse<FanDto>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(fanService.getList(page, size)));
    }

    @GetMapping("/fans/all")
    public ResponseEntity<ApiResponse<java.util.List<FanDto>>> getAllFans() {
        return ResponseEntity.ok(ApiResponse.ok(fanService.getAll()));
    }

    @GetMapping("/room/{roomId}/fans")
    public ResponseEntity<ApiResponse<PaginatedResponse<FanDto>>> getByRoom(
            @PathVariable(name = "roomId") Long roomId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        
        return ResponseEntity.ok(ApiResponse.ok(fanService.getListByRoomId(roomId, page, size)));
    }

    @GetMapping("/room/{roomId}/fans/all")
    public ResponseEntity<ApiResponse<java.util.List<FanDto>>> getAllByRoom(
            @PathVariable(name = "roomId") Long roomId) {
        
        return ResponseEntity.ok(ApiResponse.ok(fanService.getAllByRoomId(roomId)));
    }

    @GetMapping("/room/{roomId}/fans/{naturalId}")
    public ResponseEntity<ApiResponse<FanDto>> getByRoomAndNaturalId(
            @PathVariable(name = "roomId") Long roomId,
            @PathVariable(name = "naturalId") String naturalId) {
        
        FanDto fan = fanService.getByRoomAndNaturalId(roomId, naturalId);
        return ResponseEntity.ok(ApiResponse.ok(fan));
    }

    @GetMapping("/fans/{id}")
    public ResponseEntity<ApiResponse<FanDto>> getById(
            @PathVariable(name = "id") Long id) {
        
        return ResponseEntity.ok(ApiResponse.ok(fanService.getById(id)));
    }

    @PostMapping("/fans")
    public ResponseEntity<ApiResponse<FanDto>> create(
            @RequestBody @Valid CreateFanDto request) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(fanService.create(request)));
    }

    @PutMapping("/fans/{id}")
    public ResponseEntity<ApiResponse<FanDto>> update(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateFanDto request) {
        
        return ResponseEntity.ok(ApiResponse.ok(fanService.update(id, request)));
    }

    @DeleteMapping("/fans/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable(name = "id") Long id) {
        
        fanService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Deleted successfully"));
    }

    @PutMapping("/fans/{naturalId}/control")
    public ResponseEntity<ApiResponse<FanDto>> control(
        @PathVariable(name = "naturalId") String naturalId,
        @RequestBody @Valid FanControlRequestBody params) {

        fanControlService.control(naturalId, params);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.ACCEPTED, null, "Controlled successfully"));
    }
}
