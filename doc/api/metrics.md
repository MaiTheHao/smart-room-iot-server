# Metric Module

## Quản lý và Truy vấn các chỉ số hệ thống (Metrics)

---

## 1. Energy Domain (ENERGY)

<details>
<summary><b>GET</b> <code>/api/v1/metrics?domain=ENERGY</code> - Truy vấn chỉ số năng lượng</summary>

> Lấy dữ liệu chỉ số năng lượng (mới nhất hoặc lịch sử) cho các thiết bị như Đèn, Điều hòa, Quạt.

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| domain | string | Lĩnh vực metric. Giá trị: `ENERGY` | Có |
| category | string | Loại thiết bị (xem [EnergyMetricCategory](#energymetriccategory) dưới Enumerations) | Có |
| targetId | Long | ID của thiết bị mục tiêu | Có |
| latest | boolean | `true` để lấy giá trị mới nhất, `false` để lấy lịch sử | Mặc định: `false` |
| from | Instant | Thời gian bắt đầu (ISO-8601) | Bắt buộc nếu `latest=false` |
| to | Instant | Thời gian kết thúc (ISO-8601) | Bắt buộc nếu `latest=false` |

### Response Example (Latest Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-04-18T10:00:00Z",
        "voltage": 220.5,
        "current": 0.45,
        "power": 95.2,
        "energy": 1240.5,
        "frequency": 50.0,
        "powerFactor": 0.95
    },
    "timestamp": "2026-04-18T10:01:00Z"
}
```

### Response Example (Historical Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": [
        {
            "timestamp": "2026-04-18T10:00:00Z",
            "voltage": 220.5,
            "current": 0.45,
            "power": 95.2,
            "energy": 1240.5,
            "frequency": 50.0,
            "powerFactor": 0.95
        },
        {
            "timestamp": "2026-04-18T10:15:00Z",
            "voltage": 219.8,
            "current": 0.44,
            "power": 92.1,
            "energy": 1240.55,
            "frequency": 50.0,
            "powerFactor": 0.94
        }
    ],
    "timestamp": "2026-04-18T10:20:00Z"
}
```

</details>

<br>

---

## 2. Health Domain (HEALTH)

<details>
<summary><b>GET</b> <code>/api/v1/metrics?domain=HEALTH</code> - Truy vấn chỉ số sức khỏe/môi trường</summary>

> (Đang phát triển) Truy vấn các chỉ số như nhiệt độ, độ ẩm.

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| domain | string | Lĩnh vực metric. Giá trị: `HEALTH` | Có |
| category | string | Loại cảm biến (ví dụ: TEMPERATURE) | Có |
| targetId | Long | ID của cảm biến/phòng | Có |
| latest | boolean | `true` để lấy giá trị mới nhất, `false` để lấy lịch sử | Mặc định: `false` |

</details>

<br>

---

## 3. Device Status Domain (DEVICE_STATUS)

<details>
<summary><b>GET</b> <code>/api/v1/metrics?domain=DEVICE_STATUS</code> - Truy vấn trạng thái thiết bị chấp hành</summary>

> Lấy dữ liệu trạng thái (mới nhất hoặc lịch sử) cho các thiết bị chấp hành (Actuator): Đèn, Quạt, Điều hòa.

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| domain | string | Lĩnh vực metric. Giá trị: `DEVICE_STATUS` | Có |
| category | string | Loại thiết bị (xem [DeviceCategory](#devicecategory) dưới Enumerations). Giá trị: `LIGHT` \| `FAN` \| `AIR_CONDITION` | Có |
| targetId | Long | ID của thiết bị mục tiêu | Có |
| latest | boolean | `true` để lấy trạng thái mới nhất, `false` để lấy lịch sử | Mặc định: `false` |
| from | Instant | Thời gian bắt đầu (ISO-8601) | Bắt buộc nếu `latest=false` |
| to | Instant | Thời gian kết thúc (ISO-8601) | Bắt buộc nếu `latest=false` |

### Response Example (Latest Data - 200 OK)

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

### Response Example (Historical Data - 200 OK)

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

> **Lưu ý:** Trường `statusData` có cấu trúc JSON động, phụ thuộc vào loại thiết bị (`category`). Xem chi tiết tại mục [DeviceCategory](#devicecategory).

### Response (Latest - 404 Not Found)

```json
{
    "status": 404,
    "message": "Not Found"
}
```

#### Cấu trúc statusData theo loại thiết bị

##### Light (LIGHT)

| Trường | Loại | Mô tả |
| :----- | :--- | :---- |
| `power` | string | Trạng thái nguồn: `ON` \| `OFF` |
| `level` | integer | Mức độ sáng (0-100), `null` nếu không hỗ trợ |

```json
{
    "power": "ON",
    "level": 80
}
```

##### Fan (FAN)

| Trường | Loại | Mô tả |
| :----- | :--- | :---- |
| `power` | string | Trạng thái nguồn: `ON` \| `OFF` |
| `speed` | integer | Tốc độ quạt, `null` nếu không hỗ trợ |
| `duration` | integer | Thời gian hẹn giờ (phút), `null` nếu không hỗ trợ |
| `mode` | string | Chế độ hoạt động, `null` nếu không hỗ trợ |
| `swing` | string | Trạng thái đảo gió, `null` nếu không hỗ trợ |
| `light` | string | Trạng thái đèn quạt: `ON` \| `OFF`, `null` nếu không có |

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

##### AirCondition (AIR_CONDITION)

| Trường | Loại | Mô tả |
| :----- | :--- | :---- |
| `power` | string | Trạng thái nguồn: `ON` \| `OFF` |
| `temperature` | integer | Nhiệt độ cài đặt (°C), `null` nếu không hỗ trợ |
| `mode` | string | Chế độ hoạt động, `null` nếu không hỗ trợ |
| `fanSpeed` | integer | Tốc độ quạt dàn lạnh, `null` nếu không hỗ trợ |
| `swing` | string | Trạng thái đảo gió, `null` nếu không hỗ trợ |
| `duration` | integer | Thời gian hẹn giờ (phút), `null` nếu không hỗ trợ |

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

</details>

<br>

---

<details>
<summary>Xem chi tiết các hằng số (Enums)</summary>

### MetricDomain

| Giá trị | Mô tả |
| :--- | :--- |
| ENERGY | Các chỉ số liên quan đến năng lượng (điện năng, công suất, điện áp) |
| HEALTH | Các chỉ số liên quan đến môi trường (nhiệt độ, độ ẩm) |
| DEVICE_STATUS | Các chỉ số trạng thái thiết bị chấp hành (bật/tắt, mức độ, chế độ) |

### EnergyMetricCategory

| Giá trị | Mô tả |
| :--- | :--- |
| LIGHT | Thiết bị chiếu sáng |
| AIR_CONDITION | Điều hòa nhiệt độ |
| FAN | Thiết bị quạt |
| ROOM | Thiết bị đo tiêu thụ điện năng |

### DeviceCategory

| Giá trị | Mô tả |
| :--- | :--- |
| LIGHT | Thiết bị chiếu sáng |
| FAN | Thiết bị quạt |
| AIR_CONDITION | Điều hòa nhiệt độ |

</details>

<br>

---
