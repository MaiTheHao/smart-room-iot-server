package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.iviet.ivshs.enumeration.DeviceCategoryV1;
import com.iviet.ivshs.enumeration.DeviceControlTypeV1;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SetupRequest {

    @NotBlank(message = "Room code is required")
    private String roomCode;
    private List<DeviceConfig> devices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceConfig {
        private String naturalId;
        private DeviceCategoryV1 category;
        private DeviceControlTypeV1 controlType;
        private Integer gpioPin;
        private String bleMac;
        private String apiEndpoint;
        private String name;
        private Map<String, TranslationDetail> translations;

        @JsonProperty("isActive")
        @Builder.Default
        private boolean isActive = true;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TranslationDetail {
            private String name;
            private String description;
        }
    }
}