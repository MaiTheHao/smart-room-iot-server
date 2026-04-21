# Floor Module

## API Documentation for Floor Management (v1)

<details>
<summary><b>GET</b> <code>/api/v1/floors</code> - Lấy danh sách các tầng (phân trang)</summary>

> Lấy danh sách các tầng (phân trang).

### Query Parameters

| Tên  | Loại | Mô tả                         | Bắt buộc/Mặc định |
| :--- | :--- | :---------------------------- | :---------------- |
| page | int  | Trang hiện tại (bắt đầu từ 0) | Mặc định: 0       |
| size | int  | Số lượng phần tử/trang        | Mặc định: 10      |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"name": "Tầng 1",
				"description": "Khu vực lễ tân",
				"level": 1
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
<summary><b>GET</b> <code>/api/v1/floors/{floorId}</code> - Lấy thông tin chi tiết của một tầng</summary>

> Lấy thông tin chi tiết của một tầng theo ID.

### Path Parameters

| Tên     | Loại | Mô tả               | Bắt buộc/Mặc định |
| :------ | :--- | :------------------ | :---------------- |
| floorId | Long | ID của tầng cần lấy | Có                |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Tầng 1",
		"description": "Khu vực lễ tân",
		"level": 1
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<details>
<summary><b>GET</b> <code>/api/v1/floors/{floorId}/v</code> - Lấy version của tầng</summary>

> Lấy version hiện tại của tầng theo ID (dùng cho optimistic locking hoặc sync).

### Path Parameters

| Tên     | Loại | Mô tả               | Bắt buộc/Mặc định |
| :------ | :--- | :------------------ | :---------------- |
| floorId | Long | ID của tầng cần lấy | Có                |

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
<summary><b>POST</b> <code>/api/v1/floors</code> - Tạo mới một tầng</summary>

> Tạo mới một tầng trong hệ thống.

### Request Body

| Tên trường  | Loại   | Bắt buộc | Mô tả                         |
| :---------- | :----- | :------- | :---------------------------- |
| name        | string | Có       | Tên tầng (1-100 ký tự)        |
| description | string | Không    | Mô tả tầng (tối đa 255 ký tự) |
| level       | int    | Có       | Số thứ tự tầng                |
| langCode    | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự) |

### Request Example

```json
{
	"name": "Tầng 2",
	"description": "Khu vực phòng họp",
	"level": 2,
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
		"name": "Tầng 2",
		"description": "Khu vực phòng họp",
		"level": 2
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<details>
<summary><b>PUT</b> <code>/api/v1/floors/{floorId}</code> - Cập nhật thông tin tầng</summary>

> Cập nhật thông tin tầng hiện có theo ID.

### Path Parameters

| Tên     | Loại | Mô tả                | Bắt buộc/Mặc định |
| :------ | :--- | :------------------- | :---------------- |
| floorId | Long | ID tầng cần cập nhật | Có                |

### Request Body

| Tên trường  | Loại   | Bắt buộc | Mô tả                         |
| :---------- | :----- | :------- | :---------------------------- |
| name        | string | Không    | Tên tầng (1-100 ký tự)        |
| description | string | Không    | Mô tả tầng (tối đa 255 ký tự) |
| level       | int    | Không    | Số thứ tự tầng                |
| langCode    | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự) |

### Request Example

```json
{
	"name": "Tầng 2 - Đã sửa",
	"description": "Phòng họp lớn",
	"level": 2,
	"langCode": "vi"
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 2,
		"name": "Tầng 2 - Đã sửa",
		"description": "Phòng họp lớn",
		"level": 2
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<details>
<summary><b>DELETE</b> <code>/api/v1/floors/{floorId}</code> - Xóa tầng theo ID</summary>

> Xóa bỏ một tầng khỏi hệ thống.

### Path Parameters

| Tên     | Loại | Mô tả           | Bắt buộc/Mặc định |
| :------ | :--- | :-------------- | :---------------- |
| floorId | Long | ID tầng cần xóa | Có                |

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
