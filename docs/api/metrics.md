# Metric Module

## Quản lý và Truy vấn các chỉ số hệ thống (Metrics)

---

> **Tham số `category` dùng chung cho các domain cảm biến môi trường (TEMPERATURE, HUMIDITY, CO2, LUX):**
> - `category = null | "" | "DEFAULT"` → Truy vấn theo cảm biến đơn lẻ (hành vi hiện tại). `targetId` = sensor ID.
> - `category = "ROOM"` → Tổng hợp từ toàn bộ cảm biến active trong phòng theo thuật toán riêng cho từng domain. `targetId` = room ID.
> - Giá trị khác → HTTP 400 `BadRequestException`.
>
> Xem chi tiết tại [SensorMetricCategory](#sensormetriccategory).

### Tổng hợp phép tính (theo CALC.md)

| Domain | Thông số trả về | Phép tính | Nguồn / Mô tả |
| :--- | :--- | :--- | :--- |
| `TEMPERATURE` | `avgTemp` | **Mean (AVG)** | AVG `currentValue` của tất cả `Temperature` sensor active trong phòng |
| `HUMIDITY` | `medianHumidity` | **Median** | Trung vị `currentHumidity` của các `HumiditySensor` active — loại bỏ outliers |
| `LUX` | `medianLux` | **Median** | Trung vị `currentLux` của các `LuxSensor` active — giảm nhiễu chói cục bộ |
| `CO2` | `avgCo2`, `maxCo2` | **Mean + Max** | AVG cho lịch sử/đồ thị; **Max** cho Automation |
| `ENERGY` | `voltage`, `current`, `power`, `frequency`, `powerFactor` | **Mean (AVG)** | Trung bình cộng các thông số trong bucket |
| `ENERGY` | `energy` | **Max** | Chỉ số **tích lũy lũy kế** (kWh) → lấy giá trị cuối chu kỳ |
| `DEVICE_STATUS` | `statusData` | Không tổng hợp | Bản ghi trạng thái gốc (bật/tắt, mức độ, chế độ) |

## 1. Energy Domain (ENERGY)

<details>
<summary><b>GET</b> <code>/api/v1/metrics?domain=ENERGY</code> - Truy vấn chỉ số năng lượng</summary>

> Lấy dữ liệu chỉ số năng lượng (mới nhất hoặc lịch sử) cho các thiết bị như Đèn, Điều hòa, Quạt.

### Phép tính (theo CALC.md)

| Thông số | Phép tính | Mô tả |
| :--- | :--- | :--- |
| `voltage`, `current`, `power`, `frequency`, `powerFactor` | **Mean (AVG)** | Trung bình cộng các giá trị trong khoảng thời gian gom nhóm (bucket) |
| `energy` | **Max** | Chỉ số **tích lũy lũy kế** (cumulative kWh) → lấy giá trị lớn nhất cuối chu kỳ |

> **Lưu ý:** Domain ENERGY **không có tổng hợp chéo cảm biến** (cross-sensor). `category=ROOM` là **thiết bị đo tổng của phòng** (`PowerConsumption`) — mỗi phòng có một thiết bị đo, `targetId` = ID cảm biến. **Không áp dụng phép SUM.**

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

## 2. Temperature Domain (TEMPERATURE)

<details>
<summary><b>GET</b> <code>/api/v1/metrics?domain=TEMPERATURE</code> - Truy vấn chỉ số nhiệt độ</summary>

> Lấy dữ liệu nhiệt độ (mới nhất hoặc lịch sử) từ bảng `temperature_metrics`. Dữ liệu được ghi nhận đồng thời với bảng `temperature_value` truyền thống.

### Phép tính (theo CALC.md)

- **Phương pháp:** **Mean (AVG)** — trung bình cộng, phản ánh nhiệt độ chung của không gian phòng.
- `category=DEFAULT` (cảm biến đơn lẻ): history → `AVG(temperature)` theo time bucket.
- `category=ROOM` (cả phòng): `AVG(currentValue)` của tất cả `Temperature` sensor **active** trong phòng; history → `AVG` per time bucket.

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| domain | string | Lĩnh vực metric. Giá trị: `TEMPERATURE` | Có |
| category | string | Phạm vi truy vấn: `DEFAULT` \| `ROOM` (xem hướng dẫn chung ở đầu trang) | Không. Mặc định: `DEFAULT` |
| targetId | Long | ID của cảm biến nhiệt độ | Có |
| latest | boolean | `true` để lấy giá trị mới nhất, `false` để lấy lịch sử | Mặc định: `false` |
| from | Instant | Thời gian bắt đầu (ISO-8601) | Bắt buộc nếu `latest=false` |
| to | Instant | Thời gian kết thúc (ISO-8601) | Bắt buộc nếu `latest=false` |

### Response Example (Latest Data - 200 OK)

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

### Response Example (Historical Data - 200 OK)

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

#### Response Example (ROOM Latest - 200 OK)

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

> **Thuật toán ROOM:** AVG(`currentValue`) của tất cả `Temperature` sensor active trong phòng. Với history, AVG per time bucket.

</details>

<br>

---

## 3. Humidity Domain (HUMIDITY)

<details>
<summary><b>GET</b> <code>/api/v1/metrics?domain=HUMIDITY</code> - Truy vấn chỉ số độ ẩm</summary>

> Lấy dữ liệu độ ẩm (mới nhất hoặc lịch sử) từ bảng `humidity_metrics`. Đây là nơi lưu trữ chính cho dữ liệu độ ẩm.

### Phép tính (theo CALC.md)

- **Phương pháp:** **Median** (trung vị) — loại bỏ ảnh hưởng của các giá trị dị biệt (outliers) hoặc dữ liệu nhiễu cục bộ từ cảm biến đơn lẻ.
- `category=DEFAULT` (cảm biến đơn lẻ): history → `AVG(humidity)` theo time bucket (phục vụ vẽ đồ thị).
- `category=ROOM` (cả phòng): **Median** của `currentHumidity` các `HumiditySensor` active; history → Median per time bucket.

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| domain | string | Lĩnh vực metric. Giá trị: `HUMIDITY` | Có |
| category | string | Phạm vi truy vấn: `DEFAULT` \| `ROOM` (xem hướng dẫn chung ở đầu trang) | Không. Mặc định: `DEFAULT` |
| targetId | Long | ID của cảm biến độ ẩm | Có |
| latest | boolean | `true` để lấy giá trị mới nhất, `false` để lấy lịch sử | Mặc định: `false` |
| from | Instant | Thời gian bắt đầu (ISO-8601) | Bắt buộc nếu `latest=false` |
| to | Instant | Thời gian kết thúc (ISO-8601) | Bắt buộc nếu `latest=false` |

### Response Example (Latest Data - 200 OK)

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

### Response Example (Historical Data - 200 OK)

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

#### Response Example (ROOM Latest - 200 OK)

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

> **Thuật toán ROOM:** Median(`currentHumidity`) của tất cả `HumiditySensor` active trong phòng. Với history, Median per time bucket.

</details>

<br>

---

## 4. Device Status Domain (DEVICE_STATUS)

<details>
<summary><b>GET</b> <code>/api/v1/metrics?domain=DEVICE_STATUS</code> - Truy vấn trạng thái thiết bị chấp hành</summary>

> Lấy dữ liệu trạng thái (mới nhất hoặc lịch sử) cho các thiết bị chấp hành (Actuator): Đèn, Quạt, Điều hòa.

### Phép tính

- **Không áp dụng phép tổng hợp số học.** Đây là dữ liệu trạng thái (bật/tắt, mức độ, chế độ), trả về **bản ghi gốc** theo timestamp.

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

## 5. CO₂ Domain (CO2)

<details>
<summary><b>GET</b> <code>/api/v1/metrics?domain=CO2</code> - Truy vấn chỉ số nồng độ CO₂</summary>

> Lấy dữ liệu nồng độ CO₂ (mới nhất hoặc lịch sử) từ bảng `co2_metrics`. Dữ liệu được ghi nhận mỗi lần telemetry CO2 sensor được collect.

### Phép tính (theo CALC.md)

- **Phương pháp:** **Max + Mean (AVG)**.
  - **Max:** phục vụ **Automation** — kích hoạt quạt thông gió ngay khi phát hiện vùng ngột ngạt cục bộ vượt ngưỡng an toàn.
  - **Mean:** phục vụ **lịch sử** và vẽ đồ thị xu hướng chất lượng không khí chung của phòng.
- `category=DEFAULT` (cảm biến đơn lẻ): history → `AVG(co2)` theo time bucket.
- `category=ROOM` (cả phòng): `AVG(currentCO2)` + `MAX(currentCO2)` của các `Co2Sensor` active; history → `AVG` + `MAX` per time bucket.

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| domain | string | Lĩnh vực metric. Giá trị: `CO2` | Có |
| category | string | Phạm vi truy vấn: `DEFAULT` \| `ROOM` (xem hướng dẫn chung ở đầu trang) | Không. Mặc định: `DEFAULT` |
| targetId | Long | ID của cảm biến CO₂ | Có |
| latest | boolean | `true` để lấy giá trị mới nhất, `false` để lấy lịch sử | Mặc định: `false` |
| from | Instant | Thời gian bắt đầu (ISO-8601) | Bắt buộc nếu `latest=false` |
| to | Instant | Thời gian kết thúc (ISO-8601) | Bắt buộc nếu `latest=false` |

### Response Example (Latest Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-16T10:00:00Z",
        "co2": 420.5
    },
    "timestamp": "2026-07-16T10:00:01Z"
}
```

### Response Example (Historical Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": [
        {
            "timestamp": "2026-07-16T09:00:00Z",
            "co2": 410.0
        },
        {
            "timestamp": "2026-07-16T10:00:00Z",
            "co2": 420.5
        }
    ],
    "timestamp": "2026-07-16T10:00:01Z"
}
```

