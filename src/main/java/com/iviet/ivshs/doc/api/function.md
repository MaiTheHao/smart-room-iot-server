# Function Module

## API Documentation for Function Management (v1)

> Quản lý Functions (Quyền/Chức năng) trong hệ thống

---

## GET /api/v1/functions

> Lấy danh sách các Functions với phân trang.

### Query Parameters

| Tên  | Loại | Mô tả                         | Mặc định |
| :--- | :--- | :---------------------------- | :------- |
| page | int  | Trang hiện tại (bắt đầu từ 0) | 0        |
| size | int  | Số lượng phần tử/trang        | 10       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"functionCode": "VIEW_DASHBOARD",
				"name": "Xem dashboard",
				"description": "Quyền xem trang dashboard"
			},
			{
				"id": 2,
				"functionCode": "EDIT_USER",
				"name": "Chỉnh sửa người dùng",
				"description": "Quyền chỉnh sửa thông tin người dùng"
			}
		],
		"page": 0,
		"size": 10,
		"totalElements": 2,
		"totalPages": 1
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/functions/all

> Lấy tất cả Functions (không phân trang).

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"functionCode": "VIEW_DASHBOARD",
			"name": "Xem dashboard",
			"description": "Quyền xem trang dashboard"
		},
		{
			"id": 2,
			"functionCode": "EDIT_USER",
			"name": "Chỉnh sửa người dùng",
			"description": "Quyền chỉnh sửa thông tin người dùng"
		}
	],
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/functions/{functionId}

> Lấy thông tin chi tiết của một Function theo ID.

### Path Parameters

| Tên        | Loại | Mô tả                   | Bắt buộc |
| :--------- | :--- | :---------------------- | :------- |
| functionId | Long | ID của Function cần lấy | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"functionCode": "VIEW_DASHBOARD",
		"name": "Xem dashboard",
		"description": "Quyền xem trang dashboard"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

### Error Response (404 Not Found)

```json
{
	"status": 404,
	"message": "Function not found with ID: 999",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/functions/code/{functionCode}

> Lấy Function theo function code.

### Path Parameters

| Tên          | Loại   | Mô tả                     | Bắt buộc |
| :----------- | :----- | :------------------------ | :------- |
| functionCode | String | Code của Function cần lấy | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"functionCode": "VIEW_DASHBOARD",
		"name": "Xem dashboard",
		"description": "Quyền xem trang dashboard"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/functions/with-group-status/{groupId}

> Lấy tất cả Functions với trạng thái đã assigned vào Group hay chưa.
>
> **Use case:** Hiển thị checkbox list cho UI khi quản lý functions của group.

### Path Parameters

| Tên     | Loại | Mô tả                     | Bắt buộc |
| :------ | :--- | :------------------------ | :------- |
| groupId | Long | ID của Group cần kiểm tra | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"functionCode": "VIEW_DASHBOARD",
			"name": "Xem dashboard",
			"description": "Quyền xem trang dashboard",
			"isAssignedToGroup": true,
			"roleId": 10
		},
		{
			"id": 2,
			"functionCode": "EDIT_USER",
			"name": "Chỉnh sửa người dùng",
			"description": "Quyền chỉnh sửa thông tin người dùng",
			"isAssignedToGroup": false,
			"roleId": null
		}
	],
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## POST /api/v1/functions

> Tạo mới một Function.

### Request Body

| Tên trường   | Loại   | Bắt buộc | Mô tả                                      |
| :----------- | :----- | :------- | :----------------------------------------- |
| functionCode | string | Có       | Mã function (tối đa 256 ký tự, **UNIQUE**) |
| name         | string | Có       | Tên function (1-100 ký tự)                 |
| description  | string | Không    | Mô tả (tối đa 255 ký tự)                   |
| langCode     | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự)              |

### Request Example

```json
{
	"functionCode": "VIEW_REPORTS",
	"name": "Xem báo cáo",
	"description": "Quyền xem các báo cáo hệ thống",
	"langCode": "vi"
}
```

### Response (201 Created)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 3,
		"functionCode": "VIEW_REPORTS",
		"name": "Xem báo cáo",
		"description": "Quyền xem các báo cáo hệ thống"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

### Error Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Function code already exists: VIEW_REPORTS",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## PUT /api/v1/functions/{functionId}

> Update Function (chỉ update translation: name, description).
>
> **Lưu ý:** `functionCode` KHÔNG thể thay đổi.

### Path Parameters

| Tên        | Loại | Mô tả                      | Bắt buộc |
| :--------- | :--- | :------------------------- | :------- |
| functionId | Long | ID của Function cần update | Có       |

### Request Body

| Tên trường  | Loại   | Bắt buộc | Mô tả                          |
| :---------- | :----- | :------- | :----------------------------- |
| name        | string | Không    | Tên function mới (1-100 ký tự) |
| description | string | Không    | Mô tả mới (tối đa 255 ký tự)   |
| langCode    | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự)  |

### Request Example

```json
{
	"name": "Xem báo cáo chi tiết",
	"description": "Quyền xem tất cả báo cáo chi tiết trong hệ thống",
	"langCode": "vi"
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 3,
		"functionCode": "VIEW_REPORTS",
		"name": "Xem báo cáo chi tiết",
		"description": "Quyền xem tất cả báo cáo chi tiết trong hệ thống"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## DELETE /api/v1/functions/{functionId}

> Xóa Function (cascade delete tất cả roles liên quan).
>
> **Cảnh báo:** Tất cả roles mapping với function này sẽ bị xóa và cache sẽ được clear.

### Path Parameters

| Tên        | Loại | Mô tả                   | Bắt buộc |
| :--------- | :--- | :---------------------- | :------- |
| functionId | Long | ID của Function cần xóa | Có       |

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "Function deleted successfully",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/functions/count

> Đếm tổng số Functions trong hệ thống.

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": 25,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│                   Function Management                    │
└─────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────┐
              │  SysFunctionControllerV1  │
              └───────────────┬───────────┘
                              │
                              ▼
              ┌───────────────────────────┐
              │  SysFunctionServiceImplV1 │
              └───────────────┬───────────┘
                              │
                              ▼
              ┌───────────────────────────┐
              │     SysFunctionDaoV1      │
              └───────────────┬───────────┘
                              │
                              ▼
              ┌───────────────────────────┐
              │    JPA EntityManager      │
              │    + JPQL Queries         │
              └───────────────┬───────────┘
                              │
                              ▼
              ┌───────────────────────────┐
              │  sys_function_v1 (Table)  │
              │  sys_function_lan_v1      │
              └───────────────────────────┘
```

---

## Business Rules

1. **Unique Function Code**: `functionCode` phải unique trong toàn hệ thống
2. **Immutable Code**: Sau khi tạo, `functionCode` không thể thay đổi
3. **Multi-language Support**: Name và description support nhiều ngôn ngữ
4. **Cascade Delete**: Xóa function sẽ xóa tất cả roles liên quan
5. **Cache Management**: Mọi thay đổi đều trigger cache clear/rebuild
