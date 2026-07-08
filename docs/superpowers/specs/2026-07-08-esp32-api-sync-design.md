# Design Spec: ESP32 API Sync (July 2026)

## 1. Context & Goal
The ESP32 firmware was updated with support for `AIR_CONDITION` device controls (IR sender implementation), updated default login credentials, updated device config format (nesting `peripheralType` under `internal`), and a new `PATCH /config` endpoint. 

This spec outlines the synchronization requirements between the Java server and the ESP32 gateway API changes, clarifying what will be implemented and what will be skipped.

---

## 2. Scope

### In Scope
1. **Implement AC Control**: Create `Esp32AcControlClient` to send `POST /control` requests for `AIR_CONDITION` device categories.
2. **Dispatch AC Control**: Modify `Esp32GatewayAdapter` to route `AIR_CONDITION` commands through the new `Esp32AcControlClient`.
3. **Fix ESP32 Fan Client issues**:
   - Replace hardcoded `"FAN"` string with `DeviceCategory.FAN` enum.
   - Resolve early-return bug in `Esp32FanControlClient` when `power` and `speed` are null but other IR fan params (`mode` or `swing`) are present.
4. **Document Setup DTO omission**: Add explaining comments in `SetupRequest.java` indicating that the `internal` field is only needed by the gateway and is deliberately skipped by the server.

### Out of Scope (Skipped)
1. **PATCH `/config` endpoint**: The server does not manage or request ESP32 configuration updates directly, so `Esp32ConfigClient` will be skipped.
2. **Credentials Sync**: Default credential change (`admin`/`admin123` -> `vuesp`/`123456789`) is purely firmware configuration. The Java client retrieves connection credentials dynamically from the database, so no credential sync logic is required.

---

## 3. Detailed Changes

### 3.1 `Esp32AcControlClient.java` (New Class)
- **Path**: `src/main/java/com/iviet/ivshs/integration/gateway/impl/esp32/Esp32AcControlClient.java`
- **Implementation**:
  - Extend `Esp32BaseClient`.
  - Extract parameters: `power` (mapped to `"ON"`/`"OFF"`), `temperature`, `mode`, `fanSpeed` (fallback to `speed`), and `swing`.
  - Assemble a `POST /control` payload.
  - Return `null` to skip request execution if no control parameters are passed.

### 3.2 `Esp32GatewayAdapter.java` (Modify)
- **Path**: `src/main/java/com/iviet/ivshs/integration/gateway/impl/esp32/Esp32GatewayAdapter.java`
- **Implementation**:
  - Inject `Esp32AcControlClient`.
  - Add dispatch case in `dispatchControl()` for `DeviceCategory.AIR_CONDITION`.

### 3.3 `Esp32FanControlClient.java` (Modify)
- **Path**: `src/main/java/com/iviet/ivshs/integration/gateway/impl/esp32/Esp32FanControlClient.java`
- **Implementation**:
  - Use `DeviceCategory.FAN` instead of `"FAN"`.
  - Support extracting `mode` and `swing` parameters.
  - Ensure size check (`body.size() == 2`) runs after checking all parameters (`power`, `speed`, `mode`, `swing`).

### 3.4 `SetupRequest.java` (Modify)
- **Path**: `src/main/java/com/iviet/ivshs/dto/setup/SetupRequest.java`
- **Implementation**:
  - Add a code comment clarifying that the `internal` JSON configuration block is omitted intentionally from the server-side DTO.

---

## 4. Verification Plan

### Automated Verification
- Verify code compiles successfully using Maven:
  ```bash
  mvn clean compile
  ```

### Manual Verification
- Unit test the payload serialization for both `Esp32FanControlClient` and `Esp32AcControlClient` to ensure all fields map to their correct API equivalents.
