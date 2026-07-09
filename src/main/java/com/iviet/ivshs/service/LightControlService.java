package com.iviet.ivshs.service;

import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;

import com.iviet.ivshs.dto.LightControlRequestBody;

public interface LightControlService extends DeviceControlServiceStrategy<LightControlRequestBody> {
}
