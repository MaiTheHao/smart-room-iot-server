# ESP32 Telemetry Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate ESP32 telemetry APIs (`GET /telemetry` and `POST /temperature`) with the Spring server, ensuring robust string-to-double parsing of temperature readings, and updating client documentation.

**Architecture:** 
- Expose `GET /telemetry` and `POST /temperature` clients in `Esp32TelemetryClient`.
- Integrate `Esp32TelemetryClient` into `Esp32GatewayAdapter` to fetch global telemetry data.
- Enhance `TemperatureValueServiceImpl` to dynamically handle both string and numeric formats of `tempC`.
- Update API documentation to define the ESP32 temperature API.

**Tech Stack:** Java 21, Spring Framework, Spring Web MVC RestTemplate, Jackson, Lombok.

## Global Constraints

- Do not introduce new third-party testing or core libraries unless requested.
- Ensure all code conforms to existing styling, annotations, and naming conventions.
- Standard logs should use SLF4J logger.

---

### Task 1: Create `Esp32TelemetryClient` and Integrate with `Esp32GatewayAdapter`

**Files:**
- Create: `src/main/java/com/iviet/ivshs/integration/gateway/impl/esp32/Esp32TelemetryClient.java`
- Modify: `src/main/java/com/iviet/ivshs/integration/gateway/impl/esp32/Esp32GatewayAdapter.java`

**Interfaces:**
- Consumes: `Esp32BaseClient` and RestTemplate qualifiers from Spring context.
- Produces: `Esp32TelemetryClient` bean in the application context with `fetchGlobalTelemetry` and `fetchTemperature` methods.

- [ ] **Step 1: Create the new telemetry client file**

Create the file `src/main/java/com/iviet/ivshs/integration/gateway/impl/esp32/Esp32TelemetryClient.java` with the following content:

```java
package com.iviet.ivshs.integration.gateway.impl.esp32;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class Esp32TelemetryClient extends Esp32BaseClient {

    @Qualifier("GatewayTelemetryRestTemplate")
    private final RestTemplate restTemplate;

    public ResponseEntity<TelemetryResponseDto> fetchGlobalTelemetry(String ip) {
        String url = buildEsp32Uri(ip, "telemetry");
        return restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<TelemetryResponseDto>() {}
        );
    }

    public ResponseEntity<ApiResponse<JsonNode>> fetchTemperature(String ip, String naturalId) {
        String url = buildEsp32Uri(ip, "temperature");
        record RequestBody(String naturalId) {}
        HttpEntity<RequestBody> entity = new HttpEntity<>(new RequestBody(naturalId));
        return restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<ApiResponse<JsonNode>>() {}
        );
    }
}
```

- [ ] **Step 2: Modify `Esp32GatewayAdapter` to use `Esp32TelemetryClient`**

Edit `src/main/java/com/iviet/ivshs/integration/gateway/impl/esp32/Esp32GatewayAdapter.java` to inject `Esp32TelemetryClient` and wire it into `fetchGlobalTelemetry`.

Update the imports and fields:
```java
// ... existing imports ...
import com.iviet.ivshs.dto.TelemetryResponseDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class Esp32GatewayAdapter implements GatewayAdapter {

    private final Esp32AuthClient authClient;
    private final Esp32SystemClient systemClient;
    private final Esp32LightControlClient lightClient;
    private final Esp32FanControlClient fanClient;
    private final Esp32AcControlClient acClient;
    private final Esp32TelemetryClient telemetryClient; // Added injection
```

And update `fetchGlobalTelemetry` method:
```java
    @Override
    public GatewayFetchResult<TelemetryResponseDto> fetchGlobalTelemetry(String ip) {
        try {
            ResponseEntity<TelemetryResponseDto> response = telemetryClient.fetchGlobalTelemetry(ip);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return GatewayFetchResult.ok(response.getBody());
            }
            return GatewayFetchResult.failure("HTTP " + response.getStatusCode());
        } catch (Exception e) {
            return GatewayFetchResult.failure(e.getMessage());
        }
    }
```

- [ ] **Step 3: Verify Compilation**

Run compilation to verify there are no compilation errors:
Run: `mvn clean compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit Changes**

```bash
git add src/main/java/com/iviet/ivshs/integration/gateway/impl/esp32/Esp32TelemetryClient.java src/main/java/com/iviet/ivshs/integration/gateway/impl/esp32/Esp32GatewayAdapter.java
git commit -m "feat: implement Esp32TelemetryClient and integrate into Esp32GatewayAdapter"
```

---

### Task 2: Robust parsing of temperature value in `TemperatureValueServiceImpl`

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/service/impl/TemperatureValueServiceImpl.java`

**Interfaces:**
- Consumes: `TelemetryResponseDto.DeviceDto` data.
- Produces: Persistent `TemperatureValue` entities.

- [ ] **Step 1: Update class annotations and parsing logic**

Modify `src/main/java/com/iviet/ivshs/service/impl/TemperatureValueServiceImpl.java` to add `@Slf4j` for logging and update the parsing logic inside `create(TelemetryResponseDto.DeviceDto data)`:

