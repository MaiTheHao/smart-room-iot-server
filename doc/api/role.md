# Role Module

## API Documentation for Role Management (v1)

> Quản lý Roles (Mapping giữa Group và Function) - Hệ thống phân quyền

---

<details>
<summary><b>POST</b> <code>/api/v1/roles/groups/functions/batch-add</code> - Thêm nhiều Functions</summary>

> Batch thêm nhiều Functions vào một Group.
>
> **Use case:** Admin chọn nhiều functions trong UI và assign cho group.

### Request Body

| Tên trường    | Loại          | Bắt buộc | Mô tả                    |
| :------------ | :------------ | :------- | :----------------------- |
| groupId       | Long          | Có       | ID của Group             |
| functionCodes | Array[String] | Có       | Danh sách function codes |

### Request Example

```json
{
	"groupId": 1,
	"functionCodes": ["VIEW_DASHBOARD", "VIEW_REPORTS", "EDIT_USER"]
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"successCount": 2,
		"skippedCount": 1,
		"failedCount": 0,
		"message": "Added 2 function(s), skipped 1 (already exists)"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/roles/groups/functions/batch-remove</code> - Xóa nhiều Functions</summary>

> Batch xóa nhiều Functions khỏi một Group.
>
> **Use case:** Admin bỏ chọn nhiều functions trong UI.

### Request Body

| Tên trường    | Loại          | Bắt buộc | Mô tả                    |
| :------------ | :------------ | :------- | :----------------------- |
| groupId       | Long          | Có       | ID của Group             |
| functionCodes | Array[String] | Có       | Danh sách function codes |

### Request Example

```json
{
	"groupId": 1,
	"functionCodes": ["DELETE_USER", "DELETE_SYSTEM"]
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"successCount": 2,
		"skippedCount": 0,
		"failedCount": 0,
		"message": "Removed 2 function(s), skipped 0 (not found)"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/roles/groups/functions/toggle</code> - Đảo trạng thái Functions</summary>

> Toggle Functions cho Group (Add/Remove dựa vào Map).
>
> **Use case:** UI với checkbox list, user check/uncheck và click Save.
>
> **Recommend:** Dùng API này thay vì gọi batch-add và batch-remove riêng biệt.

### Request Body

| Tên trường      | Loại                 | Bắt buộc | Mô tả                                           |
| :-------------- | :------------------- | :------- | :---------------------------------------------- |
| groupId         | Long                 | Có       | ID của Group                                    |
| functionToggles | Map<String, Boolean> | Có       | Map: functionCode → true (add) / false (remove) |

### Request Example

```json
{
	"groupId": 1,
	"functionToggles": {
		"VIEW_DASHBOARD": true,
		"VIEW_REPORTS": true,
		"EDIT_USER": true,
		"DELETE_USER": false,
		"DELETE_SYSTEM": false
	}
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"successCount": 5,
		"skippedCount": 0,
		"failedCount": 0,
		"message": "Added 3, removed 2, skipped 0 function(s)"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/roles/groups/{groupId}/functions/{functionCode}</code> - Thêm một Function</summary>

> Thêm một Function vào Group.

### Path Parameters

| Tên          | Loại   | Mô tả             | Bắt buộc |
| :----------- | :----- | :---------------- | :------- |
| groupId      | Long   | ID của Group      | Có       |
| functionCode | String | Code của Function | Có       |

### Response (201 Created)

```json
{
	"status": 201,
	"message": "Function added to group successfully",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

### Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Function already exists in group",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/roles/groups/{groupId}/functions/{functionCode}</code> - Xóa một Function</summary>

> Xóa một Function khỏi Group.

### Path Parameters

| Tên          | Loại   | Mô tả             | Bắt buộc |
| :----------- | :----- | :---------------- | :------- |
| groupId      | Long   | ID của Group      | Có       |
| functionCode | String | Code của Function | Có       |

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "Function removed from group successfully",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/roles/clients/groups/assign</code> - Gán nhiều Groups</summary>

> Assign nhiều Groups cho một Client (User).
>
> **Use case:** Admin chọn user và assign nhiều groups cho user đó.

### Request Body

| Tên trường | Loại        | Bắt buộc | Mô tả               |
| :--------- | :---------- | :------- | :------------------ |
| clientId   | Long        | Có       | ID của Client       |
| groupIds   | Array[Long] | Có       | Danh sách Group IDs |

### Request Example

```json
{
	"clientId": 5,
	"groupIds": [1, 2, 3]
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"successCount": 2,
		"skippedCount": 1,
		"failedCount": 0,
		"message": "Assigned 2 group(s), skipped 1 (already assigned)"
	},
	"timestamp": "2026-01-05T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/roles/clients/groups/unassign</code> - Hủy gán nhiều Groups</summary>

> Unassign nhiều Groups khỏi một Client.
>
> **Use case:** Admin bỏ nhiều groups khỏi user.

### Request Body

| Tên trường | Loại        | Bắt buộc | Mô tả               |
| :--------- | :---------- | :------- | :------------------ |
| clientId   | Long        | Có       | ID của Client       |
| groupIds   | Array[Long] | Có       | Danh sách Group IDs |

### Request Example

```json
{
	"clientId": 5,
	"groupIds": [1, 2]
}
```

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "Groups unassigned from client successfully",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/roles/clients/{clientId}/groups/{groupId}</code> - Hủy gán một Group</summary>

> Unassign một Group khỏi Client.
>
> **Use case:** Admin remove user khỏi một group cụ thể.

### Path Parameters

| Tên      | Loại | Mô tả         | Bắt buộc |
| :------- | :--- | :------------ | :------- |
| clientId | Long | ID của Client | Có       |
| groupId  | Long | ID của Group  | Có       |

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "Group unassigned from client successfully",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

### Response (404 Not Found)

```json
{
	"status": 404,
	"message": "Client does not have this group",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/roles/groups/{groupId}/functions/{functionCode}/check</code> - Kiểm tra Function Group</summary>

> Kiểm tra xem Group có Function hay không.
>
> **Use case:** Validate quyền trước khi thực hiện hành động.

### Path Parameters

| Tên          | Loại   | Mô tả             | Bắt buộc |
| :----------- | :----- | :---------------- | :------- |
| groupId      | Long   | ID của Group      | Có       |
| functionCode | String | Code của Function | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": true,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

</details>

<br>

---
