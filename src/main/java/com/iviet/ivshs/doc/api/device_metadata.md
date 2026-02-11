# Device Metadata Module

## Danh sách các API lấy thông tin tổng hợp thiết bị.

---

### GET /api/v1/devices/all

> Lấy danh sách tất cả các thiết bị trong hệ thống (Đèn, Điều hòa, ...).

#### Ví dụ Response (200 OK)

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

---

### GET /api/v1/rooms/{roomId}/devices

> Lấy danh sách thiết bị theo ID phòng.

#### Tham số Đường dẫn (Path Parameters)

| Tên    | Loại | Mô tả    | Bắt buộc |
| :----- | :--- | :------- | :------- |
| roomId | Long | ID phòng | Có       |

#### Ví dụ Response (200 OK)

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
