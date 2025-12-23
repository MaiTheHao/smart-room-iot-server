package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.iviet.ivshs.enumeration.SetupCategoryV1;
import com.iviet.ivshs.enumeration.DeviceControlTypeV1;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SetupRequestV1 {

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @Builder.Default
    private String langCode = "vi";

    private List<DeviceConfig> devices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceConfig {
        private SetupCategoryV1 category;
        private String name;
        private DeviceControlTypeV1 controlType;
        private String naturalId;
        private Integer gpioPin;
        private String bleMac;
        private String apiEndpoint;

        @JsonProperty("isActive")
        @Builder.Default
        private boolean isActive = true;
    }
}