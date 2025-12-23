# Light Module

## Quản lý thiết bị chiếu sáng & điều khiển phần cứng

---

### GET /api/v1/lights

> Lấy danh sách thiết bị chiếu sáng (phân trang).

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
				"name": "Đèn phòng khách",
				"description": "Đèn LED trần",
				"isActive": true,
				"level": 80,
				"roomId": 10
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

---

### GET /api/v1/lights/room/{roomId}

> Lấy danh sách thiết bị chiếu sáng theo phòng (phân trang).

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
				"id": 2,
				"name": "Đèn bàn",
				"description": "Đèn bàn học",
				"isActive": false,
				"level": 0,
				"roomId": 10
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

---

### GET /api/v1/lights/{id}

> Lấy thông tin chi tiết thiết bị chiếu sáng theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID thiết bị | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Đèn phòng khách",
		"description": "Đèn LED trần",
		"isActive": true,
		"level": 80,
		"roomId": 10
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

---

### POST /api/v1/lights

> Tạo mới thiết bị chiếu sáng.

#### Request Body Fields

| Tên trường      | Loại   | Bắt buộc | Mô tả                  |
| :-------------- | :----- | :------- | :--------------------- |
| name            | string | Có       | Tên thiết bị           |
| description     | string | Không    | Mô tả                  |
| isActive        | bool   | Không    | Trạng thái hoạt động   |
| level           | int    | Không    | Độ sáng (0-100)        |
| langCode        | string | Không    | Mã ngôn ngữ            |
| roomId          | Long   | Có       | ID phòng               |
| deviceControlId | Long   | Có       | ID thiết bị điều khiển |

#### Ví dụ Request Body

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

#### Ví dụ Response (201 Created)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 1,
		"name": "Đèn phòng khách",
		"description": "Đèn LED trần",
		"isActive": true,
		"level": 80,
		"roomId": 10
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

---

### PUT /api/v1/lights/{id}

> Cập nhật thông tin thiết bị chiếu sáng.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID thiết bị | Có       |

#### Request Body Fields

| Tên trường      | Loại   | Bắt buộc | Mô tả                  |
| :-------------- | :----- | :------- | :--------------------- |
| name            | string | Không    | Tên thiết bị           |
| description     | string | Không    | Mô tả                  |
| isActive        | bool   | Không    | Trạng thái hoạt động   |
| level           | int    | Không    | Độ sáng (0-100)        |
| langCode        | string | Không    | Mã ngôn ngữ            |
| roomId          | Long   | Không    | ID phòng mới           |
| deviceControlId | Long   | Không    | ID thiết bị điều khiển |

#### Ví dụ Request Body

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

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Đèn phòng khách VIP",
		"description": "Đèn LED cao cấp",
		"isActive": false,
		"level": 50,
		"roomId": 11
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

---

### DELETE /api/v1/lights/{id}

> Xóa thiết bị chiếu sáng.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID thiết bị | Có       |

#### Ví dụ Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2025-11-29T09:00:00Z"
}
```

---

### PUT /api/v1/lights/{id}/toggle-state

> Bật/tắt thiết bị chiếu sáng.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID thiết bị | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"status": "ON",
		"message": "Light turned on successfully",
		"error": null
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

---

### PUT /api/v1/lights/{id}/level/{newLevel}

> Điều chỉnh độ sáng của thiết bị chiếu sáng.

#### Tham số Đường dẫn (Path Parameters)

| Tên      | Loại | Mô tả               | Bắt buộc |
| :------- | :--- | :------------------ | :------- |
| id       | Long | ID thiết bị         | Có       |
| newLevel | int  | Độ sáng mới (0-100) | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"status": "OK",
		"message": "Light level set to 75 successfully",
		"error": null
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```

---

### GET /api/v1/lights/{id}/health-check

> Kiểm tra trạng thái sức khỏe thiết bị chiếu sáng.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả       | Bắt buộc |
| :-- | :--- | :---------- | :------- |
| id  | Long | ID thiết bị | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"isHealthy": true,
		"message": "Device is healthy",
		"lastCheckTime": "2025-11-29T09:00:00Z"
	},
	"timestamp": "2025-11-29T09:00:00Z"
}
```
