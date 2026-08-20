package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.dto.ActionResult;
import com.iviet.ivshs.dto.ControlDeviceResult;
import com.iviet.ivshs.entities.Action;
import java.util.List;

public interface ActionExecutionService {

  ControlDeviceResult execute(Action action);

  List<ActionResult> executeAll(List<Action> actions);
}
