# Fan Module

## API Documentation for Fan Management (v1)

> Quản lý thiết bị quạt (Fan - bao gồm Quạt thông thường GPIO và Quạt hồng ngoại IR) trong hệ thống Smart Room

---

## CRUD Operations

<details>
<summary><b>GET</b> <code>/api/v1/fans</code> - Lấy danh sách quạt (phân trang)</summary>

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
				"mode": "NORMAL",
				"swing": "ON",
				"light": "OFF"
			}
		],
		"page": 0,
		"size": 20,
		"totalElements": 10,
		"totalPages": 1
	},
	"timestamp": "2026-02-28T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/fans/all</code> - Lấy tất cả quạt (không phân trang)</summary>

> Lấy danh sách tất cả các thiết bị quạt mà không phân trang.

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"naturalId": "FAN-ROOM-101",
			"name": "Quạt trần phòng khách",
			"description": "Quạt trần IR",
			"isActive": true,
			"roomId": 5,
			"power": "ON",
			"type": "IR",
			"speed": 3,
			"mode": "NORMAL",
			"swing": "ON",
			"light": "OFF"
		},
		{
			"id": 2,
			"naturalId": "FAN-GPIO-102",
			"name": "Quạt đứng phòng ngủ",
			"description": "Quạt GPIO",
			"isActive": true,
			"roomId": 6,
			"power": "OFF",
			"type": "GPIO",
			"speed": null,
			"mode": null,
			"swing": null,
			"light": null
		}
	],
	"timestamp": "2026-02-28T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/fans</code> - Lấy quạt theo phòng (phân trang)</summary>

> Lấy danh sách các thiết bị quạt trong một phòng cụ thể với phân trang.

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
				"name": "Quạt trần phòng khách",
				"description": "Quạt IR",
				"isActive": true,
				"roomId": 5,
				"power": "ON",
				"type": "IR",
				"speed": 3,
				"mode": "NORMAL",
				"swing": "ON",
				"light": "OFF"
			}
		],
		"page": 0,
		"size": 20,
		"totalElements": 2,
		"totalPages": 1
	},
	"timestamp": "2026-02-28T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/fans/all</code> - Lấy tất cả quạt theo phòng (không phân trang)</summary>

> Lấy danh sách tất cả các thiết bị quạt trong một phòng cụ thể mà không phân trang.

### Path Parameters

| Tên    | Loại | Mô tả                         | Bắt buộc |
| :----- | :--- | :---------------------------- | :------- |
| roomId | Long | ID của phòng cần lấy thiết bị | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"naturalId": "FAN-ROOM-101",
			"name": "Quạt trần",
			"description": "Quạt IR",
			"isActive": true,
			"roomId": 5,
			"power": "ON",
			"type": "IR",
			"speed": 3,
			"mode": "NORMAL",
			"swing": "ON",
			"light": "OFF"
		},
		{
			"id": 3,
			"naturalId": "FAN-GPIO-501",
			"name": "Quạt đứng",
			"description": "Quạt GPIO",
			"isActive": true,
			"roomId": 5,
			"power": "OFF",
			"type": "GPIO",
			"speed": null,
			"mode": null,
			"swing": null,
			"light": null
		}
	],
	"timestamp": "2026-02-28T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/fans/{naturalId}</code> - Lấy chi tiết quạt theo phòng và naturalId</summary>

> Lấy thông tin chi tiết của một thiết bị quạt dựa vào Room ID và Natural ID.

### Path Parameters

| Tên       | Loại   | Mô tả                        | Bắt buộc |
| :-------- | :----- | :--------------------------- | :------- |
| roomId    | Long   | ID của phòng                 | Có       |
| naturalId | String | Mã định danh thiết bị vật lý | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"naturalId": "FAN-ROOM-101",
		"name": "Quạt trần phòng khách",
		"description": "Quạt trần IR",
		"isActive": true,
		"roomId": 5,
		"power": "ON",
		"type": "IR",
		"speed": 3,
		"mode": "NORMAL",
		"swing": "ON",
		"light": "OFF"
	},
	"timestamp": "2026-02-28T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/fans/{id}</code> - Lấy chi tiết quạt theo ID</summary>

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
		"name": "Quạt trần phòng khách",
		"description": "Quạt IR",
		"isActive": true,
		"roomId": 5,
		"power": "ON",
		"type": "IR",
		"speed": 3,
		"mode": "NORMAL",
		"swing": "ON",
		"light": "OFF"
	},
	"timestamp": "2026-02-28T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/fans</code> - Tạo quạt mới</summary>

