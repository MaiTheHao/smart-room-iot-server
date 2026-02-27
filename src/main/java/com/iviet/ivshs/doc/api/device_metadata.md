# Device Metadata Module

## Danh sách các API lấy thông tin tổng hợp thiết bị.

---

<details>
<summary><b>GET</b> <code>/api/v1/devices/all</code> - Lấy tất cả thiết bị</summary>

> Lấy danh sách tất cả các thiết bị trong hệ thống (Đèn, Điều hòa, ...).

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
			"roomId": 10,
			"category": "LIGHT"
		},
		{
			"id": 2,
			"naturalId": "AC001",
			"name": "Điều hòa Panasonic",
			"description": "Điều hòa phòng ngủ chính",
			"isActive": true,
			"roomId": 12,
			"category": "AIR_CONDITION"
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/devices</code> - Lấy thiết bị theo phòng</summary>

> Lấy danh sách thiết bị theo ID phòng.

### Path Parameters

| Tên    | Loại | Mô tả    | Bắt buộc |
| :----- | :--- | :------- | :------- |
| roomId | Long | ID phòng | Có       |

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
			"roomId": 10,
			"category": "LIGHT"
		},
		{
			"id": 3,
			"naturalId": "L002",
			"name": "Đèn ngủ",
			"description": "Đèn ngủ cạnh giường",
			"isActive": false,
			"roomId": 10,
			"category": "LIGHT"
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

---
