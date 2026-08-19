package com.iviet.ivshs.service.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dto.ActionResult;
import com.iviet.ivshs.dto.ControlDeviceResult;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.util.List;

public interface ActionExecutionService {

  ControlDeviceResult execute(Action action);

  List<ActionResult> executeAll(List<Action> actions);

  void validateActionParams(DeviceCategory category, Long targetDeviceId, JsonNode params);
}