```java
package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dao.TemperatureDao;
import com.iviet.ivshs.dao.TemperatureValueDao;
import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.dto.CreateTemperatureValueDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.dto.TemperatureValueDto;
import com.iviet.ivshs.entities.Temperature;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.service.TemperatureValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Added import
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j // Added annotation
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemperatureValueServiceImpl implements TemperatureValueService {
```

Replace the `create(TelemetryResponseDto.DeviceDto data)` method content:
```java
  @Override
  @Transactional
  public void create(TelemetryResponseDto.DeviceDto data) {
    JsonNode tempCNode = data.getData().get("tempC");
    if (tempCNode == null)
      return;

    Double tempC;
    if (tempCNode.isNumber()) {
      tempC = tempCNode.asDouble();
    } else if (tempCNode.isTextual()) {
      try {
        tempC = Double.parseDouble(tempCNode.asText());
      } catch (NumberFormatException e) {
        log.error("Failed to parse tempC string value '{}' for sensor {}: {}", 
            tempCNode.asText(), data.getNaturalId(), e.getMessage());
        return;
      }
    } else {
      return;
    }

    var sensor = temperatureDao.findByNaturalId(data.getNaturalId()).orElseThrow(() -> new NotFoundException("Temperature sensor not found with natural ID: " + data.getNaturalId()));
    var record = CreateTemperatureValueDto.builder().sensorNaturalId(data.getNaturalId()).tempC(tempC).timestamp(Instant.now()).build().toEntity();

    record.setSensor(sensor);
    temperatureValueDao.save(record);
    sensor.setCurrentValue(record.getTempC());
  }
```

- [ ] **Step 2: Verify Compilation**

Run compilation to verify there are no compilation errors:
Run: `mvn clean compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit Changes**

```bash
git add src/main/java/com/iviet/ivshs/service/impl/TemperatureValueServiceImpl.java
git commit -m "feat: robustly parse both string and numeric formats of tempC in TemperatureValueServiceImpl"
```

---

### Task 3: Document the ESP32 Temperature API

**Files:**
- Create: `doc/esp32_api_doc/temperature.md`

- [ ] **Step 1: Create the API documentation file**

Create the file `doc/esp32_api_doc/temperature.md` with the following content:

```markdown
# Temperature Module - SmartRoom Client

## Temperature API Documentation

---

### **POST** `/temperature` - Lấy dữ liệu nhiệt độ từ cảm biến

> API này dùng để lấy dữ liệu nhiệt độ hiện tại từ cảm biến được chỉ định.
> 
> Hiện tại chỉ hỗ trợ cảm biến DS18B20 sử dụng giao tiếp 1-Wire.
> 
> Request bắt buộc phải có JWT token hợp lệ trong header `Authorization`.

### Request Headers

| Tên header | Giá trị | Bắt buộc | Mô tả |
| :--------- | :------ | :------- | :---- |
| Content-Type | application/json | Có | Định dạng dữ liệu gửi lên |
| Authorization | Bearer <token> | Có | JWT token lấy từ API đăng nhập |
| Origin | string | Không | Nguồn gốc request (hỗ trợ CORS) |

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--------- | :--- | :------- | :---- |
| naturalId | string | Có | Định danh cảm biến nhiệt độ |

### Request Example

```json
{
	"naturalId": "TEMP_ESP32_01"
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Lấy nhiệt độ thành công",
	"data": {
		"tempC": "25.500"
	},
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Response Fields

| Tên trường | Loại | Mô tả |
| :--------- | :--- | :---- |
| tempC | string | Giá trị nhiệt độ theo độ Celsius (3 chữ số thập phân) |

### Lỗi chung

#### Response (400 Bad Request)

> Xảy ra khi body trống hoặc thiếu trường `naturalId`.

```json
{
	"status": 400,
	"message": "Body bắt buộc phải có trường: naturalId",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

#### Response (401 Unauthorized)

> Xảy ra khi thiếu header `Authorization`, token không đúng định dạng, hoặc token không hợp lệ/hết hạn.

```json
{
	"status": 401,
	"message": "Token hết hạn hoặc không đúng",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

#### Response (404 Not Found)

> Xảy ra khi không tìm thấy cảm biến có `naturalId` tương ứng trong config.

```json
{
	"status": 404,
	"message": "Không tìm thấy cảm biến nhiệt độ có naturalId tương ứng",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

#### Response (500 Internal Server Error)

> Xảy ra khi:
> - Cảm biến thiếu thông tin cấu hình (module, GPIO pin)
> - Không thể đọc dữ liệu từ cảm biến (cảm biến không phản hồi)
> - Lỗi khi parse JSON cấu hình

```json
{
	"status": 500,
	"message": "Lỗi: Không thể đọc dữ liệu từ cảm biến",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

#### Response (501 Not Implemented)

> Xảy ra khi module cảm biến chưa được hỗ trợ (không phải DS18B20).

```json
{
	"status": 501,
	"message": "Module cảm biến chưa được hỗ trợ",
	"timestamp": "2026-07-07T03:28:27Z"
}
```
```

- [ ] **Step 2: Commit documentation**

```bash
git add doc/esp32_api_doc/temperature.md
git commit -m "docs: add temperature module API documentation for ESP32"
```
