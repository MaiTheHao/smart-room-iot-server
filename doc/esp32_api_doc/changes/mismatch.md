## Mismatch Report: ESP32 API Changes vs Java HttpClient Files

Based on comparing the changes in `doc/esp32_api_doc/changes/changes.txt` against the files in `src/main/java/com/iviet/ivshs/integration/gateway/impl/esp32/`, here are all the mismatches:

---

### 1. Missing `Esp32AcControlClient` — AIR_CONDITION control not implemented
- **Changes**: ESP32 firmware now supports `AIR_CONDITION` category with params: `power`, `mode`, `temperature`, `fanSpeed`, `swing`
- **Current**: No `Esp32AcControlClient` exists
- **Impact**: `Esp32GatewayAdapter.dispatchControl()` returns `null` for `AIR_CONDITION`, so AC control silently fails as "not supported"

### 2. `Esp32GatewayAdapter` doesn't dispatch AIR_CONDITION
- **File**: `Esp32GatewayAdapter.java:63-72` — only handles `LIGHT` and `FAN`
- **Reference**: `RaspiGatewayAdapter.java:82-84` already has an `AIR_CONDITION` branch with `RaspiAcControlClient`

### 3. `Esp32FanControlClient` uses hardcoded string `"FAN"` instead of `DeviceCategory.FAN`
- **File**: `Esp32FanControlClient.java:27` — `body.put("category", "FAN")`
- **Inconsistent with**: `Esp32LightControlClient.java:32` — uses `DeviceCategory.LIGHT`
- Should use the enum for consistency

### 4. `Esp32FanControlClient` early-return bug when no power/speed given
- **File**: `Esp32FanControlClient.java:32` — `if (body.size() == 2) return null;`
- If only `mode` or `swing` is sent (valid for IR fans), body has 3 entries but `power` and `speed` are null, so only 2 items are put → returns null incorrectly

### 5. `SetupRequest.DeviceConfig` missing `internal` field
- **Changes**: Device config JSON changed from flat `peripheralType` to nested `"internal": { "peripheralType": "RELAY" }` (and for AC: `brand`, `codeConfigs`)
- **Current**: `SetupRequest.DeviceConfig` has no `internal` field, no `peripheralType` at any level
- **Impact**: `Esp32SystemClient.fetchSetup()` response deserialization will silently drop the `internal` data (and any `peripheralType` info)

### 6. Missing `Esp32ConfigClient` — PATCH `/config` endpoint not implemented
- **Changes**: ESP32 firmware has a `PATCH /config` API for updating device configuration
- **Current**: No client class exists for this endpoint

### 7. Missing `Esp32AcControlClient` field injection in `Esp32GatewayAdapter`
- **File**: `Esp32GatewayAdapter.java:24-25` — only injects `lightClient` and `fanClient`

### 8. No code impact from auth credential changes (doc-only)
- The default ESP32 credentials changed from `admin`/`admin123` → `vuesp`/`123456789`
- The Java code uses DB-stored credentials via `client.getUsername()`/`client.getGatewayPassword()`, so this is purely a documentation change with no code impact

---

**Summary**: 3 missing client files/classes (AirCondition, Config, internal field), 1 broken dispatch, 2 code quality issues (hardcoded string, early-return logic), 1 DTO missing field. The `Esp32GatewayAdapter` is effectively unable to control `AIR_CONDITION` devices and cannot call the config update API.