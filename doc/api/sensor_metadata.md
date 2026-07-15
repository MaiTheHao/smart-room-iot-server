# Sensor Metadata Module

## Danh sách các API lấy thông tin tổng hợp cảm biến.

Hỗ trợ năm loại cảm biến: `TEMPERATURE` (nhiệt độ), `POWER_CONSUMPTION` (điện năng), `HUMIDITY` (độ ẩm), `SENSOR_CO2` (nồng độ CO₂), và `SENSOR_LUX` (cường độ ánh sáng).

---

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/sensors</code> - Lấy cảm biến theo phòng</summary>

> Lấy danh sách cảm biến theo ID phòng. Hỗ trợ lọc theo loại cảm biến.
>
> **Lưu ý:** API này được tối ưu hóa bằng cách truy vấn song song (Asynchronous) các loại cảm biến khác nhau để giảm thời gian phản hồi.

### Path Parameters

| Tên    | Loại | Mô tả    | Bắt buộc |
| :----- | :--- | :------- | :------- |
| roomId | Long | ID phòng | Có       |

### Query Parameters

| Tên      | Loại   | Mô tả                                                                                              | Bắt buộc |
| :------- | :----- | :------------------------------------------------------------------------------------------------- | :------- |
| category | string | Lọc cảm biến theo loại (`TEMPERATURE`, `POWER_CONSUMPTION`, `HUMIDITY`, `SENSOR_CO2`, `SENSOR_LUX`) | Không    |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"naturalId": "TEMP001",
			"name": "Cảm biến nhiệt phòng khách",
			"description": "Cảm biến nhiệt độ tầng 1",
			"isActive": true,
			"roomId": 10,
			"category": "TEMPERATURE",
			"data": {
				"currentValue": 26.5
			}
		},
		{
			"id": 2,
			"naturalId": "PWR001",
			"name": "Cảm biến điện phòng khách",
			"description": "Cảm biến điện năng tầng 1",
			"isActive": true,
			"roomId": 10,
			"category": "POWER_CONSUMPTION",
			"data": {
				"currentWatt": 150.0
			}
		},
		{
			"id": 5,
			"naturalId": "HUM_ESP32_01",
			"name": "Cảm biến độ ẩm phòng khách",
			"description": "Đo độ ẩm không khí",
			"isActive": true,
			"roomId": 10,
			"category": "HUMIDITY",
			"data": {
				"currentHumidity": 65.5
			}
		},
		{
			"id": 3,
			"naturalId": "ESP32_CO2_01",
			"name": "Cảm biến CO₂ phòng khách",
			"description": "Đo nồng độ CO₂",
			"isActive": true,
			"roomId": 10,
			"category": "SENSOR_CO2",
			"data": {
				"currentCo2": 420.5
			}
		},
		{
			"id": 4,
			"naturalId": "LUX_SENSOR_01",
			"name": "Cảm biến ánh sáng phòng khách",
			"description": "Đo cường độ ánh sáng",
			"isActive": true,
			"roomId": 10,
			"category": "SENSOR_LUX",
			"data": {
				"currentLux": 850.0
			}
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>


<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/sensors/count</code> - Đếm số lượng cảm biến theo phòng</summary>

> Trả về tổng số lượng cảm biến (Temperature + PowerConsumption + HumiditySensor + Co2Sensor + LuxSensor) thuộc phòng có ID chỉ định.

### Path Parameters

| Tên    | Loại | Mô tả    | Bắt buộc |
| :----- | :--- | :------- | :------- |
| roomId | Long | ID phòng | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": 5,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>


<details>
<summary><b>GET</b> <code>/api/v1/sensors/all</code> - Lấy tất cả cảm biến</summary>

> Lấy danh sách toàn bộ cảm biến trong hệ thống, không phân biệt phòng. Hỗ trợ lọc theo loại cảm biến.

### Query Parameters

| Tên      | Loại   | Mô tả                                                                                              | Bắt buộc |
| :------- | :----- | :------------------------------------------------------------------------------------------------- | :------- |
| category | string | Lọc cảm biến theo loại (`TEMPERATURE`, `POWER_CONSUMPTION`, `HUMIDITY`, `SENSOR_CO2`, `SENSOR_LUX`) | Không    |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"naturalId": "TEMP001",
			"name": "Cảm biến nhiệt phòng khách",
			"description": "Cảm biến nhiệt độ tầng 1",
			"isActive": true,
			"roomId": 10,
			"category": "TEMPERATURE",
			"data": {
				"currentValue": 26.5
			}
		},
		{
			"id": 3,
			"naturalId": "ESP32_CO2_01",
			"name": "Cảm biến CO₂ phòng khách",
			"description": "Đo nồng độ CO₂",
			"isActive": true,
			"roomId": 10,
			"category": "SENSOR_CO2",
			"data": {
				"currentCo2": 420.5
			}
		},
		{
			"id": 4,
			"naturalId": "LUX_SENSOR_01",
			"name": "Cảm biến ánh sáng phòng khách",
			"description": "Đo cường độ ánh sáng",
			"isActive": true,
			"roomId": 10,
			"category": "SENSOR_LUX",
			"data": {
				"currentLux": 850.0
			}
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>
