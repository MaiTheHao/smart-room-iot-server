package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.SensorEventRequestDto;
import com.iviet.ivshs.service.MotionMetricService;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class SensorEventController {

  private final MotionMetricService motionMetricService;

  @PostMapping("/sensors/{naturalId}/event")
  public ResponseEntity<ApiResponse<Void>> ingestSensorEvent(
      @PathVariable(name = "naturalId") String naturalId,
      @Valid @RequestBody SensorEventRequestDto request) {
    if (request.getCategory() == DeviceCategory.MOTION_DETECTOR) {
      motionMetricService.processMotionData(naturalId, request.getData());
    } else {
      throw new BadRequestException(
          "Unsupported sensor category for event: " + request.getCategory());
    }
    return ResponseEntity.ok(ApiResponse.ok(null));
  }
}
