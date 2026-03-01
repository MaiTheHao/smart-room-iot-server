# Language API Documentation

## Ngôn ngữ (Language) Module

---

<details>
<summary><b>GET</b> <code>/api/v1/languages</code> - Lấy danh sách ngôn ngữ</summary>

> Lấy danh sách tất cả các ngôn ngữ (có phân trang).

### Query Parameters

| Tên Tham số | Loại | Mô tả                   | Mặc định |
| :---------- | :--- | :---------------------- | :------- |
| **page**    | int  | Số trang (bắt đầu từ 0) | 0        |
| **size**    | int  | Kích thước trang        | 10       |

### Response (200 OK)

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

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/languages/{id}</code> - Lấy ngôn ngữ theo ID</summary>

> Lấy thông tin chi tiết của một ngôn ngữ bằng ID.

### Path Parameters

| Tên Tham số | Loại | Mô tả                   |
| :---------- | :--- | :---------------------- |
| **id**      | Long | ID của ngôn ngữ cần lấy |

### Response (200 OK)

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

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/languages/code/{code}</code> - Lấy ngôn ngữ theo Code</summary>

> Lấy thông tin chi tiết của một ngôn ngữ bằng mã code.

### Path Parameters

| Tên Tham số | Loại   | Mô tả                |
| :---------- | :----- | :------------------- |
| **code**    | string | Mã code của ngôn ngữ |

### Response (200 OK)

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

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/languages</code> - Tạo ngôn ngữ mới</summary>

> Tạo một ngôn ngữ mới.

### Request Body

| Tên trường      | Loại   | Bắt buộc | Mô tả                             |
| :-------------- | :----- | :------- | :-------------------------------- |
| **code**        | string | Có       | Mã ngôn ngữ (2-10 ký tự)          |
| **name**        | string | Có       | Tên ngôn ngữ (1-100 ký tự)        |
| **description** | string | Không    | Mô tả chi tiết (tối đa 255 ký tự) |
| **priority**    | int    | Không    | Độ ưu tiên                        |

### Request Example

```json
{
	"code": "en",
	"name": "English",
	"description": "English language",
	"priority": 1
}
```

### Response (201 Created)

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

</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/languages/{id}</code> - Cập nhật ngôn ngữ</summary>

> Cập nhật thông tin của một ngôn ngữ bằng ID.

### Path Parameters

| Tên Tham số | Loại | Mô tả                        |
| :---------- | :--- | :--------------------------- |
| **id**      | Long | ID của ngôn ngữ cần cập nhật |

### Request Body

| Tên trường      | Loại   | Bắt buộc | Mô tả                             |
| :-------------- | :----- | :------- | :-------------------------------- |
| **code**        | string | Không    | Mã ngôn ngữ (2-10 ký tự)          |
| **name**        | string | Có       | Tên ngôn ngữ (1-100 ký tự)        |
| **description** | string | Không    | Mô tả chi tiết (tối đa 255 ký tự) |
| **priority**    | int    | Không    | Độ ưu tiên                        |

### Request Example

```json
{
	"code": "en",
	"name": "English Updated",
	"description": "Updated description",
	"priority": 2
}
```

### Response (200 OK)

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

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/languages/{id}</code> - Xóa ngôn ngữ</summary>

> Xóa một ngôn ngữ bằng ID.

### Path Parameters

| Tên Tham số | Loại | Mô tả                   |
| :---------- | :--- | :---------------------- |
| **id**      | Long | ID của ngôn ngữ cần xóa |

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

---
