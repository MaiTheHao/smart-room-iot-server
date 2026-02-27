# Air Condition Module

API Documentation for Air Condition Management (v1)

Quản lý thiết bị điều hòa (Air Condition) trong hệ thống Smart Room. Tài liệu sử dụng định dạng đóng/mở (collapsible). Vui lòng click vào từng API để xem chi tiết.

## Danh sách API

<details>
<summary><b>GET</b> <code>/api/v1/air-conditions</code> - Lấy danh sách tất cả các thiết bị</summary>

> Lấy danh sách tất cả các thiết bị điều hòa với phân trang.

### Query Parameters

| Tên | Loại | Mô tả | Mặc định |
| :-- | :--- | :---- | :------- |
| page | int | Trang hiện tại (bắt đầu từ 0) | 0 |
| size | int | Số lượng phần tử/trang | 20 |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"naturalId": "AC-ROOM-101",
				"name": "Điều hòa phòng khách",
				"description": "Máy lạnh Daikin 12000 BTU",
				"isActive": true,
				"roomId": 5,
				"power": "ON",
				"temperature": 25,
				"mode": "COOL",
				"fanSpeed": 3,
				"swing": "OFF"
			}
		],
		"page": 0,
		"size": 20,
		"totalElements": 10,
		"totalPages": 1
	},
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

<details>
<summary><b>GET</b> <code>/api/v1/air-conditions/room/{roomId}</code> - Lấy danh sách thiết bị theo phòng</summary>

> Lấy danh sách các thiết bị điều hòa trong một phòng cụ thể.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| roomId | Long | ID của phòng cần lấy thiết bị | Có |

### Query Parameters

| Tên | Loại | Mô tả | Mặc định |
| :-- | :--- | :---- | :------- |
| page | int | Trang hiện tại (bắt đầu từ 0) | 0 |
| size | int | Số lượng phần tử/trang | 20 |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"naturalId": "AC-ROOM-101",
				"name": "Điều hòa phòng khách",
				"description": "Máy lạnh Daikin 12000 BTU",
				"isActive": true,
				"roomId": 5,
				"power": "ON",
				"temperature": 25,
				"mode": "COOL",
				"fanSpeed": 3,
				"swing": "OFF"
			}
		],
		"page": 0,
		"size": 20,
		"totalElements": 2,
		"totalPages": 1
	},
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

<details>
<summary><b>GET</b> <code>/api/v1/air-conditions/{id}</code> - Lấy thông tin chi tiết thiết bị</summary>

> Lấy thông tin chi tiết của một thiết bị điều hòa theo ID.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| id | Long | ID của thiết bị cần lấy | Có |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"naturalId": "AC-ROOM-101",
		"name": "Điều hòa phòng khách",
		"description": "Máy lạnh Daikin 12000 BTU",
		"isActive": true,
		"roomId": 5,
		"power": "ON",
		"temperature": 25,
		"mode": "COOL",
		"fanSpeed": 3,
		"swing": "OFF"
	},
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

<details>
<summary><b>POST</b> <code>/api/v1/air-conditions</code> - Tạo mới thiết bị</summary>

> Tạo mới một thiết bị điều hòa.

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--------- | :--- | :------- | :---- |
| naturalId | string | Có | Mã định danh vật lý của thiết bị (UNIQUE) |
| name | string | Có | Tên thiết bị (1-100 ký tự) |
| description | string | Không | Mô tả chi tiết (tối đa 255 ký tự) |
| isActive | boolean | Không | Trạng thái kích hoạt (mặc định: false) |
| roomId | Long | Có | ID phòng chứa thiết bị |
| deviceControlId | Long | Không | ID bộ điều khiển trung tâm (Gateway) |
| langCode | string | Không | Mã ngôn ngữ (tối đa 10 ký tự, mặc định: vi) |
| power | string | Không | Trạng thái nguồn: ON, OFF (mặc định: OFF) |
| temperature | int | Không | Nhiệt độ (16-32°C, mặc định: 25) |
| mode | string | Không | Chế độ: COOL, HEAT, DRY, FAN (mặc định: COOL) |
| fanSpeed | int | Không | Tốc độ quạt (1-5, mặc định: 3) |
| swing | string | Không | Đảo gió: ON, OFF (mặc định: OFF) |

### Request Example

```json
{
	"naturalId": "AC-ROOM-101",
	"name": "Điều hòa phòng khách",
	"description": "Máy lạnh Daikin 12000 BTU",
	"isActive": true,
	"roomId": 5,
	"deviceControlId": 3,
	"langCode": "vi",
	"power": "ON",
	"temperature": 25,
	"mode": "COOL",
	"fanSpeed": 3,
	"swing": "OFF"
}
```

