# API Changes - Temperature & Humidity Metric Domains

## 1. Mục tiêu

Tài liệu này ghi nhận các thay đổi ở tầng RESTful API phục vụ cho hai Domain mới của Metrics:
1. **Temperature Metric Domain**: Domain `TEMPERATURE` được bổ sung vào API truy vấn metrics thống nhất (`GET /api/v1/metrics`), cho phép lấy giá trị nhiệt độ mới nhất hoặc lịch sử của cảm biến nhiệt độ.
2. **Humidity Metric Domain**: Domain `HUMIDITY` được bổ sung vào API truy vấn metrics thống nhất (`GET /api/v1/metrics`), cho phép lấy giá trị độ ẩm mới nhất hoặc lịch sử của cảm biến độ ẩm.

Ngoài ra, hệ thống ghi nhận telemetry cũng được mở rộng:
- **Nhiệt độ**: Khi nhận telemetry nhiệt độ qua Gateway, dữ liệu được ghi đồng thời vào bảng `temperature_value` (cũ, giữ nguyên) và `temperature_metrics` (mới).
- **Độ ẩm**: Khi nhận telemetry độ ẩm qua Gateway, dữ liệu được ghi trực tiếp vào bảng `humidity_metrics` (mới) thông qua `HumidityMetricServiceImpl`.

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
