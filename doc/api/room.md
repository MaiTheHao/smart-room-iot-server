# Room Module

## Room API Documentation

<details>
<summary><b>GET</b> <code>/api/v1/floors/{floorId}/rooms</code> - Lấy danh sách phòng theo tầng</summary>

> Lấy danh sách phòng theo tầng, có phân trang.

### Path/Query Parameters (Nếu có)

| Tên     | Loại | Mô tả                  | Bắt buộc/Mặc định |
| :------ | :--- | :--------------------- | :---------------- |
| floorId | Long | ID của tầng            | Có                |
| page    | int  | Trang hiện tại         | 0                 |
| size    | int  | Số lượng phần tử/trang | 10                |

### Response (200 OK)

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

</details>

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}</code> - Lấy thông tin chi tiết phòng</summary>

> Lấy thông tin chi tiết của một phòng theo ID.

### Path/Query Parameters (Nếu có)

| Tên    | Loại | Mô tả        | Bắt buộc/Mặc định |
| :----- | :--- | :----------- | :---------------- |
| roomId | Long | ID của phòng | Có                |

### Response (200 OK)

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

</details>

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/v</code> - Lấy version của phòng</summary>

> Lấy version hiện tại của phòng theo ID (dùng cho optimistic locking hoặc sync).

### Path/Query Parameters (Nếu có)

| Tên    | Loại | Mô tả        | Bắt buộc/Mặc định |
| :----- | :--- | :----------- | :---------------- |
| roomId | Long | ID của phòng | Có                |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": 1,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<details>
<summary><b>POST</b> <code>/api/v1/floors/{floorId}/rooms</code> - Tạo mới phòng</summary>

> Tạo mới một phòng thuộc tầng chỉ định.

### Path/Query Parameters (Nếu có)

| Tên     | Loại | Mô tả       | Bắt buộc/Mặc định |
| :------ | :--- | :---------- | :---------------- |
| floorId | Long | ID của tầng | Có                |

### Request Body (Nếu có)

| Tên trường  | Loại   | Bắt buộc | Mô tả                               |
| :---------- | :----- | :------- | :---------------------------------- |
| name        | string | Có       | Tên phòng (1-100 ký tự, không rỗng) |
| description | string | Không    | Mô tả phòng (tối đa 255 ký tự)      |
| langCode    | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự)       |

### Request Example

```json
{
	"name": "Phòng họp",
	"description": "Phòng họp lớn",
	"langCode": "vi"
}
```

### Response (201 Created)

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

</details>

<details>
<summary><b>PUT</b> <code>/api/v1/rooms/{roomId}</code> - Cập nhật phòng</summary>

> Cập nhật thông tin phòng theo ID.

### Path/Query Parameters (Nếu có)

| Tên    | Loại | Mô tả        | Bắt buộc/Mặc định |
| :----- | :--- | :----------- | :---------------- |
| roomId | Long | ID của phòng | Có                |

### Request Body (Nếu có)

| Tên trường  | Loại   | Bắt buộc | Mô tả                          |
| :---------- | :----- | :------- | :----------------------------- |
| name        | string | Không    | Tên phòng (1-100 ký tự)        |
| description | string | Không    | Mô tả phòng (tối đa 255 ký tự) |
| floorId     | Long   | Không    | ID tầng mới (nếu chuyển phòng) |
| langCode    | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự)  |

### Request Example

```json
{
	"name": "Phòng họp VIP",
	"description": "Phòng họp dành cho lãnh đạo",
	"floorId": 3,
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
		"name": "Phòng họp VIP",
		"description": "Phòng họp dành cho lãnh đạo",
		"floorId": 3
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<details>
<summary><b>DELETE</b> <code>/api/v1/rooms/{roomId}</code> - Xóa phòng</summary>

> Xóa một phòng theo ID.

### Path/Query Parameters (Nếu có)

| Tên    | Loại | Mô tả        | Bắt buộc/Mặc định |
| :----- | :--- | :----------- | :---------------- |
| roomId | Long | ID của phòng | Có                |

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

