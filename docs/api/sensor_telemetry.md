# Sensor Telemetry Module

## API thống nhất lấy lịch sử dữ liệu đo đạc từ cảm biến.

Hỗ trợ năm loại cảm biến: `TEMPERATURE` (nhiệt độ), `POWER_CONSUMPTION` (điện năng), `HUMIDITY` (độ ẩm), `SENSOR_CO2` (nồng độ CO₂), và `SENSOR_LUX` (cường độ ánh sáng).

### Lưu ý quan trọng

- Tham số `category` là **bắt buộc** để định tuyến đến bảng dữ liệu tương ứng, tránh xung đột ID giữa các bảng.
- Khoảng thời gian tối đa 365 ngày.
- Dữ liệu trả về ở dạng **raw values** (bản ghi gốc), không áp dụng time-bucketing hay average.
- Giới hạn tối đa **10.000 bản ghi** cho mỗi request để tránh quá tải.

---

<details>
<summary><b>GET</b> <code>/api/v1/sensors/{sensorId}/history</code> - Lịch sử đo đạc theo sensor ID</summary>

> Lấy lịch sử dữ liệu đo đạc của cảm biến theo ID.

### Path Parameters

| Tên       | Loại | Mô tả                    | Bắt buộc |
| :-------- | :--- | :----------------------- | :------- |
| sensorId  | Long | ID cảm biến trong bảng   | Có       |

### Query Parameters

| Tên      | Loại   | Mô tả                                                                                              | Bắt buộc |
| :------- | :----- | :------------------------------------------------------------------------------------------------- | :------- |
| category | string | Loại cảm biến (`TEMPERATURE`, `POWER_CONSUMPTION`, `HUMIDITY`, `SENSOR_CO2`, `SENSOR_LUX`)  | Có       |
| from     | Instant| Thời điểm bắt đầu (ISO-8601)                                                                       | Có       |
| to       | Instant| Thời điểm kết thúc (ISO-8601)                                                                      | Có       |

### Response (200 OK) - Temperature
Trả về danh sách `TemperatureValueDto` (raw values):
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1001,
			"sensorId": 5,
			"tempC": 26.5,
			"timestamp": "2024-06-07T08:00:00Z"
		},
		{
			"id": 1002,
			"sensorId": 5,
			"tempC": 26.8,
			"timestamp": "2024-06-07T08:05:00Z"
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

### Response (200 OK) - Power Consumption
Trả về danh sách `EnergyMetricDto` (raw values từ bảng `energy_metrics`, không group/avg):
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": "2024-06-07T08:00:00Z",
			"voltage": 220.5,
			"current": 1.2,
			"power": 264.6,
			"energy": 12.34,
			"frequency": 50.0,
			"powerFactor": 0.98
		},
		{
			"timestamp": "2024-06-07T08:01:00Z",
			"voltage": 221.0,
			"current": 1.5,
			"power": 331.5,
			"energy": 12.67,
			"frequency": 50.0,
			"powerFactor": 0.99
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

### Response (200 OK) - CO2 Sensor
Trả về danh sách `Co2MetricDto` (raw values từ bảng `co2_metrics`):
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": "2026-07-16T08:00:00Z",
			"co2": 415.5
		},
		{
			"timestamp": "2026-07-16T08:01:00Z",
			"co2": 420.0
		}
	],
	"timestamp": "2026-07-16T09:00:00Z"
}
```

### Response (200 OK) - Lux Sensor
Trả về danh sách `LuxMetricDto` (raw values từ bảng `lux_metrics`):
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": "2026-07-16T08:00:00Z",
			"lux": 820.0
		},
		{
			"timestamp": "2026-07-16T08:01:00Z",
			"lux": 850.5
		}
	],
	"timestamp": "2026-07-16T09:00:00Z"
}
```

