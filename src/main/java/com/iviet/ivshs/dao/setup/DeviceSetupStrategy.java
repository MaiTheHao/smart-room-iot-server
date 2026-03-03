package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.DeviceCategory;

public interface DeviceSetupStrategy {

    DeviceCategory getSupportedCategory();

    void persist(
        SetupRequest.BodyData.DeviceConfig device,
        Room room,
        DeviceControl deviceControl
    );
}
