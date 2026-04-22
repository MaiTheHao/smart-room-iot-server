# Device Metadata Module

## Danh sách các API lấy thông tin tổng hợp thiết bị.

---


<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/devices</code> - Lấy thiết bị theo phòng</summary>

> Lấy danh sách thiết bị theo ID phòng.

### Path Parameters

| Tên    | Loại | Mô tả    | Bắt buộc |
| :----- | :--- | :------- | :------- |
| roomId | Long | ID phòng | Có       |

### Query Parameters

| Tên      | Loại   | Mô tả                                                          | Bắt buộc |
| :------- | :----- | :------------------------------------------------------------- | :------- |
| category | string | Lọc thiết bị theo loại (`LIGHT`, `FAN`, `AIR_CONDITION`) | Không    |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"naturalId": "L001",
			"name": "Đèn trần",
			"description": "Đèn phòng khách",
			"isActive": true,
			"power": "ON",
			"level": 80,
			"roomId": 10,
			"deviceControlId": 1
		},
		{
			"id": 3,
			"naturalId": "FAN001",
			"name": "Quạt trần",
			"description": "Quạt trần IR",
			"isActive": true,
			"power": "OFF",
			"type": "IR",
			"speed": 3,
			"mode": "NORMAL",
			"swing": "ON",
			"light": "OFF",
			"roomId": 10,
			"deviceControlId": 3
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>


<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/devices/count</code> - Đếm số lượng thiết bị theo phòng</summary>

> Trả về số lượng thiết bị thuộc phòng có ID chỉ định.

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

<br>
---
