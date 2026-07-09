package com.iviet.ivshs.service.strategy;

import java.util.List;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

public interface DeviceMetadataServiceStrategy {
    DeviceCategory getSupportedCategory();
    List<?> getDeviceByRoomId(Long roomId);
    List<?> getAllDevice();
}