### Response (201 Created)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 1,
		"naturalId": "AC-ROOM-101",
		"name": "Điều hòa phòng khách",
		"description": "Máy lạnh Daikin 12000 BTU",
		"isActive": true,
		"roomId": 5,
		"power": "ON",
		"temperature": 25,
		"mode": "COOL",
		"fanSpeed": 3,
		"swing": "OFF"
	},
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

<details>
<summary><b>PUT</b> <code>/api/v1/air-conditions/{id}</code> - Cập nhật thông tin cấu hình</summary>

> Cập nhật thông tin thiết bị điều hòa.
>
> Lưu ý: Endpoint này dùng để cập nhật cấu hình thiết bị (name, description, room, ...), không dùng để điều khiển thiết bị.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| id | Long | ID của thiết bị cần cập nhật | Có |

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--------- | :--- | :------- | :---- |
| name | string | Không | Tên thiết bị mới (1-100 ký tự) |
| naturalId | string | Không | Mã định danh vật lý mới |
| description | string | Không | Mô tả mới (tối đa 255 ký tự) |
| isActive | boolean | Không | Cập nhật trạng thái kích hoạt |
| roomId | Long | Không | Chuyển thiết bị sang phòng khác |
| deviceControlId | Long | Không | Thay đổi bộ điều khiển |
| langCode | string | Không | Mã ngôn ngữ (tối đa 10 ký tự) |
| power | string | Không | Cập nhật trạng thái nguồn: ON, OFF |
| temperature | int | Không | Cập nhật nhiệt độ (16-32°C) |
| mode | string | Không | Cập nhật chế độ: COOL, HEAT, DRY, FAN |
| fanSpeed | int | Không | Cập nhật tốc độ quạt (0-5) |
| swing | string | Không | Cập nhật đảo gió: ON, OFF |

### Request Example

```json
{
	"name": "Điều hòa phòng ngủ lớn",
	"description": "Máy lạnh Daikin Inverter 18000 BTU",
	"isActive": true,
	"temperature": 24,
	"fanSpeed": 4,
	"langCode": "vi"
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"naturalId": "AC-ROOM-101",
		"name": "Điều hòa phòng ngủ lớn",
		"description": "Máy lạnh Daikin Inverter 18000 BTU",
		"isActive": true,
		"roomId": 5,
		"power": "ON",
		"temperature": 24,
		"mode": "COOL",
		"fanSpeed": 4,
		"swing": "OFF"
	},
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

<details>
<summary><b>DELETE</b> <code>/api/v1/air-conditions/{id}</code> - Xóa thiết bị</summary>

> Xóa thiết bị điều hòa khỏi hệ thống.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| id | Long | ID của thiết bị cần xóa | Có |

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

<details>
<summary><b>POST</b> <code>/api/v1/air-conditions/{id}/power</code> - Bật/Tắt thiết bị (Theo ID)</summary>

> Điều khiển nguồn điện của thiết bị điều hòa (Bật/Tắt).

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| id | Long | ID của thiết bị cần điều khiển | Có |

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| state | string | Trạng thái nguồn: ON hoặc OFF | Có |

### Request Example

```
POST /api/v1/air-conditions/1/power?state=ON
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Power controlled successfully",
	"data": null,
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

<details>
<summary><b>POST</b> <code>/api/v1/air-conditions/{id}/temperature</code> - Chỉnh nhiệt độ (Theo ID)</summary>

> Điều chỉnh nhiệt độ của thiết bị điều hòa.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| id | Long | ID của thiết bị cần điều khiển | Có |

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| value | int | Giá trị nhiệt độ (16-32°C) | Có |

### Request Example

```
POST /api/v1/air-conditions/1/temperature?value=24
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Temperature controlled successfully",
	"data": null,
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

<details>
<summary><b>POST</b> <code>/api/v1/air-conditions/{id}/mode</code> - Chỉnh chế độ (Theo ID)</summary>

> Thay đổi chế độ hoạt động của điều hòa.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| id | Long | ID của thiết bị cần điều khiển | Có |

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| value | string | Chế độ: COOL (Làm lạnh), HEAT (Sưởi), DRY (Hút ẩm), FAN (Quạt), AUTO | Có |

### Request Example

```
POST /api/v1/air-conditions/1/mode?value=COOL
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Mode controlled successfully",
	"data": null,
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

