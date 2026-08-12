# Light Module

## Quản lý thiết bị chiếu sáng & điều khiển phần cứng

---

## CRUD Operations

<details>
<summary><b>GET</b> <code>/api/v1/lights</code> - Lấy danh sách đèn (phân trang)</summary>

> Lấy danh sách thiết bị chiếu sáng (phân trang).

### Query Parameters

| Tên  | Loại | Mô tả            | Mặc định |
| :--- | :--- | :--------------- | :------- |
| page | int  | Trang hiện tại   | 0        |
| size | int  | Số phần tử/trang | 20       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"name": "Đèn phòng khách",
				"description": "Đèn LED trần",
				"isActive": true,
				"power": "ON",
				"level": 80,
				"roomId": 10,
				"deviceControlId": 1,
				"category": "LIGHT"
			}
		],
		"page": 0,
		"size": 20,
		"totalElements": 1,
		"totalPages": 1
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/lights/room/{roomId}</code> - Lấy đèn theo phòng</summary>

> Lấy danh sách thiết bị chiếu sáng theo phòng (phân trang).

### Path Parameters

| Tên    | Loại | Mô tả    | Bắt buộc |
| :----- | :--- | :------- | :------- |
| roomId | Long | ID phòng | Có       |

### Query Parameters

| Tên  | Loại | Mô tả            | Mặc định |
| :--- | :--- | :--------------- | :------- |
| page | int  | Trang hiện tại   | 0        |
| size | int  | Số phần tử/trang | 20       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 2,
				"name": "Đèn bàn",
				"description": "Đèn bàn học",
				"isActive": false,
				"power": "OFF",
				"level": 0,
				"roomId": 10,
				"deviceControlId": 2,
				"category": "LIGHT"
			}
		],
		"page": 0,
		"size": 20,
		"totalElements": 1,
		"totalPages": 1
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/lights/{id}</code> - Lấy chi tiết đèn</summary>

> Lấy thông tin chi tiết thiết bị chiếu sáng theo ID.

### Path Parameters

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID thiết bị | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Đèn phòng khách",
		"description": "Đèn LED trần",
		"isActive": true,
		"power": "ON",
		"level": 80,
		"roomId": 10,
		"deviceControlId": 1,
		"category": "LIGHT"
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/lights</code> - Tạo đèn mới</summary>

> Tạo mới thiết bị chiếu sáng.

### Request Body

| Tên trường      | Loại   | Bắt buộc | Mô tả                  |
| :-------------- | :----- | :------- | :--------------------- |
| name            | string | Có       | Tên thiết bị           |
| description     | string | Không    | Mô tả                  |
| isActive        | bool   | Không    | Trạng thái hoạt động   |
| level           | int    | Không    | Độ sáng (0-100)        |
| langCode        | string | Không    | Mã ngôn ngữ            |
| roomId          | Long   | Có       | ID phòng               |
| deviceControlId | Long   | Có       | ID thiết bị điều khiển |

### Request Example

```json
{
	"name": "Đèn phòng khách",
	"description": "Đèn LED trần",
	"isActive": true,
	"level": 80,
	"langCode": "vi",
	"roomId": 10,
	"deviceControlId": 5
}
```

### Response (201 Created)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 1,
		"name": "Đèn phòng khách",
		"description": "Đèn LED trần",
		"isActive": true,
		"power": "ON",
		"level": 80,
		"roomId": 10,
		"deviceControlId": 5,
		"category": "LIGHT"
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/lights/{id}</code> - Cập nhật thông tin đèn</summary>

> Cập nhật thông tin thiết bị chiếu sáng.

### Path Parameters

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID thiết bị | Có       |

### Request Body

| Tên trường      | Loại   | Bắt buộc | Mô tả                  |
| :-------------- | :----- | :------- | :--------------------- |
| name            | string | Không    | Tên thiết bị           |
| description     | string | Không    | Mô tả                  |
| isActive        | bool   | Không    | Trạng thái hoạt động   |
| level           | int    | Không    | Độ sáng (0-100)        |
| langCode        | string | Không    | Mã ngôn ngữ            |
| roomId          | Long   | Không    | ID phòng mới           |
| deviceControlId | Long   | Không    | ID thiết bị điều khiển |

### Request Example

```json
{
	"name": "Đèn phòng khách VIP",
	"description": "Đèn LED cao cấp",
	"isActive": false,
	"level": 50,
	"langCode": "vi",
	"roomId": 11,
	"deviceControlId": 6
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Đèn phòng khách VIP",
		"description": "Đèn LED cao cấp",
		"isActive": false,
		"power": "OFF",
		"level": 50,
		"roomId": 11,
		"deviceControlId": 6,
		"category": "LIGHT"
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/lights/{id}</code> - Xóa đèn</summary>

> Xóa thiết bị chiếu sáng.

### Path Parameters

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID thiết bị | Có       |

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2025-11-29T09:00:00Z"
}
```

</details>

<br>

---

## Control Operations

<details>
<summary><b>PUT</b> <code>/api/v1/lights/{naturalId}/control</code> - Điều khiển đèn (Power, Level) (Unified Control Endpoint)</summary>

> Điều khiển thiết bị chiếu sáng bằng mã định danh vật lý (naturalId).
>
> Hỗ trợ thay đổi trạng thái nguồn và/hoặc điều chỉnh độ sáng trong cùng một request.

### Path Parameters

| Tên       | Loại   | Mô tả                                       | Bắt buộc |
| :-------- | :----- | :------------------------------------------ | :------- |
| naturalId | string | Mã định danh vật lý của thiết bị chiếu sáng | Có       |

### Request Body

| Tên trường | Loại   | Bắt buộc | Mô tả                         |
| :--------- | :----- | :------- | :---------------------------- |
| power      | enum   | Không    | Trạng thái nguồn: `ON`, `OFF` |
| level      | int    | Không    | Độ sáng (0-100)               |

### Request Example

```json
{
	"power": "ON",
	"level": 80
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Controlled successfully",
	"data": {
		"successCount": 2,
		"totalCount": 2,
		"details": [
			{
				"parameter": "power",
				"success": true,
				"message": "Success"
			},
			{
				"parameter": "level",
				"success": true,
				"message": "Success"
			}
		]
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

</details>

<br>

---


<br>

---
