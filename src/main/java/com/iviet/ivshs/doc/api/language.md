# Language API Documentation

## Ngôn ngữ (Language) Module

---

### GET /api/v1/languages

> Lấy danh sách tất cả các ngôn ngữ (có phân trang).

#### Tham số Truy vấn (Query Parameters)

| Tên Tham số | Loại | Mô tả                   | Mặc định |
| :---------- | :--- | :---------------------- | :------- |
| **page**    | int  | Số trang (bắt đầu từ 0) | 0        |
| **size**    | int  | Kích thước trang        | 10       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"code": "en",
				"name": "English",
				"description": "English language",
				"priority": 1
			}
		],
		"page": 0,
		"size": 10,
		"totalElements": 1,
		"totalPages": 1
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### GET /api/v1/languages/{id}

> Lấy thông tin chi tiết của một ngôn ngữ bằng ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên Tham số | Loại | Mô tả                   |
| :---------- | :--- | :---------------------- |
| **id**      | Long | ID của ngôn ngữ cần lấy |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"code": "en",
		"name": "English",
		"description": "English language",
		"priority": 1
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### GET /api/v1/languages/code/{code}

> Lấy thông tin chi tiết của một ngôn ngữ bằng mã code.

#### Tham số Đường dẫn (Path Parameters)

| Tên Tham số | Loại   | Mô tả                |
| :---------- | :----- | :------------------- |
| **code**    | string | Mã code của ngôn ngữ |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"code": "en",
		"name": "English",
		"description": "English language",
		"priority": 1
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/languages

> Tạo một ngôn ngữ mới.

#### Request Body Fields

| Tên trường      | Loại   | Bắt buộc | Mô tả                             |
| :-------------- | :----- | :------- | :-------------------------------- |
| **code**        | string | Có       | Mã ngôn ngữ (2-10 ký tự)          |
| **name**        | string | Có       | Tên ngôn ngữ (1-100 ký tự)        |
| **description** | string | Không    | Mô tả chi tiết (tối đa 255 ký tự) |
| **priority**    | int    | Không    | Độ ưu tiên                        |

#### Ví dụ Request Body

```json
{
	"code": "en",
	"name": "English",
	"description": "English language",
	"priority": 1
}
```

#### Ví dụ Response (201 Created)

```json
{
	"status": 201,
	"message": "Created successfully",
	"data": {
		"id": 1,
		"code": "en",
		"name": "English",
		"description": "English language",
		"priority": 1
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### PUT /api/v1/languages/{id}

> Cập nhật thông tin của một ngôn ngữ bằng ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên Tham số | Loại | Mô tả                        |
| :---------- | :--- | :--------------------------- |
| **id**      | Long | ID của ngôn ngữ cần cập nhật |

#### Request Body Fields

| Tên trường      | Loại   | Bắt buộc | Mô tả                             |
| :-------------- | :----- | :------- | :-------------------------------- |
| **code**        | string | Không    | Mã ngôn ngữ (2-10 ký tự)          |
| **name**        | string | Có       | Tên ngôn ngữ (1-100 ký tự)        |
| **description** | string | Không    | Mô tả chi tiết (tối đa 255 ký tự) |
| **priority**    | int    | Không    | Độ ưu tiên                        |

#### Ví dụ Request Body

```json
{
	"code": "en",
	"name": "English Updated",
	"description": "Updated description",
	"priority": 2
}
```

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"code": "en",
		"name": "English Updated",
		"description": "Updated description",
		"priority": 2
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### DELETE /api/v1/languages/{id}

> Xóa một ngôn ngữ bằng ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên Tham số | Loại | Mô tả                   |
| :---------- | :--- | :---------------------- |
| **id**      | Long | ID của ngôn ngữ cần xóa |

#### Ví dụ Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```
