package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.DeviceControlType;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

      private String specificType;
      private Integer duration; // TODO: Not clean property, remove later

      private DeviceControlType controlType;
      private List<Integer> gpioPin;
      private String bleMac;
      private String apiEndpoint;
      private String name;
      private Map<String, TranslationDetail> translations;

      // Note: The nested 'internal' block from ESP32 configuration payload is
      // gateway-specific (e.g. IR sender hex codes or relay metadata). The Java
      // server does not require this configuration, hence it is deliberately omitted
      // from this DTO.

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

      @JsonSetter("gpioPin")
      public void setGpioPin(Object value) {
        if (value == null) {
          this.gpioPin = null;
          return;
        }

        if (value instanceof List<?> list) {
          this.gpioPin =
              list.stream().map(item -> Integer.parseInt(item.toString())).toList();
        } else {
          this.gpioPin = List.of(Integer.parseInt(value.toString()));
        }
      }
    }
  }
}
