# API Changes - Temperature & Humidity Metric Domains

## 1. Mục tiêu

Tài liệu này ghi nhận các thay đổi ở tầng RESTful API phục vụ cho hai Domain mới của Metrics:
1. **Temperature Metric Domain**: Domain `TEMPERATURE` được bổ sung vào API truy vấn metrics thống nhất (`GET /api/v1/metrics`), cho phép lấy giá trị nhiệt độ mới nhất hoặc lịch sử của cảm biến nhiệt độ.
2. **Humidity Metric Domain**: Domain `HUMIDITY` được bổ sung vào API truy vấn metrics thống nhất (`GET /api/v1/metrics`), cho phép lấy giá trị độ ẩm mới nhất hoặc lịch sử của cảm biến độ ẩm.

Ngoài ra, hệ thống ghi nhận telemetry cũng được mở rộng:
- **Nhiệt độ**: Khi nhận telemetry nhiệt độ qua Gateway, dữ liệu được ghi đồng thời vào bảng `temperature_value` (cũ, giữ nguyên) và `temperature_metrics` (mới).
- **Độ ẩm**: Khi nhận telemetry độ ẩm qua Gateway, dữ liệu được ghi vào bảng `humidity_metrics` (mới) thông qua `HumidityMetricServiceImpl` (đồng thời cập nhật `currentHumidity` của cảm biến độ ẩm).

---

## 2. Danh sách API thay đổi

| Method | Endpoint | Mô tả thay đổi |
| :----- | :------- | :------------- |
| **GET** | `/api/v1/metrics?domain=TEMPERATURE` | Bổ sung domain `TEMPERATURE` cho API truy vấn metrics hiện có. Hỗ trợ lấy nhiệt độ mới nhất (`latest=true`) hoặc lịch sử (`latest=false`). |
| **GET** | `/api/v1/metrics?domain=HUMIDITY` | Bổ sung domain `HUMIDITY` cho API truy vấn metrics hiện có. Hỗ trợ lấy độ ẩm mới nhất (`latest=true`) hoặc lịch sử (`latest=false`). |

---

## 3. Chi tiết API Contract & Tham số

### A. Truy vấn nhiệt độ (`GET /api/v1/metrics?domain=TEMPERATURE`)

> Lấy dữ liệu nhiệt độ (mới nhất hoặc lịch sử) từ bảng `temperature_metrics`. Dữ liệu được ghi nhận đồng thời với bảng `temperature_value` truyền thống.

#### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| `domain` | `MetricDomain` | Lĩnh vực metric. Giá trị: `TEMPERATURE` | Có |
| `targetId` | `Long` | ID của cảm biến nhiệt độ | Có |
| `latest` | `boolean` | `true` để lấy giá trị mới nhất, `false` để lấy lịch sử | Mặc định: `false` |
| `from` | `Instant` (ISO-8601) | Thời gian bắt đầu truy vấn lịch sử | Bắt buộc nếu `latest=false` |
| `to` | `Instant` (ISO-8601) | Thời gian kết thúc truy vấn lịch sử | Bắt buộc nếu `latest=false` |

#### Response Example (Latest Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-14T10:00:00Z",
        "temperature": 25.5
    },
    "timestamp": "2026-07-14T10:00:01Z"
}
```

#### Response Example (Historical Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": [
        {
            "timestamp": "2026-07-14T09:00:00Z",
            "temperature": 25.0
        },
        {
            "timestamp": "2026-07-14T10:00:00Z",
            "temperature": 25.5
        }
    ],
    "timestamp": "2026-07-14T10:00:01Z"
}
```

#### Response khi không tìm thấy dữ liệu (Latest - 404 Not Found)

```json
{
    "status": 404,
    "message": "Not Found"
}
```

---

### B. Truy vấn độ ẩm (`GET /api/v1/metrics?domain=HUMIDITY`)

> Lấy dữ liệu độ ẩm (mới nhất hoặc lịch sử) từ bảng `humidity_metrics`. Đây là nơi lưu trữ chính cho dữ liệu độ ẩm.

#### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| `domain` | `MetricDomain` | Lĩnh vực metric. Giá trị: `HUMIDITY` | Có |
| `targetId` | `Long` | ID của cảm biến độ ẩm | Có |
| `latest` | `boolean` | `true` để lấy giá trị mới nhất, `false` để lấy lịch sử | Mặc định: `false` |
| `from` | `Instant` (ISO-8601) | Thời gian bắt đầu truy vấn lịch sử | Bắt buộc nếu `latest=false` |
| `to` | `Instant` (ISO-8601) | Thời gian kết thúc truy vấn lịch sử | Bắt buộc nếu `latest=false` |

#### Response Example (Latest Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-14T10:00:00Z",
        "humidity": 65.5
    },
    "timestamp": "2026-07-14T10:00:01Z"
}
```

#### Response Example (Historical Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": [
        {
            "timestamp": "2026-07-14T09:00:00Z",
            "humidity": 68.0
        },
        {
            "timestamp": "2026-07-14T10:00:00Z",
            "humidity": 65.5
        }
    ],
    "timestamp": "2026-07-14T10:00:01Z"
}
```

#### Response khi không tìm thấy dữ liệu (Latest - 404 Not Found)

```json
{
    "status": 404,
    "message": "Not Found"
}
```

---

### C. Cập nhật MetricDomain Enum

| Giá trị | Mô tả |
| :------ | :---- |
| `ENERGY` | (Đã có) Các chỉ số liên quan đến năng lượng |
| `HEALTH` | (Đã có) Các chỉ số liên quan đến môi trường (nhiệt độ, độ ẩm) |
| `DEVICE_STATUS` | (Đã có) Trạng thái thiết bị chấp hành (bật/tắt, mức độ, chế độ) |
| `TEMPERATURE` | **(Mới)** Chỉ số nhiệt độ từ cảm biến, lưu trong bảng `temperature_metrics` |
| `HUMIDITY` | **(Mới)** Chỉ số độ ẩm từ cảm biến, lưu trong bảng `humidity_metrics` |

---

## 4. Kiến trúc xử lý

### A. Luồng ghi nhận Telemetry Nhiệt độ

```
Gateway Telemetry (tempC)
    → TemperatureValueServiceImpl.create()
        ├── Bước 1: Ghi vào bảng temperature_value (cũ, giữ nguyên)
        ├── Bước 2: Cập nhật currentValue trong Temperature sensor
        └── Bước 3 (MỚI): Ghi vào bảng temperature_metrics
                            thông qua TemperatureMetricDao
```

### B. Luồng ghi nhận Telemetry Độ ẩm

```
Gateway Telemetry (humidity)
    → HumidityMetricServiceImpl.create() (MỚI)
        ├── Bước 1: Cập nhật currentHumidity trong HumiditySensor
        └── Bước 2: Ghi vào bảng humidity_metrics
                    thông qua HumidityMetricDao
```

### C. Luồng truy vấn Metric

```
GET /api/v1/metrics?domain=TEMPERATURE&targetId=1&latest=true
    → MetricOrchestratorService
        → TemperatureMetricServiceImpl.getLatest() (MỚI)
            → TemperatureMetricDao.findLatest()
                → Bảng temperature_metrics

GET /api/v1/metrics?domain=HUMIDITY&targetId=1&latest=true
    → MetricOrchestratorService
        → HumidityMetricServiceImpl.getLatest() (MỚI)
            → HumidityMetricDao.findLatest()
                → Bảng humidity_metrics
```

---

## 5. Cập nhật Database

### Bảng mới

| Bảng | Mục đích |
| :--- | :------- |
| `temperature_metrics` | Lưu chỉ số nhiệt độ, kế thừa từ `BaseMetricData`. Cột `temperature DOUBLE NOT NULL`. |
| `humidity_metrics` | Lưu chỉ số độ ẩm, kế thừa từ `BaseMetricData`. Cột `humidity DOUBLE NOT NULL`. |

### Bảng bị loại bỏ

| Bảng | Lý do |
| :--- | :---- |
| `humidity_value` | Thay thế bởi `humidity_metrics`. Entity `HumidityValue.java` đã bị xoá. |