<details>
<summary><b>POST</b> <code>/api/v1/air-conditions/{id}/fan</code> - Chỉnh tốc độ quạt (Theo ID)</summary>

> Điều chỉnh tốc độ quạt của điều hòa.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| id | Long | ID của thiết bị cần điều khiển | Có |

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| speed | int | Tốc độ quạt (0-5) | Có |

### Request Example

```
POST /api/v1/air-conditions/1/fan?speed=3
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Fan speed controlled successfully",
	"data": null,
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

<details>
<summary><b>POST</b> <code>/api/v1/air-conditions/{id}/swing</code> - Chỉnh đảo gió (Theo ID)</summary>

> Điều khiển chế độ đảo gió (swing) của điều hòa.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| id | Long | ID của thiết bị cần điều khiển | Có |

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| state | string | Trạng thái đảo gió: ON hoặc OFF | Có |

### Request Example

```
POST /api/v1/air-conditions/1/swing?state=ON
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Swing controlled successfully",
	"data": null,
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

<details>
<summary><b>POST</b> <code>/api/v1/air-conditions/{naturalId}/control</code> - Điều khiển cấu hình đa tham số [NEW]</summary>

> Gửi một lệnh điều khiển bao gồm nhiều trạng thái (Nguồn, Quạt, Chế độ, Đảo gió, Nhiệt độ) cùng lúc dựa trên mã định danh vật lý (naturalId).

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :-- | :--- | :---- | :------- |
| naturalId | string | Mã định danh vật lý của máy điều hòa | Có |

### Request Body

Tất cả các trường đều là tùy chọn (chỉ gửi cấu hình cần thay đổi).

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--------- | :--- | :------- | :---- |
| power | string | Không | Trạng thái nguồn: ON, OFF |
| temperature | int | Không | Cài đặt nhiệt độ (Giá trị từ 16 đến 32) |
| mode | string | Không | Chế độ hoạt động: COOL, HEAT, DRY, FAN, AUTO |
| fanSpeed | int | Không | Tốc độ quạt (Giá trị từ 0 đến 5) |
| swing | string | Không | Trạng thái đảo gió: ON, OFF |

### Request Example

```json
{
	"power": "OFF",
	"fanSpeed": 1,
	"mode": "DRY",
	"swing": "ON"
}
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Bulk control command sent successfully",
	"data": null,
	"timestamp": "2026-01-28T10:00:00Z"
}
```

### Error Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Temperature must be between 16 and 32",
	"data": null,
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>

## Enumerations & Constraints

<details>
<summary>Xem chi tiết các hằng số (Enums)</summary>

### AcPower (ActuatorPower)

| Giá trị | Mô tả |
| :------ | :---- |
| ON | Bật nguồn |
| OFF | Tắt nguồn |

### AcMode (ActuatorMode)

| Giá trị | Mô tả |
| :------ | :---- |
| COOL | Làm lạnh |
| HEAT | Sưởi ấm |
| DRY | Hút ẩm |
| FAN | Chế độ quạt |
| AUTO | Chế độ tự động |

### AcSwing (ActuatorSwing)

| Giá trị | Mô tả |
| :------ | :---- |
| ON | Bật đảo gió |
| OFF | Tắt đảo gió |

### Ràng buộc bổ sung

| Tên | Mô tả |
| :-- | :---- |
| Temperature (Nhiệt độ) | Giá trị hợp lệ từ 16 đến 32 |
| FanSpeed (Tốc độ quạt) | Giá trị hợp lệ từ 0 đến 5 |

</details>

## Error Responses Chung

<details>
<summary>Xem chi tiết lỗi HTTP thường gặp</summary>

### 400 Bad Request

Lỗi logic hệ thống hoặc Validation DTO thất bại.

```json
{
	"status": 400,
	"message": "Air Condition already exists with naturalId: AC-ROOM-101",
	"data": null,
	"timestamp": "2026-01-28T10:00:00Z"
}
```

### 404 Not Found

Không tìm thấy thiết bị hoặc phòng.

```json
{
	"status": 404,
	"message": "Air Condition not found with ID/NaturalID: 999",
	"data": null,
	"timestamp": "2026-01-28T10:00:00Z"
}
```

### 500 Internal Server Error

Lỗi hệ thống hoặc mất kết nối Gateway.

```json
{
	"status": 500,
	"message": "Failed to control air condition: Connection timeout",
	"data": null,
	"timestamp": "2026-01-28T10:00:00Z"
}
```

</details>