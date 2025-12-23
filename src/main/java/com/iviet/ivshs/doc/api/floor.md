# Floor Module

## API Documentation for Floor Management (v1)

---

## GET /api/v1/floors

> Lấy danh sách các tầng (phân trang).

#### Tham số Truy vấn (Query Parameters)

| Tên  | Loại | Mô tả                         | Mặc định |
| :--- | :--- | :---------------------------- | :------- |
| page | int  | Trang hiện tại (bắt đầu từ 0) | 0        |
| size | int  | Số lượng phần tử/trang        | 10       |

#### Ví dụ Response (200 OK)

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

---

## GET /api/v1/floors/{floorId}

> Lấy thông tin chi tiết của một tầng theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên     | Loại | Mô tả               | Bắt buộc |
| :------ | :--- | :------------------ | :------- |
| floorId | Long | ID của tầng cần lấy | Có       |

#### Ví dụ Response (200 OK)

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

---

## POST /api/v1/floors

> Tạo mới một tầng.

#### Request Body Fields

| Tên trường  | Loại   | Bắt buộc | Mô tả                         |
| :---------- | :----- | :------- | :---------------------------- |
| name        | string | Có       | Tên tầng (1-100 ký tự)        |
| description | string | Không    | Mô tả tầng (tối đa 255 ký tự) |
| level       | int    | Có       | Số thứ tự tầng                |
| langCode    | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự) |

#### Ví dụ Request Body

```json
{
	"name": "Tầng 2",
	"description": "Khu vực phòng họp",
	"level": 2,
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
		"name": "Tầng 2",
		"description": "Khu vực phòng họp",
		"level": 2
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## PUT /api/v1/floors/{floorId}

> Cập nhật thông tin tầng theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên     | Loại | Mô tả                | Bắt buộc |
| :------ | :--- | :------------------- | :------- |
| floorId | Long | ID tầng cần cập nhật | Có       |

#### Request Body Fields

| Tên trường  | Loại   | Bắt buộc | Mô tả                         |
| :---------- | :----- | :------- | :---------------------------- |
| name        | string | Không    | Tên tầng (1-100 ký tự)        |
| description | string | Không    | Mô tả tầng (tối đa 255 ký tự) |
| level       | int    | Không    | Số thứ tự tầng                |
| langCode    | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự) |

#### Ví dụ Request Body

```json
{
	"name": "Tầng 2 - Đã sửa",
	"description": "Phòng họp lớn",
	"level": 2,
	"langCode": "vi"
}
```

#### Ví dụ Response (200 OK)

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

---

## DELETE /api/v1/floors/{floorId}

> Xóa tầng theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên     | Loại | Mô tả           | Bắt buộc |
| :------ | :--- | :-------------- | :------- |
| floorId | Long | ID tầng cần xóa | Có       |

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
