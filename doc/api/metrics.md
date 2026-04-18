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

<details>
<summary>Xem chi tiết các hằng số (Enums)</summary>

### MetricDomain

| Giá trị | Mô tả |
| :--- | :--- |
| ENERGY | Các chỉ số liên quan đến năng lượng (điện năng, công suất, điện áp) |
| HEALTH | Các chỉ số liên quan đến môi trường (nhiệt độ, độ ẩm) |

### EnergyMetricCategory

| Giá trị | Mô tả |
| :--- | :--- |
| LIGHT | Thiết bị chiếu sáng |
| AIR_CONDITION | Điều hòa nhiệt độ |
| FAN | Thiết bị quạt |

</details>

<br>

---
