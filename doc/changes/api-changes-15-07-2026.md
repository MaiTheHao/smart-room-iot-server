# API Changes - Category Integration into Sensor Metric Services

## Date: 2026-07-15

## 1. Mục tiêu

Tham số `category` trong API `GET /api/v1/metrics` cho bốn domain cảm biến môi trường (**TEMPERATURE**, **HUMIDITY**, **CO2**, **LUX**) trước đây bị **bỏ qua hoàn toàn**. Cần implement để:

- `category = null | "" | "DEFAULT"` → Hành vi hiện tại (query theo sensor ID)
- `category = "ROOM"` → Tổng hợp từ toàn bộ cảm biến trong phòng theo thuật toán quy định
- Giá trị khác → Ném `BadRequestException` (400)

Ngữ nghĩa `targetId`:
- `DEFAULT` → `targetId` = ID cảm biến đơn lẻ
- `ROOM` → `targetId` = ID phòng (roomId)

Ngoài ra, Enum `SensorMetricCategory` được tạo ra để chuẩn hóa việc parse và validate tham số `category`.

---

## 2. Danh sách API thay đổi

| Method | Endpoint | Mô tả thay đổi |
| :----- | :------- | :------------- |
| **GET** | `/api/v1/metrics?domain=TEMPERATURE` | Bổ sung tham số `category`. Hỗ trợ ROOM: AVG `currentValue` của tất cả Temperature sensor trong phòng. |
| **GET** | `/api/v1/metrics?domain=HUMIDITY` | Bổ sung tham số `category`. Hỗ trợ ROOM: Median `currentHumidity` của tất cả HumiditySensor trong phòng. |
| **GET** | `/api/v1/metrics?domain=CO2` | Bổ sung tham số `category`. Hỗ trợ ROOM: AVG `currentCO2` của tất cả Co2Sensor trong phòng. |
| **GET** | `/api/v1/metrics?domain=LUX` | Bổ sung tham số `category`. Hỗ trợ ROOM: Median `currentLux` của tất cả LuxSensor trong phòng. |

---

## 3. Chi tiết API Contract & Tham số

### A. Tham số `category` dùng chung cho 4 domain

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| `category` | `string` | Phạm vi truy vấn. Giá trị: `DEFAULT` (hoặc null/empty) → truy vấn theo sensor ID; `ROOM` → tổng hợp theo phòng | Không. Mặc định: `DEFAULT` |

> Nếu truyền giá trị khác `DEFAULT` hoặc `ROOM`, API trả về HTTP 400 BadRequest.

---

### B. Truy vấn theo phòng (`category=ROOM`)

Khi `category=ROOM`, tham số `targetId` được hiểu là **roomId**. Kết quả trả về là giá trị tổng hợp từ tất cả cảm biến active trong phòng đó.

#### Thuật toán tổng hợp

| Domain | getLatest | getHistory | Response field(s) |
| :--- | :--- | :--- | :--- |
| **TEMPERATURE** | AVG(`currentValue`) | AVG per time bucket | `avgTemp` |
| **HUMIDITY** | Median(`currentHumidity`) | Median per time bucket | `medianHumidity` |
| **LUX** | Median(`currentLux`) | Median per time bucket | `medianLux` |
| **CO2** | AVG + MAX(`currentCO2`) | AVG + MAX per time bucket | `avgCo2`, `maxCo2` |

#### Response Example: TEMPERATURE ROOM getLatest

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-15T10:00:00Z",
        "avgTemp": 26.3
    },
    "timestamp": "2026-07-15T10:00:01Z"
}
```

#### Response Example: HUMIDITY ROOM getLatest

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-15T10:00:00Z",
        "medianHumidity": 64.2
    },
    "timestamp": "2026-07-15T10:00:01Z"
}
```

#### Response Example: LUX ROOM getHistory

```json
{
    "status": 200,
    "message": "Success",
    "data": [
        {
            "timestamp": "2026-07-15T09:00:00Z",
            "medianLux": 810.0
        },
        {
            "timestamp": "2026-07-15T10:00:00Z",
            "medianLux": 850.0
        }
    ],
    "timestamp": "2026-07-15T10:00:01Z"
}
```

