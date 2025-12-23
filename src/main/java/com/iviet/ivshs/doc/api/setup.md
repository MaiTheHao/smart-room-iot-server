# Setup Module - API Documentation

## Create Room Setup

### Endpoint

`POST /api/v1/setup`

Thiết lập cấu hình thiết bị cho một phòng.

---

## Request

### Body Fields

| Field    | Type           | Required | Description                               |
| -------- | -------------- | -------- | ----------------------------------------- |
| roomId   | Long           | Yes      | ID phòng cần thiết lập                    |
| langCode | string         | No       | Mã ngôn ngữ (default: "vi", max 10 chars) |
| devices  | DeviceConfig[] | No       | Danh sách cấu hình thiết bị               |

### DeviceConfig

| Field       | Type                | Required | Description                               |
| ----------- | ------------------- | -------- | ----------------------------------------- |
| category    | SetupCategoryV1     | Yes      | LIGHT, TEMPERATURE, POWER_CONSUMPTION     |
| name        | string              | Yes      | Tên thiết bị                              |
| controlType | DeviceControlTypeV1 | Yes      | GPIO, BLUETOOTH, API                      |
| gpioPin     | int                 | No       | Số chân GPIO (nếu controlType = GPIO)     |
| bleMac      | string              | No       | Địa chỉ MAC (nếu controlType = BLUETOOTH) |
| apiEndpoint | string              | No       | Endpoint API (nếu controlType = API)      |
| isActive    | boolean             | No       | Trạng thái hoạt động (default: true)      |

### Example Request

```json
{
	"roomId": 101,
	"langCode": "vi",
	"devices": [
		{
			"category": "LIGHT",
			"name": "Đèn trần",
			"controlType": "GPIO",
			"gpioPin": 17,
			"isActive": true
		},
		{
			"category": "TEMPERATURE",
			"name": "Cảm biến nhiệt độ",
			"controlType": "BLUETOOTH",
			"bleMac": "AA:BB:CC:DD:EE:FF",
			"isActive": true
		},
		{
			"category": "POWER_CONSUMPTION",
			"name": "Thiết bị đo điện",
			"controlType": "API",
			"apiEndpoint": "https://api.example.com/power",
			"isActive": false
		}
	]
}
```

---

## Response

### Status: 201 Created

```json
{
	"status": 201,
	"message": "Created successfully",
	"data": {
		"roomId": 101,
		"createdDevices": [
			{
				"deviceControlId": 5,
				"category": "LIGHT",
				"targetId": 1,
				"name": "Đèn trần",
				"controlType": "GPIO",
				"isActive": true,
				"position": 0
			},
			{
				"deviceControlId": 6,
				"category": "TEMPERATURE",
				"targetId": 2,
				"name": "Cảm biến nhiệt độ",
				"controlType": "BLUETOOTH",
				"isActive": true,
				"position": 1
			},
			{
				"deviceControlId": 7,
				"category": "POWER_CONSUMPTION",
				"targetId": 3,
				"name": "Thiết bị đo điện",
				"controlType": "API",
				"isActive": false,
				"position": 2
			}
		]
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

### CreatedDevice Fields

| Field           | Type                | Description                                 |
| --------------- | ------------------- | ------------------------------------------- |
| deviceControlId | Long                | ID của DeviceControl vừa tạo                |
| category        | SetupCategoryV1     | LIGHT, TEMPERATURE, POWER_CONSUMPTION       |
| targetId        | Long                | ID của sensor/thiết bị tương ứng            |
| name            | string              | Tên thiết bị                                |
| controlType     | DeviceControlTypeV1 | GPIO, BLUETOOTH, API                        |
| isActive        | boolean             | Trạng thái hoạt động                        |
| position        | int                 | Thứ tự trong request (0-based), dùng để map |

---

## Get Device Details

Sử dụng `targetId` từ response để lấy thông tin chi tiết:

### Lights

```
GET /api/v1/lights/{targetId}
```

**Response:**

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Đèn trần",
		"description": null,
		"isActive": true,
		"level": 0,
		"roomId": 101
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

### Temperature

```
GET /api/v1/temperatures/{targetId}
```

### Power Consumption

```
GET /api/v1/power-consumptions/{targetId}
```

---

## Enumerations

### SetupCategoryV1

| Value             | Description           |
| ----------------- | --------------------- |
| LIGHT             | Thiết bị chiếu sáng   |
| TEMPERATURE       | Thiết bị đo nhiệt độ  |
| POWER_CONSUMPTION | Thiết bị đo điện năng |

### DeviceControlTypeV1

| Value     | Description              |
| --------- | ------------------------ |
| GPIO      | Điều khiển qua GPIO      |
| BLUETOOTH | Điều khiển qua BLUETOOTH |
| API       | Điều khiển qua API       |
