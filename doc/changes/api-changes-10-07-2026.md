# API Changes - Device Status Metric Domain

## 1. Mục tiêu

Tài liệu này ghi nhận các thay đổi ở tầng RESTful API phục vụ cho Domain mới của Metrics:
1. **Device Status Metric Domain**: Domain `DEVICE_STATUS` được bổ sung vào API truy vấn metrics thống nhất (`GET /api/v1/metrics`), cho phép lấy trạng thái mới nhất hoặc lịch sử trạng thái của các thiết bị chấp hành (Actuator) như Đèn (`LIGHT`), Quạt (`FAN`), Điều hòa (`AIR_CONDITION`).

---

## 2. Danh sách API thay đổi

| Method | Endpoint | Mô tả thay đổi |
| :----- | :------- | :------------- |
| **GET** | `/api/v1/metrics?domain=DEVICE_STATUS` | Bổ sung domain `DEVICE_STATUS` cho API truy vấn metrics hiện có. Hỗ trợ lấy trạng thái mới nhất (`latest=true`) hoặc lịch sử trạng thái (`latest=false`) của thiết bị chấp hành. |

---

## 3. Chi tiết API Contract & Tham số

### A. Truy vấn trạng thái thiết bị (`GET /api/v1/metrics?domain=DEVICE_STATUS`)

> Lấy dữ liệu trạng thái (mới nhất hoặc lịch sử) cho các thiết bị chấp hành: Đèn, Quạt, Điều hòa.

#### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| `domain` | `MetricDomain` | Lĩnh vực metric. Giá trị: `DEVICE_STATUS` | Có |
| `category` | `string` | Loại thiết bị. Giá trị: `LIGHT` \| `FAN` \| `AIR_CONDITION` (xem [DeviceCategory](#devicecategory)) | Có |
| `targetId` | `Long` | ID của thiết bị mục tiêu | Có |
| `latest` | `boolean` | `true` để lấy trạng thái mới nhất, `false` để lấy lịch sử | Mặc định: `false` |
| `from` | `Instant` (ISO-8601) | Thời gian bắt đầu truy vấn lịch sử | Bắt buộc nếu `latest=false` |
| `to` | `Instant` (ISO-8601) | Thời gian kết thúc truy vấn lịch sử | Bắt buộc nếu `latest=false` |

#### Response Example (Latest Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-10T08:00:00Z",
        "targetCategory": "LIGHT",
        "targetId": 1,
        "statusData": {
            "power": "ON",
            "level": 80
        }
    }
}
```

#### Response Example (Historical Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": [
        {
            "timestamp": "2026-07-10T08:00:00Z",
            "targetCategory": "LIGHT",
            "targetId": 1,
            "statusData": {
                "power": "ON",
                "level": 80
            }
        },
        {
            "timestamp": "2026-07-10T08:15:00Z",
            "targetCategory": "LIGHT",
            "targetId": 1,
            "statusData": {
                "power": "OFF",
                "level": 0
            }
        }
    ]
}
```

