package com.iviet.ivshs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.iviet.ivshs.enumeration.SetupCategoryV1;
import com.iviet.ivshs.enumeration.DeviceControlTypeV1;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetupResponseV1 {
    
    private Long roomId;
    private List<CreatedDevice> createdDevices;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatedDevice {
        private Long deviceControlId;
        private SetupCategoryV1 category;
        private Long targetId;
        private String naturalId;
        private String name;
        private DeviceControlTypeV1 controlType;
        private boolean isActive;
        private int position;
    }
}