#### Response Example: CO2 ROOM getLatest

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-15T10:00:00Z",
        "avgCo2": 450.0,
        "maxCo2": 800.0
    },
    "timestamp": "2026-07-15T10:00:01Z"
}
```

> `avgCo2` dùng cho lưu trữ lịch sử và hiển thị đồ thị.  
> `maxCo2` dùng cho Automation (kích hoạt quạt thông gió khi phát hiện vùng ngột ngạt).

#### Response Example: CO2 ROOM getHistory

```json
{
    "status": 200,
    "message": "Success",
    "data": [
        {
            "timestamp": "2026-07-15T09:00:00Z",
            "avgCo2": 415.0,
            "maxCo2": 720.0
        },
        {
            "timestamp": "2026-07-15T10:00:00Z",
            "avgCo2": 420.5,
            "maxCo2": 750.0
        }
    ],
    "timestamp": "2026-07-15T10:00:01Z"
}
```

#### Error Response: Invalid category (400 BadRequest)

```json
{
    "status": 400,
    "message": "Invalid category 'INVALID' for sensor metrics. Accepted values: [DEFAULT, ROOM]"
}
```

---

### C. Cập nhật Enum

#### SensorMetricCategory (MỚI)

| Giá trị | Mô tả |
| :------ | :---- |
| `DEFAULT` | Truy vấn theo cảm biến đơn lẻ (hành vi hiện tại). `targetId` = sensor ID |
| `ROOM` | Tổng hợp theo phòng. `targetId` = room ID |

`SensorMetricCategory.fromString()`:
- `null` / `""` (blank) → `DEFAULT`
- `"ROOM"` / `"room"` → `ROOM`
- Giá trị khác → `BadRequestException` với message thân thiện
---

---

# API / Architecture Changes - Sensor Strategy Integration

## Date: 2026-07-17

## 1. Mục tiêu

Tích hợp 3 loại cảm biến mới (HumiditySensor, Co2Sensor, LuxSensor) vào Dynamic Rule Engine để:
- Hỗ trợ đọc trạng thái cảm biến trong Rule Condition (dataSource=SENSOR)
- Hỗ trợ tổng hợp chỉ số phòng (dataSource=ROOM) theo CALC.md
- Chuẩn hóa `SensorMetadataServiceStrategy` với Generic type và method naming rõ ràng

## 2. Danh sách thay đổi

### 2.1. Chuẩn hóa Interface `SensorMetadataServiceStrategy<T>`

**File:** `service/strategy/SensorMetadataServiceStrategy.java`

| Thay đổi | Chi tiết |
| :------- | :------- |
| **Generic type** | Thêm `<T extends BaseIoTSensor<?>>` để type-safe entity access |
| **Rename methods** | `getSensorByRoomId` → `getSensorMetadataByRoomId` (return DTO) |
| | `getAllSensor` → `getAllSensorMetadata` (return DTO) |
| | `getSensorById` → `getSensorMetadataById` (return DTO) |
| | `getSensorByNaturalId` → `getSensorMetadataByNaturalId` (return DTO) |
| **New methods** | `T getSensorById(Long id)` — trả về entity thay vì DTO |
| | `T getSensorByNaturalId(String naturalId)` — trả về entity thay vì DTO |

**Impact:** Cả 5 service implementations phải cập nhật (rename 4 methods + implement 2 new).

### 2.2. Generic Binding trên 5 Service Interfaces

| Interface | Bound to |
| :-------- | :------- |
| `TemperatureService` | `SensorMetadataServiceStrategy<Temperature>` |
| `PowerConsumptionService` | `SensorMetadataServiceStrategy<PowerConsumption>` |
| `HumidityMetricService` | `SensorMetadataServiceStrategy<HumiditySensor>` |
| `Co2MetricService` | `SensorMetadataServiceStrategy<Co2Sensor>` |
| `LuxMetricService` | `SensorMetadataServiceStrategy<LuxSensor>` |

### 2.3. Dispatcher Update

**File:** `service/impl/SensorMetadataServiceImpl.java`

- `Map<DeviceCategory, SensorMetadataServiceStrategy>` → `Map<DeviceCategory, SensorMetadataServiceStrategy<?>>`
- 6 call sites updated với method names mới

### 2.4. 3 SensorStateStrategy Implementations MỚI

| File | `supports()` | Property | Getter |
| :--- | :------------ | :------- | :----- |
| `HumiditySensorStateStrategy.java` | `HUMIDITY` | `"humidity"` | `sensor.getCurrentHumidity()` |
| `Co2SensorStateStrategy.java` | `SENSOR_CO2` | `"co2"` | `sensor.getCurrentCO2()` |
| `LuxSensorStateStrategy.java` | `SENSOR_LUX` | `"lux"` | `sensor.getCurrentLux()` |

Các strategy được `@Component` auto-discover bởi `SensorDataSourceStrategy`.

### 2.5. RoomDataSourceStrategy — 4 Property MỚI

**File:** `scheduler/dynamic/rule/strategy/impl/RoomDataSourceStrategy.java`

| Property | Phương pháp | Nguồn | Mô tả |
| :------- | :---------- | :---- | :---- |
| `avg_humidity` | **Median** | `HumidityMetricDao.findCurrentValuesByRoomId()` + `Calculator.median()` | Độ ẩm trung vị phòng |
| `avg_lux` | **Median** | `LuxMetricDao.findCurrentValuesByRoomId()` + `Calculator.median()` | Ánh sáng trung vị phòng |
| `avg_co2` | **Mean** | `Co2MetricDao.findLatestByRoomId().getAvgCo2()` | CO2 trung bình phòng |
| `max_co2` | **Max** | `Co2MetricDao.findLatestByRoomId().getMaxCo2()` | CO2 lớn nhất phòng (cho automation) |

### 2.6. Cập nhật API Doc `rule.md`

- Bảng `DeviceCategory`: thêm `HUMIDITY`, `SENSOR_CO2`, `SENSOR_LUX`
- Mục SENSOR: thêm category + property cho 3 cảm biến mới
- Mục ROOM: thêm 4 properties `avg_humidity`, `avg_lux`, `avg_co2`, `max_co2`

---

## 3. Files thay đổi / tạo mới

### Files đã sửa (13)

| # | File | Thay đổi |
|---|------|---------|
| 1 | `SensorMetadataServiceStrategy.java` | Generic + rename 4 + add 2 methods |
| 2 | `TemperatureService.java` | Generic binding `<Temperature>` |
| 3 | `PowerConsumptionService.java` | Generic binding `<PowerConsumption>` |
| 4 | `HumidityMetricService.java` | Generic binding `<HumiditySensor>` |
| 5 | `Co2MetricService.java` | Generic binding `<Co2Sensor>` |
| 6 | `LuxMetricService.java` | Generic binding `<LuxSensor>` |
| 7 | `TemperatureServiceImpl.java` | Rename 4 methods + add 2 new |
| 8 | `PowerConsumptionServiceImpl.java` | Rename 4 methods + add 2 new |
| 9 | `HumidityMetricServiceImpl.java` | Rename 4 methods + add 2 new |
| 10 | `Co2MetricServiceImpl.java` | Rename 4 methods + add 2 new |
| 11 | `LuxMetricServiceImpl.java` | Rename 4 methods + add 2 new |
| 12 | `SensorMetadataServiceImpl.java` | Generic wildcard + 6 call sites |
| 13 | `RoomDataSourceStrategy.java` | Inject 3 DAOs + 4 properties |

### Files đã tạo mới (3)

| # | File |
|---|------|
| 14 | `HumiditySensorStateStrategy.java` |
| 15 | `Co2SensorStateStrategy.java` |
| 16 | `LuxSensorStateStrategy.java` |

---

## 4. Rule Engine — Cấu trúc resourceParam mới

### dataSource=SENSOR — bổ sung category

```json
{
  "dataSource": "SENSOR",
  "resourceParam": {
    "category": "HUMIDITY",
    "sensorId": 1,
    "property": "humidity"
  }
}
```

Hỗ trợ các category mới: `HUMIDITY`, `SENSOR_CO2`, `SENSOR_LUX`
Property tương ứng: `humidity`, `co2`, `lux`

### dataSource=ROOM — bổ sung property

```json
{
  "dataSource": "ROOM",
  "resourceParam": {
    "roomId": 1,
    "property": "avg_humidity"
  }
}
```

Hỗ trợ property mới: `avg_humidity`, `avg_lux`, `avg_co2`, `max_co2`

---

## 5. Lưu ý khi migrate / backward compatibility

- `SensorMetadataServiceStrategy` cũ (raw type) sẽ không compile được — tất cả implementations phải được update generic binding
- Các method cũ `getSensorByRoomId`, `getAllSensor`, `getSensorById`, `getSensorByNaturalId` đã được rename → nếu có code ngoài gọi trực tiếp các method này sẽ bị lỗi compile
- Các method mới `getSensorById` (entity) và `getSensorByNaturalId` (entity) KHÔNG conflict với method cũ vì method cũ đã được rename
- `SensorStateStrategy` không thay đổi interface — 3 implementations mới auto-register
- `RoomDataSourceStrategy` thêm 4 properties — backward compatible (các property cũ vẫn hoạt động)
