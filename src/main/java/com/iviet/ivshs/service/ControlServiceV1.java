package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.ControlDeviceRequestV1;
import com.iviet.ivshs.dto.ControlDeviceResponseV1;
import com.iviet.ivshs.entities.DeviceControlV1;

public interface ControlServiceV1 {
	
	ControlDeviceResponseV1 controlDevice(ControlDeviceRequestV1 request);
	ControlDeviceResponseV1 turnOn(DeviceControlV1 deviceControl);
	ControlDeviceResponseV1 turnOff(DeviceControlV1 deviceControl);
	ControlDeviceResponseV1 setLevel(DeviceControlV1 deviceControl, int level);
	ControlDeviceRequestV1 buildRequest(DeviceControlV1 deviceControl, String command);
}
