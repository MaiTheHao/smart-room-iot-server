# Design Spec: CO2 & Lux Sensor Integration

**Date:** 2026-07-16  
**Branch:** `feat/integrate-co2-lux-sensors`

---

## 1. Tổng quan

Tích hợp 2 loại sensor mới vào hệ thống Smart Room IoT theo đúng pattern đã được xây dựng cho `HumiditySensor`:

| Sensor | DeviceCategory | Telemetry field | Entity field | MetricDomain |
|--------|---------------|-----------------|--------------|--------------|
| CO2 Sensor | `SENSOR_CO2` | `co2` | `currentCO2` | `CO2` |
| Lux Sensor | `SENSOR_LUX` | `lux` | `currentLux` | `LUX` |

Hệ thống phải hoạt động end-to-end: từ setup thiết bị → nhận telemetry → lưu metric → query history.

---

## 2. Kiến trúc tổng thể

Mỗi sensor mới đi theo đúng chiều dọc (vertical slice) của Humidity:

```
Telemetry flow:
  TelemetryServiceImpl
    └─ strategyMap.get(SENSOR_CO2) → Co2MetricServiceImpl.create()
         ├─ parse "co2" from JSON
         ├─ update Co2Sensor.currentCO2
         └─ save Co2Metric(targetCategory="SENSOR_CO2", co2=value)

Setup flow:
  DeviceSetupOrchestrator
    └─ strategyMap.get(SENSOR_CO2) → Co2SetupStrategy.persist()
         └─ create Co2Sensor + Co2SensorLan

Metric query flow:
  MetricServiceStrategy (Co2MetricServiceImpl)
    └─ getLatest / getHistory via Co2MetricDao
```

---

## 3. Danh sách thay đổi chi tiết

### 3.1 Enums (3 files sửa)

#### `DeviceCategory.java`
Thêm 2 enum value mới:
```java
SENSOR_CO2,
SENSOR_LUX
```

#### `MetricDomain.java`
Thêm 2 enum value mới:
```java
CO2,
LUX
```

#### `SensorCategory.java`
Thêm 2 value để đồng bộ:
```java
SENSOR_CO2,
SENSOR_LUX
```

---

### 3.2 Entities (6 files mới)

#### [NEW] `Co2Sensor.java`
- Extends `BaseIoTSensor<Co2SensorLan>`
- Table: `co2_sensor`
- Field: `@Column(name="current_co2") private Double currentCO2`
- `extractBusinessData()` → `new Co2SensorData(this.currentCO2)`
- `getCategory()` → `DeviceCategory.SENSOR_CO2`

#### [NEW] `Co2SensorLan.java`
- Extends `BaseTranslation<Co2Sensor>`
- Table: `co2_sensor_lan`

#### [NEW] `LuxSensor.java`
- Extends `BaseIoTSensor<LuxSensorLan>`
- Table: `lux_sensor`
- Field: `@Column(name="current_lux") private Double currentLux`
- `extractBusinessData()` → `new LuxSensorData(this.currentLux)`
- `getCategory()` → `DeviceCategory.SENSOR_LUX`

#### [NEW] `LuxSensorLan.java`
- Extends `BaseTranslation<LuxSensor>`
- Table: `lux_sensor_lan`

#### [NEW] `Co2Metric.java`
- Extends `BaseMetricData`
- Table: `co2_metrics`
- Field: `@Column(name="co2") private Double co2`
- `setTargetCategory()` validate chỉ accept `"SENSOR_CO2"`

#### [NEW] `LuxMetric.java`
- Extends `BaseMetricData`
- Table: `lux_metrics`
- Field: `@Column(name="lux") private Double lux`
- `setTargetCategory()` validate chỉ accept `"SENSOR_LUX"`

---

### 3.3 DTOs (4 files mới + 1 sửa)

#### [NEW] `Co2SensorData.java`
```java
public record Co2SensorData(Double currentCO2) implements SensorSpecificData {}
```

#### [NEW] `LuxSensorData.java`
```java
public record LuxSensorData(Double currentLux) implements SensorSpecificData {}
```

#### [NEW] `Co2MetricDto.java`
- Fields: `Instant timestamp`, `Double co2`
- Builder pattern + `fromEntity(Co2Metric)` static factory

#### [NEW] `LuxMetricDto.java`
- Fields: `Instant timestamp`, `Double lux`
- Builder pattern + `fromEntity(LuxMetric)` static factory

#### [MODIFY] `SensorSpecificData.java`
Thêm 2 subtype vào sealed interface:
```java
permits ..., Co2SensorData, LuxSensorData
@JsonSubTypes.Type(Co2SensorData.class),
@JsonSubTypes.Type(LuxSensorData.class)
```

---

### 3.4 DAOs (4 files mới)

#### [NEW] `Co2SensorDao.java`
- Extends `BaseIoTSensorDao<Co2Sensor>`
- Pattern y hệt `HumiditySensorDao`

#### [NEW] `LuxSensorDao.java`
- Extends `BaseIoTSensorDao<LuxSensor>`
- Pattern y hệt `HumiditySensorDao`

#### [NEW] `Co2MetricDao.java`
- Extends `BaseEntityDao<Co2Metric>`
- JDBC batch insert: `INSERT INTO co2_metrics (target_category, target_id, timestamp, unix_minute, co2)`
- `findHistory()`, `findHistory(divisor)`, `findLatest()`
- Pattern y hệt `HumidityMetricDao`

#### [NEW] `LuxMetricDao.java`
- Extends `BaseEntityDao<LuxMetric>`
- Table: `lux_metrics`, field: `lux`