### Response (200 OK) - Humidity Sensor
Trả về danh sách `HumidityMetricDto` (raw values từ bảng `humidity_metrics`):
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": "2026-07-14T08:00:00Z",
			"humidity": 68.0
		},
		{
			"timestamp": "2026-07-14T08:01:00Z",
			"humidity": 65.5
		}
	],
	"timestamp": "2026-07-14T09:00:00Z"
}
```

</details>


<details>
<summary><b>GET</b> <code>/api/v1/sensors/natural/{naturalId}/history</code> - Lịch sử đo đạc theo natural ID</summary>

> Lấy lịch sử dữ liệu đo đạc của cảm biến theo mã tự nhiên (naturalId).

### Path Parameters

| Tên       | Loại   | Mô tả                      | Bắt buộc |
| :-------- | :----- | :------------------------- | :------- |
| naturalId | string | Mã tự nhiên của cảm biến   | Có       |

### Query Parameters

| Tên      | Loại   | Mô tả                                                                                              | Bắt buộc |
| :------- | :----- | :------------------------------------------------------------------------------------------------- | :------- |
| category | string | Loại cảm biến (`TEMPERATURE`, `POWER_CONSUMPTION`, `HUMIDITY`, `SENSOR_CO2`, `SENSOR_LUX`)  | Có       |
| from     | Instant| Thời điểm bắt đầu (ISO-8601)                                                                       | Có       |
| to       | Instant| Thời điểm kết thúc (ISO-8601)                                                                      | Có       |

### Response (200 OK) - Temperature
Trả về danh sách `TemperatureValueDto` (raw values):
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 2001,
			"sensorId": 5,
			"tempC": 26.5,
			"timestamp": "2024-06-07T08:00:00Z"
		},
		{
			"id": 2002,
			"sensorId": 5,
			"tempC": 26.8,
			"timestamp": "2024-06-07T08:05:00Z"
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

### Response (200 OK) - Power Consumption
Trả về danh sách `EnergyMetricDto` (raw values từ bảng `energy_metrics`, không group/avg):
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": "2024-06-07T08:00:00Z",
			"voltage": 220.5,
			"current": 1.2,
			"power": 264.6,
			"energy": 12.34,
			"frequency": 50.0,
			"powerFactor": 0.98
		},
		{
			"timestamp": "2024-06-07T08:01:00Z",
			"voltage": 221.0,
			"current": 1.5,
			"power": 331.5,
			"energy": 12.67,
			"frequency": 50.0,
			"powerFactor": 0.99
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

### Response (200 OK) - CO2 Sensor
Trả về danh sách `Co2MetricDto` (raw values từ bảng `co2_metrics`):
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": "2026-07-16T08:00:00Z",
			"co2": 415.5
		},
		{
			"timestamp": "2026-07-16T08:01:00Z",
			"co2": 420.0
		}
	],
	"timestamp": "2026-07-16T09:00:00Z"
}
```

### Response (200 OK) - Lux Sensor
Trả về danh sách `LuxMetricDto` (raw values từ bảng `lux_metrics`):
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": "2026-07-16T08:00:00Z",
			"lux": 820.0
		},
		{
			"timestamp": "2026-07-16T08:01:00Z",
			"lux": 850.5
		}
	],
	"timestamp": "2026-07-16T09:00:00Z"
}
```

### Response (200 OK) - Humidity Sensor
Trả về danh sách `HumidityMetricDto` (raw values từ bảng `humidity_metrics`):
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": "2026-07-14T08:00:00Z",
			"humidity": 68.0
		},
		{
			"timestamp": "2026-07-14T08:01:00Z",
			"humidity": 65.5
		}
	],
	"timestamp": "2026-07-14T09:00:00Z"
}
```

</details>

<br>

---

### Error Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Category query parameter is required",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

```json
{
	"status": 400,
	"message": "Invalid sensor category: LIGHT",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

> **Lưu ý:** `HUMIDITY` cũng được hỗ trợ qua `category=HUMIDITY`. Lịch sử độ ẩm trả về danh sách `HumidityMetricDto` với field `humidity` (% RH). Ngoài ra, có thể dùng `GET /api/v1/metrics?domain=HUMIDITY` để lấy dữ liệu aggregate (time-bucketed).
