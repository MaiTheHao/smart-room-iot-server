# Temperature Module

## Quản lý cảm biến nhiệt độ & ghi nhận dữ liệu

---

### GET /api/v1/rooms/{roomId}/temperatures

> Lấy danh sách cảm biến nhiệt độ theo phòng, có phân trang.

#### Tham số Đường dẫn (Path Parameters)

| Tên    | Loại | Mô tả    | Bắt buộc |
| :----- | :--- | :------- | :------- |
| roomId | Long | ID phòng | Có       |

#### Tham số Truy vấn (Query Parameters)

| Tên  | Loại | Mô tả            | Mặc định |
| :--- | :--- | :--------------- | :------- |
| page | int  | Trang hiện tại   | 0        |
| size | int  | Số phần tử/trang | 20       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"name": "Sensor 1",
				"description": "Phòng khách",
				"isActive": true,
				"currentValue": 28.5,
				"naturalId": "T001",
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

---

### GET /api/v1/temperatures/{id}

> Lấy thông tin chi tiết cảm biến nhiệt độ theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID cảm biến | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Sensor 1",
		"description": "Phòng khách",
		"isActive": true,
		"currentValue": 28.5,
		"naturalId": "T001",
		"roomId": 10
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/temperatures

> Tạo mới cảm biến nhiệt độ.

#### Request Body Fields

| Tên trường      | Loại   | Bắt buộc | Mô tả                  |
| :-------------- | :----- | :------- | :--------------------- |
| name            | string | Có       | Tên cảm biến           |
| description     | string | Không    | Mô tả                  |
| isActive        | bool   | Không    | Trạng thái hoạt động   |
| naturalId       | string | Không    | Mã định danh tự nhiên  |
| langCode        | string | Không    | Mã ngôn ngữ            |
| roomId          | Long   | Có       | ID phòng               |
| deviceControlId | Long   | Có       | ID thiết bị điều khiển |

#### Ví dụ Request Body

```json
{
	"name": "Sensor 1",
	"description": "Phòng khách",
	"isActive": true,
	"naturalId": "T001",
	"langCode": "vi",
	"roomId": 10,
	"deviceControlId": 5
}
```

#### Ví dụ Response (201 Created)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 1,
		"name": "Sensor 1",
		"description": "Phòng khách",
		"isActive": true,
		"currentValue": null,
		"naturalId": "T001",
		"roomId": 10
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### PUT /api/v1/temperatures/{id}

> Cập nhật thông tin cảm biến nhiệt độ.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID cảm biến | Có       |

#### Request Body Fields

| Tên trường      | Loại   | Bắt buộc | Mô tả                  |
| :-------------- | :----- | :------- | :--------------------- |
| name            | string | Không    | Tên cảm biến           |
| description     | string | Không    | Mô tả                  |
| isActive        | bool   | Không    | Trạng thái hoạt động   |
| naturalId       | string | Không    | Mã định danh tự nhiên  |
| langCode        | string | Không    | Mã ngôn ngữ            |
| deviceControlId | Long   | Không    | ID thiết bị điều khiển |

#### Ví dụ Request Body

```json
{
	"name": "Sensor 1 Updated",
	"description": "Phòng khách VIP",
	"isActive": false,
	"naturalId": "T001U",
	"langCode": "vi",
	"deviceControlId": 6
}
```

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Sensor 1 Updated",
		"description": "Phòng khách VIP",
		"isActive": false,
		"currentValue": 28.5,
		"naturalId": "T001U",
		"roomId": 10
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### DELETE /api/v1/temperatures/{id}

> Xóa cảm biến nhiệt độ.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID cảm biến | Có       |

#### Ví dụ Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### GET /api/v1/rooms/{roomId}/temperature-values/average

> Lấy giá trị nhiệt độ trung bình theo phòng trong khoảng thời gian.

#### Tham số Đường dẫn (Path Parameters)

| Tên    | Loại | Mô tả    | Bắt buộc |
| :----- | :--- | :------- | :------- |
| roomId | Long | ID phòng | Có       |

#### Tham số Truy vấn (Query Parameters)

| Tên  | Loại    | Mô tả              | Bắt buộc |
| :--- | :------ | :----------------- | :------- |
| from | Instant | Thời gian bắt đầu  | Có       |
| to   | Instant | Thời gian kết thúc | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"timestamp": "2024-06-07T09:00:00Z",
			"avgTempC": 27.8
		},
		{
			"timestamp": "2024-06-07T10:00:00Z",
			"avgTempC": 28.2
		}
	],
	"timestamp": "2024-06-07T11:00:00Z"
}
```

---