#### Response Example (ROOM Latest - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-15T10:00:00Z",
        "avgCo2": 420.5,
        "maxCo2": 520.0
    },
    "timestamp": "2026-07-15T10:00:01Z"
}
```

> **Thuật toán ROOM:** AVG(`currentCO2`) của tất cả `Co2Sensor` active trong phòng. Với history, AVG per time bucket.

</details>

<br>

---

## 6. Lux Domain (LUX)

<details>
<summary><b>GET</b> <code>/api/v1/metrics?domain=LUX</code> - Truy vấn chỉ số cường độ ánh sáng</summary>

> Lấy dữ liệu cường độ ánh sáng (mới nhất hoặc lịch sử) từ bảng `lux_metrics`. Dữ liệu được ghi nhận mỗi lần telemetry Lux sensor được collect.

### Phép tính (theo CALC.md)

- **Phương pháp:** **Median** (trung vị) — giảm thiểu ảnh hưởng của hiện tượng chói sáng cục bộ (ví dụ nắng rọi trực tiếp vào cảm biến).
- `category=DEFAULT` (cảm biến đơn lẻ): history → `AVG(lux)` theo time bucket.
- `category=ROOM` (cả phòng): **Median** của `currentLux` các `LuxSensor` active; history → Median per time bucket.

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc / Mặc định |
| :--- | :--- | :--- | :--- |
| domain | string | Lĩnh vực metric. Giá trị: `LUX` | Có |
| category | string | Phạm vi truy vấn: `DEFAULT` \| `ROOM` (xem hướng dẫn chung ở đầu trang) | Không. Mặc định: `DEFAULT` |
| targetId | Long | ID của cảm biến ánh sáng | Có |
| latest | boolean | `true` để lấy giá trị mới nhất, `false` để lấy lịch sử | Mặc định: `false` |
| from | Instant | Thời gian bắt đầu (ISO-8601) | Bắt buộc nếu `latest=false` |
| to | Instant | Thời gian kết thúc (ISO-8601) | Bắt buộc nếu `latest=false` |

### Response Example (Latest Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-16T10:00:00Z",
        "lux": 850.0
    },
    "timestamp": "2026-07-16T10:00:01Z"
}
```