> **Lưu ý:** Trường `statusData` có cấu trúc JSON động, phụ thuộc vào loại thiết bị (`category`). Xem chi tiết tại mục [Status Data theo loại thiết bị](#status-data-theo-loại-thiết-bị).

#### Response khi không tìm thấy dữ liệu (Latest - 404 Not Found)

```json
{
    "status": 404,
    "message": "Not Found"
}
```

---

### B. Status Data theo loại thiết bị

#### 1. Light (LIGHT)

Dành cho thiết bị chiếu sáng. Cấu trúc `statusData`:

| Trường | Loại | Mô tả |
| :----- | :--- | :---- |
| `power` | `string` | Trạng thái nguồn: `ON` \| `OFF` |
| `level` | `integer` | Mức độ sáng (0-100), `null` nếu không hỗ trợ |

**Ví dụ:**
```json
{
    "power": "ON",
    "level": 80
}
```

#### 2. Fan (FAN)

Dành cho thiết bị quạt. Cấu trúc `statusData`:

| Trường | Loại | Mô tả |
| :----- | :--- | :---- |
| `power` | `string` | Trạng thái nguồn: `ON` \| `OFF` |
| `speed` | `integer` | Tốc độ quạt, `null` nếu không hỗ trợ |
| `duration` | `integer` | Thời gian hẹn giờ (phút), `null` nếu không hỗ trợ |
| `mode` | `string` | Chế độ hoạt động, `null` nếu không hỗ trợ |
| `swing` | `string` | Trạng thái đảo gió, `null` nếu không hỗ trợ |
| `light` | `string` | Trạng thái đèn quạt: `ON` \| `OFF`, `null` nếu không có |

**Ví dụ:**
```json
{
    "power": "ON",
    "speed": 3,
    "duration": 60,
    "mode": "NORMAL",
    "swing": "ON",
    "light": "OFF"
}
```

#### 3. AirCondition (AIR_CONDITION)

Dành cho thiết bị điều hòa nhiệt độ. Cấu trúc `statusData`:

| Trường | Loại | Mô tả |
| :----- | :--- | :---- |
| `power` | `string` | Trạng thái nguồn: `ON` \| `OFF` |
| `temperature` | `integer` | Nhiệt độ cài đặt (°C), `null` nếu không hỗ trợ |
| `mode` | `string` | Chế độ hoạt động, `null` nếu không hỗ trợ |
| `fanSpeed` | `integer` | Tốc độ quạt dàn lạnh, `null` nếu không hỗ trợ |
| `swing` | `string` | Trạng thái đảo gió, `null` nếu không hỗ trợ |
| `duration` | `integer` | Thời gian hẹn giờ (phút), `null` nếu không hỗ trợ |

**Ví dụ:**
```json
{
    "power": "ON",
    "temperature": 25,
    "mode": "COOL",
    "fanSpeed": 2,
    "swing": "OFF",
    "duration": null
}
```

---

### C. Cập nhật MetricDomain Enum

| Giá trị | Mô tả |
| :------ | :---- |
| `ENERGY` | (Đã có) Các chỉ số liên quan đến năng lượng |
| `HEALTH` | (Đã có) Các chỉ số liên quan đến môi trường |
| `DEVICE_STATUS` | **(Mới)** Trạng thái thiết bị chấp hành (bật/tắt, mức độ, chế độ) |

---

### D. DeviceCategory

Các giá trị `category` hợp lệ khi truy vấn với domain `DEVICE_STATUS`:

| Giá trị | Mô tả |
| :------ | :---- |
| `LIGHT` | Thiết bị chiếu sáng |
| `FAN` | Thiết bị quạt |
| `AIR_CONDITION` | Điều hòa nhiệt độ |

> **Lưu ý:** Truyền `category` không hợp lệ hoặc không phải loại thiết bị chấp hành sẽ trả về `400 Bad Request`.

---

## 4. Cơ chế Backup trạng thái thiết bị

Hệ thống định kỳ sao lưu trạng thái của tất cả thiết bị chấp hành đang hoạt động vào bảng `device_status_metrics` thông qua `DeviceStatusMetricJob`. Cơ chế hoạt động:

1. **Phát hiện thay đổi**: So sánh `device_version` hiện tại của thiết bị với phiên bản đã lưu gần nhất. Chỉ lưu khi có sự thay đổi.
2. **Dữ liệu lưu**: Mỗi bản ghi bao gồm `targetCategory`, `targetId`, `timestamp`, `statusData` (JSON động), và `deviceVersion`.
3. **Mục đích**: Cung cấp dữ liệu lịch sử trạng thái thiết bị phục vụ truy vấn qua API `GET /api/v1/metrics?domain=DEVICE_STATUS&latest=false`.

> **Lưu ý:** Đối với truy vấn trạng thái mới nhất (`latest=true`), API trả về bản ghi gần nhất trong bảng `device_status_metrics`, không phải trạng thái real-time từ thiết bị.
