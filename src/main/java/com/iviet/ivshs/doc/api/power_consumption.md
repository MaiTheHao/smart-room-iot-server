# Power Consumption Module

## Danh sách các API quản lý cảm biến và dữ liệu tiêu thụ điện năng.

---

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/power-consumptions</code> - Lấy cảm biến điện năng theo phòng</summary>

> Lấy danh sách cảm biến tiêu thụ điện năng theo phòng, có phân trang.

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
				"id": 1,
				"name": "Power Sensor 1",
				"description": "Cảm biến phòng khách",
				"isActive": true,
				"currentWatt": 120.5,
				"naturalId": "P001",
				"roomId": 10
			}
		],
		"page": 0,
		"size": 20,
		"totalElements": 1,
		"totalPages": 1
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/power-consumptions/{id}</code> - Lấy chi tiết cảm biến</summary>

> Lấy thông tin chi tiết cảm biến tiêu thụ điện năng theo ID.

### Path Parameters

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID cảm biến | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Power Sensor 1",
		"description": "Cảm biến phòng khách",
		"isActive": true,
		"currentWatt": 120.5,
		"naturalId": "P001",
		"roomId": 10
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/power-consumptions</code> - Tạo mới cảm biến</summary>

> Tạo mới cảm biến tiêu thụ điện năng.

### Request Body

| Tên trường      | Loại   | Bắt buộc | Mô tả                  |
| :-------------- | :----- | :------- | :--------------------- |
| name            | string | Có       | Tên cảm biến           |
| description     | string | Không    | Mô tả                  |
| isActive        | bool   | Không    | Trạng thái hoạt động   |
| naturalId       | string | Không    | Mã định danh tự nhiên  |
| langCode        | string | Không    | Mã ngôn ngữ            |
| roomId          | Long   | Có       | ID phòng               |
| deviceControlId | Long   | Có       | ID thiết bị điều khiển |

### Request Example

```json
{
	"name": "Power Sensor 1",
	"description": "Cảm biến phòng khách",
	"isActive": true,
	"naturalId": "P001",
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
		"name": "Power Sensor 1",
		"description": "Cảm biến phòng khách",
		"isActive": true,
		"currentWatt": null,
		"naturalId": "P001",
		"roomId": 10
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/power-consumptions/{id}</code> - Cập nhật thông tin cảm biến</summary>

> Cập nhật thông tin cảm biến tiêu thụ điện năng.

### Path Parameters

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID cảm biến | Có       |

### Request Body

| Tên trường      | Loại   | Bắt buộc | Mô tả                  |
| :-------------- | :----- | :------- | :--------------------- |
| name            | string | Không    | Tên cảm biến           |
| description     | string | Không    | Mô tả                  |
| isActive        | bool   | Không    | Trạng thái hoạt động   |
| naturalId       | string | Không    | Mã định danh tự nhiên  |
| langCode        | string | Không    | Mã ngôn ngữ            |
| deviceControlId | Long   | Không    | ID thiết bị điều khiển |

### Request Example

```json
{
	"name": "Power Sensor 1 Updated",
	"description": "Cập nhật mô tả",
	"isActive": false,
	"naturalId": "P001U",
	"langCode": "vi",
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
		"name": "Power Sensor 1 Updated",
		"description": "Cập nhật mô tả",
		"isActive": false,
		"currentWatt": 120.5,
		"naturalId": "P001U",
		"roomId": 10
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/power-consumptions/{id}</code> - Xóa cảm biến</summary>

> Xóa cảm biến tiêu thụ điện năng.

### Path Parameters

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID cảm biến | Có       |

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/power-consumption-values/sum</code> - Tổng giá trị điện</summary>

> Lấy tổng giá trị tiêu thụ điện năng theo phòng trong khoảng thời gian.

### Path Parameters

| Tên    | Loại | Mô tả    | Bắt buộc |
| :----- | :--- | :------- | :------- |
| roomId | Long | ID phòng | Có       |

### Query Parameters

| Tên  | Loại    | Mô tả              | Bắt buộc |
| :--- | :------ | :----------------- | :------- |
| from | Instant | Thời gian bắt đầu  | Có       |
| to   | Instant | Thời gian kết thúc | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": "2024-06-07T09:00:00Z",
			"sumWatt": 450.75
		},
		{
			"timestamp": "2024-06-07T10:00:00Z",
			"sumWatt": 520.25
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

---
