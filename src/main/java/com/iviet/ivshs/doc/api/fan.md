# Fan Module

## API Documentation for Fan Management (v1)

> Quản lý thiết bị quạt (Fan - bao gồm Quạt thông thường GPIO và Quạt hồng ngoại IR) trong hệ thống Smart Room

---

## GET /api/v1/fans

> Lấy danh sách tất cả các thiết bị quạt với phân trang.

### Query Parameters

| Tên  | Loại | Mô tả                         | Mặc định |
| :--- | :--- | :---------------------------- | :------- |
| page | int  | Trang hiện tại (bắt đầu từ 0) | 0        |
| size | int  | Số lượng phần tử/trang        | 20       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"naturalId": "FAN-ROOM-101",
				"name": "Quạt trần phòng khách",
				"description": "Quạt trần Panasonic kết nối IR",
				"isActive": true,
				"roomId": 5,
				"power": "ON",
				"type": "IR",
				"speed": 3,
				"mode": "COOL",
				"swing": "ON",
				"light": "OFF"
			}
		],
		"page": 0,
		"size": 20,
		"totalElements": 10,
		"totalPages": 1
	},
	"timestamp": "2026-02-24T10:00:00Z"
}
```

---

## GET /api/v1/fans/room/{roomId}

> Lấy danh sách các thiết bị quạt trong một phòng cụ thể.

### Path Parameters

| Tên    | Loại | Mô tả                         | Bắt buộc |
| :----- | :--- | :---------------------------- | :------- |
| roomId | Long | ID của phòng cần lấy thiết bị | Có       |

### Query Parameters

| Tên  | Loại | Mô tả                         | Mặc định |
| :--- | :--- | :---------------------------- | :------- |
| page | int  | Trang hiện tại (bắt đầu từ 0) | 0        |
| size | int  | Số lượng phần tử/trang        | 20       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"naturalId": "FAN-ROOM-101",
				"name": "Quạt trần",
				"isActive": true,
				"roomId": 5,
				"power": "ON",
				"type": "IR"
			}
		],
		"page": 0,
		"size": 20,
		"totalElements": 2,
		"totalPages": 1
	},
	"timestamp": "2026-02-24T10:00:00Z"
}
```

---

## GET /api/v1/fans/room/{roomId}/fans/{naturalId}

> Lấy thông tin chi tiết của một thiết bị quạt dựa vào Room ID và Natural ID.

### Path Parameters

| Tên | Loại | Mô tả                        | Bắt buộc |
| :-- | :--- | :--------------------------- | :------- |
| roomId  | Long | ID của phòng                     | Có       |
| naturalId  | String | Mã định danh thiết bị vật lý       | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"naturalId": "FAN-ROOM-101",
		"name": "Quạt trần phòng khách",
		"description": "Quạt trần",
		"isActive": true,
		"roomId": 5,
		"power": "ON",
		"type": "IR",
		"speed": 3,
		"mode": "COOL",
		"swing": "ON",
		"light": "OFF"
	},
	"timestamp": "2026-02-24T10:00:00Z"
}
```

---

## GET /api/v1/fans/{id}

> Lấy thông tin chi tiết của một thiết bị quạt theo ID hệ thống.

### Path Parameters

| Tên | Loại | Mô tả                   | Bắt buộc |
| :-- | :--- | :---------------------- | :------- |
| id  | Long | ID của thiết bị cần lấy | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"naturalId": "FAN-ROOM-101",
		"name": "Quạt trần",
		"isActive": true,
		"roomId": 5,
		"power": "ON",
		"type": "IR"
	},
	"timestamp": "2026-02-24T10:00:00Z"
}
```

---

## POST /api/v1/fans

> Tạo mới một thiết bị quạt. Phải chỉ định loại cấu hình (GPIO hoặc IR).

### Request Body

| Tên trường      | Loại    | Bắt buộc | Mô tả                                                 |
| :-------------- | :------ | :------- | :---------------------------------------------------- |
| naturalId       | string  | Có       | Mã định danh vật lý của thiết bị (**UNIQUE**)         |
| name            | string  | Có       | Tên thiết bị (1-100 ký tự)                            |
| type            | string  | Có       | Loại quạt: `GPIO` hoặc `IR`                           |
| description     | string  | Không    | Mô tả chi tiết (tối đa 255 ký tự)                     |
| isActive        | boolean | Không    | Trạng thái kích hoạt (mặc định: false)                |
| roomId          | Long    | Có       | ID phòng chứa thiết bị                                |
| deviceControlId | Long    | Không    | ID bộ điều khiển trung tâm (Gateway)                  |
| langCode        | string  | Không    | Mã ngôn ngữ (tối đa 10 ký tự, mặc định: vi)           |
| power           | string  | Không    | Trạng thái nguồn: `ON`, `OFF` (mặc định: OFF)         |
| mode            | string  | Không    | Chế độ cấu hình (chỉ áp dụng IR)                      |
| swing           | string  | Không    | Đảo gió: `ON`, `OFF` (chỉ áp dụng IR)                 |
| light           | string  | Không    | Bật/tắt đèn quạt phụ: `ON`, `OFF` (chỉ áp dụng IR)    |
| speed           | int     | Không    | Tốc độ quạt (chỉ áp dụng IR)                          |

### Request Example

```json
{
	"naturalId": "FAN-ROOM-101",
	"name": "Quạt trần phòng khách",
	"type": "IR",
	"isActive": true,
	"roomId": 5,
	"deviceControlId": 3,
	"langCode": "vi"
}
```