> Tạo mới một thiết bị quạt. Phải chỉ định loại cấu hình (GPIO hoặc IR). Cấu trúc request body phụ thuộc vào `type`.

### Request Body - Tạo quạt GPIO (`type: "GPIO"`)

| Tên trường      | Loại    | Bắt buộc | Mô tả                                       |
| :-------------- | :------ | :------- | :------------------------------------------ |
| type            | string  | Có       | Phải là `GPIO`                              |
| name            | string  | Có       | Tên thiết bị (1-100 ký tự)                  |
| naturalId       | string  | Có       | Mã định danh vật lý (**UNIQUE**)            |
| roomId          | Long    | Có       | ID phòng chứa thiết bị                      |
| description     | string  | Không    | Mô tả chi tiết (tối đa 255 ký tự)           |
| isActive        | boolean | Không    | Trạng thái kích hoạt (mặc định: false)      |
| deviceControlId | Long    | Không    | ID bộ điều khiển trung tâm (Gateway)        |
| langCode        | string  | Không    | Mã ngôn ngữ (tối đa 10 ký tự, mặc định: vi) |
| power           | string  | Không    | Trạng thái nguồn: `ON`, `OFF` (mặc định: OFF) |

### Request Body - Tạo quạt IR (`type: "IR"`)

| Tên trường      | Loại    | Bắt buộc | Mô tả                                       |
| :-------------- | :------ | :------- | :------------------------------------------ |
| type            | string  | Có       | Phải là `IR`                                |
| name            | string  | Có       | Tên thiết bị (1-100 ký tự)                  |
| naturalId       | string  | Có       | Mã định danh vật lý (**UNIQUE**)            |
| roomId          | Long    | Có       | ID phòng chứa thiết bị                      |
| mode            | string  | Có       | Chế độ: `NORMAL`, `SLEEP`, `NATURAL`        |
| speed           | int     | Có       | Tốc độ quạt (0-9999)                        |
| swing           | string  | Có       | Đảo gió: `ON`, `OFF`                        |
| light           | string  | Có       | Đèn quạt: `ON`, `OFF`                       |
| description     | string  | Không    | Mô tả chi tiết (tối đa 255 ký tự)           |
| isActive        | boolean | Không    | Trạng thái kích hoạt (mặc định: false)      |
| deviceControlId | Long    | Không    | ID bộ điều khiển trung tâm (Gateway)        |
| langCode        | string  | Không    | Mã ngôn ngữ (tối đa 10 ký tự, mặc định: vi) |
| power           | string  | Không    | Trạng thái nguồn: `ON`, `OFF` (mặc định: OFF) |

### Request Example - GPIO (Minimal - Chỉ field bắt buộc)

```json
{
	"type": "GPIO",
	"name": "Quạt đứng phòng ngủ",
	"naturalId": "FAN-GPIO-102",
	"roomId": 6
}
```

### Request Example - GPIO (Full - Có thêm optional fields)

```json
{
	"type": "GPIO",
	"name": "Quạt đứng phòng ngủ",
	"naturalId": "FAN-GPIO-102",
	"roomId": 6,
	"description": "Quạt GPIO điều khiển đơn giản",
	"isActive": true,
	"power": "OFF",
	"deviceControlId": 3,
	"langCode": "vi"
}
```

### Request Example - IR (Minimal - Chỉ field bắt buộc)

```json
{
	"type": "IR",
	"name": "Quạt trần phòng khách",
	"naturalId": "FAN-ROOM-101",
	"roomId": 5,
	"mode": "NORMAL",
	"speed": 1,
	"swing": "OFF",
	"light": "OFF"
}
```

### Request Example - IR (Full - Có thêm optional fields)

```json
{
	"type": "IR",
	"name": "Quạt trần phòng khách",
	"naturalId": "FAN-ROOM-101",
	"roomId": 5,
	"mode": "NORMAL",
	"speed": 1,
	"swing": "OFF",
	"light": "OFF",
	"description": "Quạt trần IR với remote",
	"isActive": true,
	"power": "OFF",
	"deviceControlId": 3,
	"langCode": "vi"
}
```

