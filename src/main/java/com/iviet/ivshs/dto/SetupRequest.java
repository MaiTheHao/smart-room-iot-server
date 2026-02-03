package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.enumeration.DeviceControlType;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetupRequest {

    private int status;
	private String message;
	private Instant timestamp;
	private BodyData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BodyData {
        @NotBlank(message = "Room code is required")
        private String roomCode;
        private List<DeviceConfig> devices;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DeviceConfig {
            private String naturalId;

            @JsonSetter(nulls = Nulls.AS_EMPTY)
            private DeviceCategory category;
            
            private DeviceControlType controlType;
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
}