# Sensor Telemetry Module

## API thống nhất lấy lịch sử dữ liệu đo đạc từ cảm biến.

Hỗ trợ hai loại cảm biến: `TEMPERATURE` (nhiệt độ) và `POWER_CONSUMPTION` (điện năng).

### Lưu ý quan trọng

- Tham số `category` là **bắt buộc** để định tuyến đến bảng dữ liệu tương ứng, tránh xung đột ID giữa các bảng.
- Khoảng thời gian tối đa 365 ngày.
- Dữ liệu được time-bucketing tự động dựa trên khoảng thời gian.

---

<details>
<summary><b>GET</b> <code>/api/v1/sensors/{sensorId}/history</code> - Lịch sử đo đạc theo sensor ID</summary>

> Lấy lịch sử dữ liệu đo đạc của cảm biến theo ID.

### Path Parameters

| Tên       | Loại | Mô tả                    | Bắt buộc |
| :-------- | :--- | :----------------------- | :------- |
| sensorId  | Long | ID cảm biến trong bảng   | Có       |

### Query Parameters

| Tên      | Loại   | Mô tả                                                              | Bắt buộc |
| :------- | :----- | :----------------------------------------------------------------- | :------- |
| category | string | Loại cảm biến (`TEMPERATURE`, `POWER_CONSUMPTION`)           | Có       |
| from     | Instant| Thời điểm bắt đầu (ISO-8601)                                       | Có       |
| to       | Instant| Thời điểm kết thúc (ISO-8601)                                      | Có       |

### Response (200 OK) - Temperature
Trả về danh sách `AverageTemperatureValueDto`:
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": 1719878400,
			"avgTempC": 26.5
		},
		{
			"timestamp": 1719878700,
			"avgTempC": 26.8
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

### Response (200 OK) - Power Consumption
Trả về danh sách `EnergyMetricDto` (lấy từ bảng `energy_metrics`):
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
			"timestamp": "2024-06-07T09:00:00Z",
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

</details>


<details>
<summary><b>GET</b> <code>/api/v1/sensors/natural/{naturalId}/history</code> - Lịch sử đo đạc theo natural ID</summary>

> Lấy lịch sử dữ liệu đo đạc của cảm biến theo mã tự nhiên (naturalId).

### Path Parameters

| Tên       | Loại   | Mô tả                      | Bắt buộc |
| :-------- | :----- | :------------------------- | :------- |
| naturalId | string | Mã tự nhiên của cảm biến   | Có       |

### Query Parameters

| Tên      | Loại   | Mô tả                                                              | Bắt buộc |
| :------- | :----- | :----------------------------------------------------------------- | :------- |
| category | string | Loại cảm biến (`TEMPERATURE`, `POWER_CONSUMPTION`)           | Có       |
| from     | Instant| Thời điểm bắt đầu (ISO-8601)                                       | Có       |
| to       | Instant| Thời điểm kết thúc (ISO-8601)                                      | Có       |

### Response (200 OK) - Temperature
Trả về danh sách `AverageTemperatureValueDto`:
```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": 1719878400,
			"avgTempC": 26.5
		},
		{
			"timestamp": 1719878700,
			"avgTempC": 26.8
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

### Response (200 OK) - Power Consumption
Trả về danh sách `EnergyMetricDto` (lấy từ bảng `energy_metrics`):
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
			"timestamp": "2024-06-07T09:00:00Z",
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
