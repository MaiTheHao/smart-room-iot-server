# Group Module

## API Documentation for Group Management (v1)

> Quản lý Groups (Nhóm người dùng) trong hệ thống

---

## GET /api/v1/groups

> Lấy danh sách các Groups với phân trang.

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
				"groupCode": "ADMIN",
				"name": "Quản trị viên",
				"description": "Nhóm quản trị hệ thống"
			},
			{
				"id": 2,
				"groupCode": "USER",
				"name": "Người dùng",
				"description": "Nhóm người dùng thông thường"
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

## GET /api/v1/groups/all

> Lấy tất cả Groups (không phân trang).

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"groupCode": "ADMIN",
			"name": "Quản trị viên",
			"description": "Nhóm quản trị hệ thống"
		},
		{
			"id": 2,
			"groupCode": "USER",
			"name": "Người dùng",
			"description": "Nhóm người dùng thông thường"
		}
	],
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/groups/{groupId}

> Lấy thông tin chi tiết của một Group theo ID.

### Path Parameters

| Tên     | Loại | Mô tả                | Bắt buộc |
| :------ | :--- | :------------------- | :------- |
| groupId | Long | ID của Group cần lấy | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"groupCode": "ADMIN",
		"name": "Quản trị viên",
		"description": "Nhóm quản trị hệ thống"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/groups/code/{groupCode}

> Lấy Group theo group code.

### Path Parameters

| Tên       | Loại   | Mô tả                  | Bắt buộc |
| :-------- | :----- | :--------------------- | :------- |
| groupCode | String | Code của Group cần lấy | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"groupCode": "ADMIN",
		"name": "Quản trị viên",
		"description": "Nhóm quản trị hệ thống"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## POST /api/v1/groups

> Tạo mới một Group.

### Request Body

| Tên trường  | Loại   | Bắt buộc | Mô tả                                   |
| :---------- | :----- | :------- | :-------------------------------------- |
| groupCode   | string | Có       | Mã group (tối đa 100 ký tự, **UNIQUE**) |
| name        | string | Có       | Tên group (1-100 ký tự)                 |
| description | string | Không    | Mô tả (tối đa 255 ký tự)                |
| langCode    | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự)           |

### Request Example

```json
{
	"groupCode": "MANAGER",
	"name": "Quản lý",
	"description": "Nhóm quản lý cấp trung",
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
		"groupCode": "MANAGER",
		"name": "Quản lý",
		"description": "Nhóm quản lý cấp trung"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## PUT /api/v1/groups/{groupId}

> Update Group (chỉ update translation: name, description).
>
> **Lưu ý:** `groupCode` KHÔNG thể thay đổi.

### Path Parameters

| Tên     | Loại | Mô tả                   | Bắt buộc |
| :------ | :--- | :---------------------- | :------- |
| groupId | Long | ID của Group cần update | Có       |

### Request Body

| Tên trường  | Loại   | Bắt buộc | Mô tả                         |
| :---------- | :----- | :------- | :---------------------------- |
| name        | string | Không    | Tên group mới (1-100 ký tự)   |
| description | string | Không    | Mô tả mới (tối đa 255 ký tự)  |
| langCode    | string | Không    | Mã ngôn ngữ (tối đa 10 ký tự) |

### Request Example

```json
{
	"name": "Quản lý cấp cao",
	"description": "Nhóm quản lý cấp cao trong công ty",
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
		"groupCode": "MANAGER",
		"name": "Quản lý cấp cao",
		"description": "Nhóm quản lý cấp cao trong công ty"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## DELETE /api/v1/groups/{groupId}

> Xóa Group.
>
> **Lưu ý:** Không thể xóa group nếu còn clients thuộc group này.

### Path Parameters

| Tên     | Loại | Mô tả                | Bắt buộc |
| :------ | :--- | :------------------- | :------- |
| groupId | Long | ID của Group cần xóa | Có       |

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "Group deleted successfully",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

### Error Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Cannot delete group. It has 5 client(s). Please remove all clients from this group first.",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/groups/{groupId}/functions

> Lấy danh sách Functions của một Group.

### Path Parameters

| Tên     | Loại | Mô tả        | Bắt buộc |
| :------ | :--- | :----------- | :------- |
| groupId | Long | ID của Group | Có       |

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

## GET /api/v1/groups/{groupId}/clients

> Lấy danh sách Clients (Users) thuộc một Group.

### Path Parameters

| Tên     | Loại | Mô tả        | Bắt buộc |
| :------ | :--- | :----------- | :------- |
| groupId | Long | ID của Group | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"username": "admin",
			"clientType": "USER",
			"ipAddress": "192.168.1.100",
			"macAddress": "00:11:22:33:44:55",
			"avatarUrl": "/images/avatar1.jpg",
			"lastLoginAt": "2026-01-05T09:30:00Z"
		}
	],
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/groups/count

> Đếm tổng số Groups.

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": 5,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/groups/{groupId}/functions/count

> Đếm số Functions của một Group.

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": 15,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/groups/{groupId}/clients/count

> Đếm số Clients của một Group.

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": 8,
	"timestamp": "2026-01-05T10:00:00Z"
}
```