---

# API Changes - CO₂ & Lux Sensor Integration

## Date: 2026-07-16

## 1. Mục tiêu

Tài liệu này ghi nhận các thay đổi phục vụ việc tích hợp hai loại cảm biến mới:
1. **CO₂ Sensor** (`SENSOR_CO2`): Lưu nồng độ CO₂ (ppm) vào `co2_metrics`. Telemetry field: `"co2"`.
2. **Lux Sensor** (`SENSOR_LUX`): Lưu cường độ ánh sáng (lx) vào `lux_metrics`. Telemetry field: `"lux"`.

Ngoài ra, `HUMIDITY` được kích hoạt trong `SensorMetadataService` và `SensorTelemetryService` (trước đây chỉ được ghi nhận qua Metric, chưa xuất hiện trong API metadata/telemetry).

---

## 2. Danh sách API thay đổi

| Method | Endpoint | Mô tả thay đổi |
| :----- | :------- | :------------- |
| **GET** | `/api/v1/metrics?domain=CO2` | Thêm domain mới `CO2`. Hỗ trợ latest và history cho CO₂ sensor. |
| **GET** | `/api/v1/metrics?domain=LUX` | Thêm domain mới `LUX`. Hỗ trợ latest và history cho Lux sensor. |
| **GET** | `/api/v1/rooms/{roomId}/sensors` | Mở rộng `category` filter thêm `SENSOR_CO2`, `SENSOR_LUX`, `HUMIDITY`. |
| **GET** | `/api/v1/sensors/all` | Mở rộng `category` filter thêm `SENSOR_CO2`, `SENSOR_LUX`, `HUMIDITY`. |
| **GET** | `/api/v1/rooms/{roomId}/sensors/count` | Count giờ bao gồm cả Co2Sensor + LuxSensor + HumiditySensor. |
| **GET** | `/api/v1/sensors/{sensorId}/history` | Mở rộng `category` thêm `SENSOR_CO2`, `SENSOR_LUX`, `HUMIDITY`. |
| **GET** | `/api/v1/sensors/natural/{naturalId}/history` | Mở rộng `category` thêm `SENSOR_CO2`, `SENSOR_LUX`, `HUMIDITY`. |

---

## 3. Chi tiết API Contract

### A. Truy vấn CO₂ (`GET /api/v1/metrics?domain=CO2`)

#### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| `domain` | `MetricDomain` | Giá trị: `CO2` | Có |
| `targetId` | `Long` | ID của CO₂ sensor | Có |
| `latest` | `boolean` | `true` để lấy mới nhất | Mặc định: `false` |
| `from` | `Instant` | Thời gian bắt đầu | Bắt buộc nếu `latest=false` |
| `to` | `Instant` | Thời gian kết thúc | Bắt buộc nếu `latest=false` |

#### Response (Latest - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-16T10:00:00Z",
        "co2": 420.5
    }
}
```

### B. Truy vấn Lux (`GET /api/v1/metrics?domain=LUX`)

#### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| `domain` | `MetricDomain` | Giá trị: `LUX` | Có |
| `targetId` | `Long` | ID của Lux sensor | Có |
| `latest` | `boolean` | `true` để lấy mới nhất | Mặc định: `false` |
| `from` | `Instant` | Thời gian bắt đầu | Bắt buộc nếu `latest=false` |
| `to` | `Instant` | Thời gian kết thúc | Bắt buộc nếu `latest=false` |

#### Response (Latest - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-16T10:00:00Z",
        "lux": 850.0
    }
}
```

---

## 4. Cập nhật Enum

### DeviceCategory

| Giá trị | Mô tả |
| :------ | :---- |
| `SENSOR_CO2` | **(Mới)** Cảm biến nồng độ CO₂ |
| `SENSOR_LUX` | **(Mới)** Cảm biến cường độ ánh sáng |

### MetricDomain

| Giá trị | Mô tả |
| :------ | :---- |
| `CO2` | **(Mới)** Chỉ số nồng độ CO₂ từ cảm biến |
| `LUX` | **(Mới)** Chỉ số cường độ ánh sáng từ cảm biến |
