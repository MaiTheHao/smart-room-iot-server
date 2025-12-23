# Room Module

## Room API Documentation

---

### GET /api/v1/floors/{floorId}/rooms

> Lấy danh sách phòng theo tầng, có phân trang.

#### Tham số Đường dẫn (Path Parameters)

| Tên     | Loại | Mô tả       | Bắt buộc |
| :------ | :--- | :---------- | :------- |
| floorId | Long | ID của tầng | Có       |

#### Tham số Truy vấn (Query Parameters)

| Tên  | Loại | Mô tả                  | Mặc định |
| :--- | :--- | :--------------------- | :------- |
| page | int  | Trang hiện tại         | 0        |
| size | int  | Số lượng phần tử/trang | 10       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"name": "Phòng họp",
				"description": "Phòng họp lớn",
				"floorId": 2
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

### GET /api/v1/rooms/{roomId}

> Lấy thông tin chi tiết của một phòng theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên    | Loại | Mô tả        | Bắt buộc |
| :----- | :--- | :----------- | :------- |
| roomId | Long | ID của phòng | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Phòng họp",
		"description": "Phòng họp lớn",
		"floorId": 2
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/floors/{floorId}/rooms

> Tạo mới một phòng thuộc tầng chỉ định.

#### Tham số Đường dẫn (Path Parameters)

| Tên     | Loại | Mô tả       | Bắt buộc |
| :------ | :--- | :---------- | :------- |
| floorId | Long | ID của tầng | Có       |

#### Request Body Fields

| Tên trường  | Loại   | Bắt buộc | Mô tả                               |
| :---------- | :----- | :------- | :---------------------------------- |
| name        | string | Có       | Tên phòng (1-100 ký tự, không rỗng) |
| description | string | Không    | Mô tả phòng (tối đa 255 ký tự)      |
| langCode    | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự)       |

#### Ví dụ Request Body

```json
{
	"name": "Phòng họp",
	"description": "Phòng họp lớn",
	"langCode": "vi"
}
```

#### Ví dụ Response (201 Created)

```json
{
	"status": 201,
	"message": "Created successfully",
	"data": {
		"id": 2,
		"name": "Phòng họp",
		"description": "Phòng họp lớn",
		"floorId": 2
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### PUT /api/v1/rooms/{roomId}

> Cập nhật thông tin phòng theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên    | Loại | Mô tả        | Bắt buộc |
| :----- | :--- | :----------- | :------- |
| roomId | Long | ID của phòng | Có       |

#### Request Body Fields

| Tên trường  | Loại   | Bắt buộc | Mô tả                          |
| :---------- | :----- | :------- | :----------------------------- |
| name        | string | Không    | Tên phòng (1-100 ký tự)        |
| description | string | Không    | Mô tả phòng (tối đa 255 ký tự) |
| floorId     | Long   | Không    | ID tầng mới (nếu chuyển phòng) |
| langCode    | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự)  |

#### Ví dụ Request Body

```json
{
	"name": "Phòng họp VIP",
	"description": "Phòng họp dành cho lãnh đạo",
	"floorId": 3,
	"langCode": "vi"
}
```

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Phòng họp VIP",
		"description": "Phòng họp dành cho lãnh đạo",
		"floorId": 3
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### DELETE /api/v1/rooms/{roomId}

> Xóa một phòng theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên    | Loại | Mô tả        | Bắt buộc |
| :----- | :--- | :----------- | :------- |
| roomId | Long | ID của phòng | Có       |

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
