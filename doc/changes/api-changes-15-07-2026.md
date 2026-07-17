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