### Response Example (Historical Data - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": [
        {
            "timestamp": "2026-07-16T09:00:00Z",
            "lux": 820.0
        },
        {
            "timestamp": "2026-07-16T10:00:00Z",
            "lux": 850.0
        }
    ],
    "timestamp": "2026-07-16T10:00:01Z"
}
```

#### Response Example (ROOM Latest - 200 OK)

```json
{
    "status": 200,
    "message": "Success",
    "data": {
        "timestamp": "2026-07-15T10:00:00Z",
        "medianLux": 850.0
    },
    "timestamp": "2026-07-15T10:00:01Z"
}
```

> **Thuật toán ROOM:** Median(`currentLux`) của tất cả `LuxSensor` active trong phòng. Với history, Median per time bucket.

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
| TEMPERATURE | **(Mới 14-07)** Chỉ số nhiệt độ từ cảm biến, lưu trong bảng `temperature_metrics` |
| HUMIDITY | **(Mới 14-07)** Chỉ số độ ẩm từ cảm biến, lưu trong bảng `humidity_metrics` |
| CO2 | **(Mới 16-07)** Chỉ số nồng độ CO₂ từ cảm biến, lưu trong bảng `co2_metrics` |
| LUX | **(Mới 16-07)** Chỉ số cường độ ánh sáng từ cảm biến, lưu trong bảng `lux_metrics` |

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
| SENSOR_CO2 | **(Mới 16-07)** Cảm biến nồng độ CO₂ |
| SENSOR_LUX | **(Mới 16-07)** Cảm biến cường độ ánh sáng |

### SensorMetricCategory (Mới 15-07)

| Giá trị | Mô tả |
| :--- | :--- |
| DEFAULT | Truy vấn theo cảm biến đơn lẻ. `targetId` = sensor ID |
| ROOM | Tổng hợp theo phòng. `targetId` = room ID |

> `SensorMetricCategory.fromString()`: `null`/`""` → `DEFAULT`, `"ROOM"` → `ROOM`, giá trị khác → `BadRequestException` (400).

</details>

<br>

---
