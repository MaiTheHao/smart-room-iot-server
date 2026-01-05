# Setup Module - API Documentation

## Thiết lập cấu hình thiết bị phòng

### Endpoint

`POST /api/v1/setup/{clientId}`

Thiết lập cấu hình thiết bị cho một phòng thông qua gateway phần cứng.

---

## Request

### Path Parameter

| Field    | Type | Required | Description              |
| -------- | ---- | -------- | ------------------------ |
| clientId | Long | Yes      | ID của gateway phần cứng |

### Body Fields

> **Lưu ý:** Body gửi lên sẽ bị bỏ qua, hệ thống sẽ lấy cấu hình từ gateway phần cứng tương ứng với `clientId`.

---

## Gateway Response Structure (SetupRequestV1)

| Field    | Type           | Required | Description                 |
| -------- | -------------- | -------- | --------------------------- |
| roomCode | string         | Yes      | Mã phòng cần thiết lập      |
| devices  | DeviceConfig[] | Yes      | Danh sách cấu hình thiết bị |

### DeviceConfig

| Field        | Type                | Required | Description                               |
| ------------ | ------------------- | -------- | ----------------------------------------- |
| naturalId    | string              | No       | Mã tự nhiên của thiết bị                  |
| category     | DeviceCategoryV1    | Yes      | LIGHT, TEMPERATURE, POWER_CONSUMPTION     |
| controlType  | DeviceControlTypeV1 | Yes      | GPIO, BLUETOOTH, API                      |
| gpioPin      | int                 | No       | Số chân GPIO (nếu controlType = GPIO)     |
| bleMac       | string              | No       | Địa chỉ MAC (nếu controlType = BLUETOOTH) |
| apiEndpoint  | string              | No       | Endpoint API (nếu controlType = API)      |
| name         | string              | No       | Tên thiết bị                              |
| translations | Map<string,object>  | No       | Thông tin đa ngôn ngữ                     |
| isActive     | boolean             | No       | Trạng thái hoạt động (default: true)      |

#### TranslationDetail

| Field       | Type   | Description    |
| ----------- | ------ | -------------- |
| name        | string | Tên thiết bị   |
| description | string | Mô tả thiết bị |

### Example Gateway Response

```json
{
	"roomCode": "A101",
	"devices": [
		{
			"naturalId": "dev-01",
			"category": "LIGHT",
			"controlType": "GPIO",
			"gpioPin": 17,
			"name": "Đèn trần",
			"isActive": true
		},
		{
			"naturalId": "dev-02",
			"category": "TEMPERATURE",
			"controlType": "BLUETOOTH",
			"bleMac": "AA:BB:CC:DD:EE:FF",
			"name": "Cảm biến nhiệt độ",
			"isActive": true
		}
	]
}
```

---

## Response

### Status: 200 OK

```json
{
	"status": 200,
	"message": "Device setup completed successfully",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## Lưu ý

-   API này sẽ lấy thông tin cấu hình thiết bị từ gateway phần cứng qua địa chỉ IP của client (`clientId`).
-   Nếu không tìm thấy client hoặc phòng, hoặc dữ liệu trả về từ gateway không hợp lệ, API sẽ trả về lỗi tương ứng.
-   Không truyền trực tiếp cấu hình thiết bị trong body request.

---

## Enumerations

### DeviceCategoryV1

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