---

### 3.5 Setup Strategies (2 files mới)

#### [NEW] `Co2SetupStrategy.java`
- Extends `AbstractDeviceSetupStrategy`
- `getSupportedCategory()` → `DeviceCategory.SENSOR_CO2`
- `persist()`: tạo `Co2Sensor`, attach `Co2SensorLan`
- `rollback()`: xóa bằng `co2SensorDao.deleteById(deviceId)`

#### [NEW] `LuxSetupStrategy.java`
- Pattern y hệt, `getSupportedCategory()` → `DeviceCategory.SENSOR_LUX`

---

### 3.6 Service Interfaces (2 files mới)

#### [NEW] `Co2MetricService.java`
```java
public interface Co2MetricService
    extends TelemetryCRUDServiceStrategy, MetricServiceStrategy, SensorTelemetryServiceStrategy {}
```

#### [NEW] `LuxMetricService.java`
```java
public interface LuxMetricService
    extends TelemetryCRUDServiceStrategy, MetricServiceStrategy, SensorTelemetryServiceStrategy {}
```

---

### 3.7 Service Implementations (2 files mới)

#### [NEW] `Co2MetricServiceImpl.java`
- Implements `Co2MetricService`
- `getSupportedCategory()` → `DeviceCategory.SENSOR_CO2`
- `getSupportedDomain()` → `MetricDomain.CO2`
- `create()`: parse JSON field `"co2"`, update `sensor.currentCO2`, save `Co2Metric`
- Pattern giống hoàn toàn `HumidityMetricServiceImpl`

#### [NEW] `LuxMetricServiceImpl.java`
- Implements `LuxMetricService`
- `getSupportedCategory()` → `DeviceCategory.SENSOR_LUX`
- `getSupportedDomain()` → `MetricDomain.LUX`
- `create()`: parse JSON field `"lux"`, update `sensor.currentLux`, save `LuxMetric`

---

### 3.8 Database Migration (1 file mới)

#### [NEW] `002_Add_CO2_Lux_Sensors.sql`

6 tables: `co2_sensor`, `co2_sensor_lan`, `co2_metrics`, `lux_sensor`, `lux_sensor_lan`, `lux_metrics`

Mỗi metrics table có indexes:
- `idx_XXX_metrics_target` (`target_category`, `target_id`, `timestamp`)
- `idx_XXX_metrics_timestamp` (`timestamp`)
- `idx_XXX_unix_minute` (`unix_minute`)

---

## 4. Điểm quan trọng / Quyết định thiết kế

> [!IMPORTANT]
> **Naming convention:** Dùng prefix `SENSOR_` cho `SENSOR_CO2` và `SENSOR_LUX` trong `DeviceCategory` theo tinh thần comment trong `SensorCategory.java` ("Future refactor: Separate sensor and device types"). Prefix làm rõ đây là sensor, không phải actuator.

> [!NOTE]
> **Auto-registration:** `TelemetryServiceImpl` inject `List<TelemetryCRUDServiceStrategy>` qua Spring — chỉ cần `@Service` + implement interface là được đăng ký vào `strategyMap` mà **không cần sửa** `TelemetryServiceImpl`. Tương tự cho `DeviceSetupOrchestrator`.

> [!CAUTION]
> **`SensorSpecificData` sealed interface:** Bắt buộc phải sửa `permits` clause và `@JsonSubTypes` annotation thủ công — Java không tự mở rộng sealed interface.

---

## 5. Verification Plan

```bash
# Compile check
cd smartroom_server && mvn compile -q
```

Manual:
1. Chạy `002_Add_CO2_Lux_Sensors.sql`, verify 6 tables tạo thành công
2. POST setup request với category `SENSOR_CO2` → Co2Sensor được tạo
3. Simulate telemetry `{"co2": 850.0}` → `co2_metrics` có record
4. Simulate telemetry `{"lux": 300.0}` → `lux_metrics` có record

---

## 6. Tổng kết files

| # | Action | File |
|---|--------|------|
| 1 | MODIFY | `DeviceCategory.java` |
| 2 | MODIFY | `MetricDomain.java` |
| 3 | MODIFY | `SensorCategory.java` |
| 4 | MODIFY | `SensorSpecificData.java` |
| 5 | NEW | `Co2Sensor.java` |
| 6 | NEW | `Co2SensorLan.java` |
| 7 | NEW | `LuxSensor.java` |
| 8 | NEW | `LuxSensorLan.java` |
| 9 | NEW | `Co2Metric.java` |
| 10 | NEW | `LuxMetric.java` |
| 11 | NEW | `Co2SensorData.java` |
| 12 | NEW | `LuxSensorData.java` |
| 13 | NEW | `Co2MetricDto.java` |
| 14 | NEW | `LuxMetricDto.java` |
| 15 | NEW | `Co2SensorDao.java` |
| 16 | NEW | `LuxSensorDao.java` |
| 17 | NEW | `Co2MetricDao.java` |
| 18 | NEW | `LuxMetricDao.java` |
| 19 | NEW | `Co2SetupStrategy.java` |
| 20 | NEW | `LuxSetupStrategy.java` |
| 21 | NEW | `Co2MetricService.java` |
| 22 | NEW | `LuxMetricService.java` |
| 23 | NEW | `Co2MetricServiceImpl.java` |
| 24 | NEW | `LuxMetricServiceImpl.java` |
| 25 | NEW | `002_Add_CO2_Lux_Sensors.sql` |

**Tổng: 4 sửa + 21 tạo mới = 25 files**
