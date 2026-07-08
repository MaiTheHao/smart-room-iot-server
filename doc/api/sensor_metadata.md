# Sensor Metadata Module

## Danh sách các API lấy thông tin tổng hợp cảm biến.

Hỗ trợ hai loại cảm biến: `TEMPERATURE` (nhiệt độ) và `POWER_CONSUMPTION` (điện năng).

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

| Tên      | Loại   | Mô tả                                                              | Bắt buộc |
| :------- | :----- | :----------------------------------------------------------------- | :------- |
| category | string | Lọc cảm biến theo loại (`TEMPERATURE`, `POWER_CONSUMPTION`) | Không    |

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
			"sensor": {
				"id": 1,
				"naturalId": "TEMP001",
				"name": "Cảm biến nhiệt phòng khách",
				"description": "Cảm biến nhiệt độ tầng 1",
				"isActive": true,
				"currentValue": 26.5,
				"roomId": 10,
				"deviceControlId": 1
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
			"sensor": {
				"id": 1,
				"naturalId": "PWR001",
				"name": "Cảm biến điện phòng khách",
				"description": "Cảm biến điện năng tầng 1",
				"isActive": true,
				"currentWatt": 150.0,
				"roomId": 10,
				"deviceControlId": 2
			}
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>


<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/sensors/count</code> - Đếm số lượng cảm biến theo phòng</summary>

> Trả về số lượng cảm biến (Temperature + PowerConsumption) thuộc phòng có ID chỉ định.

### Path Parameters

| Tên    | Loại | Mô tả    | Bắt buộc |
| :----- | :--- | :------- | :------- |
| roomId | Long | ID phòng | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": 3,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>


<details>
<summary><b>GET</b> <code>/api/v1/sensors/all</code> - Lấy tất cả cảm biến</summary>

> Lấy danh sách toàn bộ cảm biến trong hệ thống, không phân biệt phòng. Hỗ trợ lọc theo loại cảm biến.

### Query Parameters

| Tên      | Loại   | Mô tả                                                              | Bắt buộc |
| :------- | :----- | :----------------------------------------------------------------- | :------- |
| category | string | Lọc cảm biến theo loại (`TEMPERATURE`, `POWER_CONSUMPTION`) | Không    |

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
			"sensor": {
				"id": 1,
				"naturalId": "TEMP001",
				"name": "Cảm biến nhiệt phòng khách",
				"description": "Cảm biến nhiệt độ tầng 1",
				"isActive": true,
				"currentValue": 26.5,
				"roomId": 10,
				"deviceControlId": 1
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
			"sensor": {
				"id": 1,
				"naturalId": "PWR001",
				"name": "Cảm biến điện phòng khách",
				"description": "Cảm biến điện năng tầng 1",
				"isActive": true,
				"currentWatt": 150.0,
				"roomId": 10,
				"deviceControlId": 2
			}
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>