### Response (201 Created)

```json
{
	"status": 201,
	"message": "Created successfully",
	"data": {
		"id": 1,
		"naturalId": "FAN-ROOM-101",
		"name": "Quạt trần phòng khách",
		"description": "Quạt trần IR với remote",
		"isActive": true,
		"roomId": 5,
		"power": "OFF",
		"type": "IR",
		"speed": 1,
		"mode": "NORMAL",
		"swing": "OFF",
		"light": "OFF"
	},
	"timestamp": "2026-02-28T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/fans/{id}</code> - Cập nhật thông tin quạt</summary>

> Cập nhật cấu hình thông tin thiết bị quạt.
>
> **Lưu ý:** 
> - Dùng để cập nhật cấu hình hệ thống (name, description, device controller, etc.)
> - Để điều khiển thiết bị vật lý (power, mode, swing, light, speed), sử dụng endpoint `/api/v1/fans/{naturalId}/control`
> - Không thể thay đổi `type` sau khi tạo (sẽ báo lỗi)
> - **Tất cả các trường đều optional** - chỉ gửi những trường muốn thay đổi

### Path Parameters

| Tên | Loại | Mô tả                        | Bắt buộc |
| :-- | :--- | :--------------------------- | :------- |
| id  | Long | ID của thiết bị cần cập nhật | Có       |

### Request Body - Cập nhật quạt GPIO (tất cả optional)

| Tên trường      | Loại    | Bắt buộc | Mô tả                                       |
| :-------------- | :------ | :------- | :------------------------------------------ |
| type            | string  | Không    | **Không thể thay đổi** - chỉ để validation |
| name            | string  | Không    | Tên thiết bị (1-100 ký tự)                  |
| naturalId       | string  | Không    | Mã định danh vật lý (**UNIQUE**)            |
| roomId          | Long    | Không    | ID phòng chứa thiết bị                      |
| description     | string  | Không    | Mô tả chi tiết (tối đa 255 ký tự)           |
| isActive        | boolean | Không    | Trạng thái kích hoạt                        |
| deviceControlId | Long    | Không    | ID bộ điều khiển trung tâm                  |
| langCode        | string  | Không    | Mã ngôn ngữ (tối đa 10 ký tự)               |
| power           | string  | Không    | Trạng thái nguồn: `ON`, `OFF`               |

### Request Body - Cập nhật quạt IR (tất cả optional)

| Tên trường      | Loại    | Bắt buộc | Mô tả                                       |
| :-------------- | :------ | :------- | :------------------------------------------ |
| type            | string  | Không    | **Không thể thay đổi** - chỉ để validation |
| name            | string  | Không    | Tên thiết bị (1-100 ký tự)                  |
| naturalId       | string  | Không    | Mã định danh vật lý (**UNIQUE**)            |
| roomId          | Long    | Không    | ID phòng chứa thiết bị                      |
| description     | string  | Không    | Mô tả chi tiết (tối đa 255 ký tự)           |
| isActive        | boolean | Không    | Trạng thái kích hoạt                        |
| deviceControlId | Long    | Không    | ID bộ điều khiển trung tâm                  |
| langCode        | string  | Không    | Mã ngôn ngữ (tối đa 10 ký tự)               |
| power           | string  | Không    | Trạng thái nguồn: `ON`, `OFF`               |
| mode            | string  | Không    | Chế độ: `NORMAL`, `SLEEP`, `NATURAL`        |
| speed           | int     | Không    | Tốc độ quạt (0-9999)                        |
| swing           | string  | Không    | Đảo gió: `ON`, `OFF`                        |
| light           | string  | Không    | Đèn quạt: `ON`, `OFF`                       |

### Request Example - GPIO (Chỉ cập nhật name)

```json
{
	"name": "Quạt đứng phòng ngủ - đã sửa tên"
}
```

### Request Example - GPIO (Cập nhật nhiều fields)

```json
{
	"name": "Quạt đứng phòng ngủ master",
	"description": "Đã cập nhật mô tả",
	"isActive": true,
	"roomId": 7
}
```

### Request Example - IR (Chỉ cập nhật name và mode)

```json
{
	"name": "Quạt trần phòng ngủ master",
	"mode": "SLEEP"
}
```

### Request Example - IR (Cập nhật nhiều fields)

```json
{
	"name": "Quạt trần phòng khách VIP",
	"description": "Đã cập nhật mô tả và cấu hình",
	"isActive": true,
	"mode": "NATURAL",
	"speed": 5
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"naturalId": "FAN-ROOM-101",
		"name": "Quạt trần phòng ngủ master",
		"description": "Đã cập nhật mô tả",
		"isActive": true,
		"roomId": 5,
		"power": "ON",
		"type": "IR",
		"speed": 3,
		"mode": "NORMAL",
		"swing": "ON",
		"light": "OFF"
	},
	"timestamp": "2026-02-28T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/fans/{id}</code> - Xóa quạt</summary>

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
	"timestamp": "2026-02-28T10:00:00Z"
}
```

</details>

<br>

---

## Control Operations

<details>
<summary><b>PUT</b> <code>/api/v1/fans/{naturalId}/control</code> - Điều khiển quạt (Unified Control Endpoint)</summary>

> Endpoint thống nhất để điều khiển thiết bị quạt vật lý. Hỗ trợ điều khiển nhiều thuộc tính cùng lúc.
>
> **Lưu ý:**
> - Sử dụng `naturalId` thay vì `id` để điều khiển
> - Tất cả các trường đều optional, chỉ gửi những trường muốn thay đổi
> - Các trường `mode`, `speed`, `swing`, `light` chỉ có hiệu lực với quạt loại IR

### Path Parameters

| Tên       | Loại   | Mô tả                        | Bắt buộc |
| :-------- | :----- | :--------------------------- | :------- |
| naturalId | String | Mã định danh thiết bị vật lý | Có       |

### Request Body

| Tên trường | Loại   | Bắt buộc | Mô tả                                     |
| :--------- | :----- | :------- | :---------------------------------------- |
| power      | string | Không    | Nguồn: `ON`, `OFF`                        |
| mode       | string | Không    | Chế độ (IR only): `NORMAL`, `SLEEP`, `NATURAL` |
| speed      | int    | Không    | Tốc độ quạt (IR only): 0-9999             |
| swing      | string | Không    | Đảo gió (IR only): `ON`, `OFF`            |
| light      | string | Không    | Đèn quạt (IR only): `ON`, `OFF`           |

### Request Example - Bật quạt và đặt tốc độ

```json
{
	"power": "ON",
	"speed": 5,
	"mode": "NORMAL"
}
```

### Request Example - Chỉ bật/tắt

```json
{
	"power": "OFF"
}
```

### Request Example - Điều khiển toàn bộ (IR)

```json
{
	"power": "ON",
	"mode": "SLEEP",
	"speed": 2,
	"swing": "ON",
	"light": "OFF"
}
```

### Response (202 Accepted)

```json
{
	"status": 202,
	"message": "Controlled successfully",
	"data": null,
	"timestamp": "2026-02-28T10:00:00Z"
}
```

</details>

<br>

---

## Enumerations

### FanType

Loại quạt trong hệ thống:

| Giá trị | Mô tả                                  |
| :------ | :------------------------------------- |
| `GPIO`  | Quạt thông thường điều khiển qua GPIO  |
| `IR`    | Quạt hồng ngoại điều khiển qua remote  |

### ActuatorPower

Trạng thái nguồn điện:

| Giá trị | Mô tả |
| :------ | :---- |
| `ON`    | Bật   |
| `OFF`   | Tắt   |

### ActuatorMode (IR Fan Only)

Chế độ hoạt động của quạt IR:

| Giá trị   | Mô tả                |
| :-------- | :------------------- |
| `NORMAL`  | Chế độ bình thường   |
| `SLEEP`   | Chế độ ngủ           |
| `NATURAL` | Chế độ gió tự nhiên  |

### ActuatorSwing (IR Fan Only)

Trạng thái đảo gió:

| Giá trị | Mô tả      |
| :------ | :--------- |
| `ON`    | Bật đảo gió |
| `OFF`   | Tắt đảo gió |

### ActuatorState (IR Fan Only)

Trạng thái đèn quạt:

| Giá trị | Mô tả  |
| :------ | :----- |
| `ON`    | Bật đèn |
| `OFF`   | Tắt đèn |

---
