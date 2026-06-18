# Device Control Payload Standardization Design

This design standardizes the usage of `DeviceControlPayload` by encapsulating the command control `data` inside the payload object itself. This aligns the gateway callers (`GatewayDeviceControlClient` and subclasses) with the Single Responsibility Principle (SRP) by shifting payload formatting logic into the payload class.

## Goals

- Refactor `DeviceControlPayload` to contain the `data` payload.
- Transition from `buildPayload(data)` to `toMap()` in `DeviceControlPayload`.
- Clean up `GatewayDeviceControlClient` signature, removing legacy `executePut` methods and passing `DeviceControlPayload` directly.
- Standardize all calling services to build and pass the `DeviceControlPayload` containing `data`.

## Proposed Changes

### DTOs

#### [MODIFY] [DeviceControlPayload.java](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/java/com/iviet/ivshs/dto/control/DeviceControlPayload.java)
- Convert `DeviceControlPayload` record to:
  ```java
  public record DeviceControlPayload(DeviceSpecificType type, Integer duration, Object data) {
  ```
- Implement `toMap()` to construct the JSON payload body.
- Implement factory methods:
  - `public static DeviceControlPayload of(DeviceSpecificType type, Object data)`
  - `public static DeviceControlPayload of(DeviceSpecificType type, Integer duration, Object data)`

### Gateway Integration Clients

#### [MODIFY] [GatewayDeviceControlClient.java](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/java/com/iviet/ivshs/integration/gateway/GatewayDeviceControlClient.java)
- Keep only:
  - `protected ResponseEntity<ApiResponse<String>> executePut(String url, Object data)` (retained for custom payloads like AC control).
  - `protected ResponseEntity<ApiResponse<String>> executePut(String url, DeviceControlPayload payload)`:
    ```java
    protected ResponseEntity<ApiResponse<String>> executePut(String url, DeviceControlPayload payload) {
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload.toMap());
        return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, new ParameterizedTypeReference<ApiResponse<String>>() {});
    }
    ```
- Delete:
  - `executePut(String url, Object data, String specificType, Integer duration)`
  - `executePut(String url, DeviceControlPayload payload, Object data)`

#### [MODIFY] [GatewayFanControlClient.java](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/java/com/iviet/ivshs/integration/gateway/GatewayFanControlClient.java)
- Update signatures to take `DeviceControlPayload` only:
  - `controlFanPower(String ip, String naturalId, DeviceControlPayload payload)`
  - `controlFanSpeed(String ip, String naturalId, DeviceControlPayload payload)`
  - `controlFanMode(String ip, String naturalId, DeviceControlPayload payload)`
  - `controlFanSwing(String ip, String naturalId, DeviceControlPayload payload)`

#### [MODIFY] [GatewayLightControlClient.java](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/java/com/iviet/ivshs/integration/gateway/GatewayLightControlClient.java)
- Update signatures to take `DeviceControlPayload` only:
  - `controlLightPower(String ip, String naturalId, DeviceControlPayload payload)`
  - `controlLightLevel(String ip, String naturalId, DeviceControlPayload payload)`

### Service Layer

#### [MODIFY] [FanControlServiceImpl.java](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/java/com/iviet/ivshs/service/control/impl/FanControlServiceImpl.java)
- Construct the `DeviceControlPayload` with the respective command data (e.g. power, speed, mode, swing) before passing to the client.

#### [MODIFY] [LightControlServiceImpl.java](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/java/com/iviet/ivshs/service/control/impl/LightControlServiceImpl.java)
- Construct the `DeviceControlPayload` with the respective command data (e.g. power, level) before passing to the client.

## Verification

### Automated Verification
- Run `mvn clean compile` to ensure compilation success.