### Response (201 Created)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 1,
		"naturalId": "FAN-ROOM-101",
		"name": "Quạt trần phòng khách",
		"type": "IR",
		"isActive": true,
		"roomId": 5,
		"power": "OFF"
	},
	"timestamp": "2026-02-24T10:00:00Z"
}
```

---

## PUT /api/v1/fans/{id}

> Cập nhật cấu hình thông tin thiết bị quạt.
> **Lưu ý:** Dùng để cập nhật cấu hình hệ thống (name, description, device controller), không dùng để điều khiển trạng thái IoT vật lý.

### Path Parameters

| Tên | Loại | Mô tả                        | Bắt buộc |
| :-- | :--- | :--------------------------- | :------- |
| id  | Long | ID của thiết bị cần cập nhật | Có       |

### Request Body

Tương tự JSON của POST /api/v1/fans nhưng các field đều là Optional, bao gồm `type` nếu muốn thay đổi mô hình quạt.

### Request Example

```json
{
	"name": "Quạt trần phòng ngủ"
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Quạt trần phòng ngủ"
	},
	"timestamp": "2026-02-24T10:00:00Z"
}
```

---

## DELETE /api/v1/fans/{id}

> Xóa thiết bị quạt khỏi hệ thống.

### Path Parameters

| Tên | Loại | Mô tả                   | Bắt buộc |
| :-- | :--- | :---------------------- | :------- |
| id  | Long | ID của thiết bị cần xóa | Có       |

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2026-02-24T10:00:00Z"
}
```

---

## PUT /api/v1/fans/{id}/power

> Điều khiển nguồn điện của thiết bị (Bật/Tắt).

### Path Parameters

| Tên | Loại | Mô tả                          | Bắt buộc |
| :-- | :--- | :----------------------------- | :------- |
| id  | Long | ID của thiết bị cần điều khiển | Có       |

### Query Parameters

| Tên   | Loại   | Mô tả                             | Bắt buộc |
| :---- | :----- | :-------------------------------- | :------- |
| state | string | Trạng thái nguồn: `ON` hoặc `OFF` | Có       |

### Request Example

```
PUT /api/v1/fans/1/power?state=ON
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Power controlled successfully",
	"data": null,
	"timestamp": "2026-02-24T10:00:00Z"
}
```

---

## PUT /api/v1/fans/{id}/mode

> Thay đổi chế độ hoạt động của quạt (Chỉ hỗ trợ IR).

### Path Parameters

| Tên | Loại | Mô tả                          | Bắt buộc |
| :-- | :--- | :----------------------------- | :------- |
| id  | Long | ID của thiết bị cần điều khiển | Có       |

### Query Parameters

| Tên   | Loại   | Mô tả                                                                  | Bắt buộc |
| :---- | :----- | :--------------------------------------------------------------------- | :------- |
| value | string | Chế độ: `COOL`, `HEAT`, `DRY`, `FAN`, `AUTO`                           | Có       |

### Request Example

```
PUT /api/v1/fans/1/mode?value=COOL
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Mode controlled successfully",
	"data": null,
	"timestamp": "2026-02-24T10:00:00Z"
}
```

---

## PUT /api/v1/fans/{id}/fan

> Điều chỉnh tốc độ gió của thiết bị (Chỉ hỗ trợ cho quạt cấu hình IR).

### Path Parameters

| Tên | Loại | Mô tả                          | Bắt buộc |
| :-- | :--- | :----------------------------- | :------- |
| id  | Long | ID của thiết bị cần điều khiển | Có       |

### Query Parameters

| Tên   | Loại | Mô tả             | Bắt buộc |
| :---- | :--- | :---------------- | :------- |
| speed | int  | Tốc độ gió truyền | Có       |

### Request Example

```
PUT /api/v1/fans/1/fan?speed=3
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Fan speed controlled successfully",
	"data": null,
	"timestamp": "2026-02-24T10:00:00Z"
}
```

---

## PUT /api/v1/fans/{id}/swing

> Điều khiển trạng thái quay/đảo gió (Chỉ hỗ trợ IR).

### Path Parameters

| Tên | Loại | Mô tả                          | Bắt buộc |
| :-- | :--- | :----------------------------- | :------- |
| id  | Long | ID của thiết bị cần điều khiển | Có       |

### Query Parameters

| Tên   | Loại   | Mô tả                               | Bắt buộc |
| :---- | :----- | :---------------------------------- | :------- |
| state | string | Trạng thái đảo gió: `ON` hoặc `OFF` | Có       |

### Request Example

```
PUT /api/v1/fans/1/swing?state=OFF
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Swing controlled successfully",
	"data": null,
	"timestamp": "2026-02-24T10:00:00Z"
}
```

---

## PUT /api/v1/fans/{id}/light

> Bật tắt đèn đính kèm với thiết bị quạt trần (Chỉ hỗ trợ IR).

### Path Parameters

| Tên | Loại | Mô tả                          | Bắt buộc |
| :-- | :--- | :----------------------------- | :------- |
| id  | Long | ID của thiết bị cần điều khiển | Có       |

### Query Parameters

| Tên   | Loại   | Mô tả                               | Bắt buộc |
| :---- | :----- | :---------------------------------- | :------- |
| state | string | Trạng thái đèn quạt: `ON` hoặc `OFF`| Có       |

### Request Example

```
PUT /api/v1/fans/1/light?state=ON
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Light controlled successfully",
	"data": null,
	"timestamp": "2026-02-24T10:00:00Z"
}
```
