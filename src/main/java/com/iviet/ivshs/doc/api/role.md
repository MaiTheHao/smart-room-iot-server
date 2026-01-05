# Role Module

## API Documentation for Role Management (v1)

> Quản lý Roles (Mapping giữa Group và Function) - Hệ thống phân quyền

---

## POST /api/v1/roles/groups/functions/batch-add

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

---

## POST /api/v1/roles/groups/functions/batch-remove

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

---

## POST /api/v1/roles/groups/functions/toggle

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

---

## POST /api/v1/roles/groups/{groupId}/functions/{functionCode}

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

### Error Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Function already exists in group",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## DELETE /api/v1/roles/groups/{groupId}/functions/{functionCode}

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

---

## POST /api/v1/roles/clients/groups/assign

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

---

## DELETE /api/v1/roles/clients/{clientId}/groups/{groupId}

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

### Error Response (404 Not Found)

```json
{
	"status": 404,
	"message": "Client does not have this group",
	"data": null,
	"timestamp": "2026-01-05T10:00:00Z"
}
```

---

## GET /api/v1/roles/groups/{groupId}/functions/{functionCode}/check

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

---

## Complete UI Workflow Example

### Scenario: Quản lý Functions của Group (Roles Page)

#### Step 1: Load Groups

```http
GET /api/v1/groups/all
```

#### Step 2: User chọn Group (ID = 1)

#### Step 3: Load tất cả Functions với status

```http
GET /api/v1/functions/with-group-status/1
```

**Response:**

```json
{
	"data": [
		{
			"id": 1,
			"functionCode": "VIEW_DASHBOARD",
			"isAssignedToGroup": true // ← Checked
		},
		{
			"id": 2,
			"functionCode": "EDIT_USER",
			"isAssignedToGroup": false // ← Unchecked
		}
	]
}
```

#### Step 4: User toggle checkboxes và Click Save

**UI State:**

-   `VIEW_DASHBOARD`: ✅ Checked (was checked)
-   `EDIT_USER`: ✅ Checked (was unchecked) → **ADD**
-   `DELETE_USER`: ❌ Unchecked (was checked) → **REMOVE**

**Request:**

```http
POST /api/v1/roles/groups/functions/toggle

{
  "groupId": 1,
  "functionToggles": {
    "VIEW_DASHBOARD": true,
    "EDIT_USER": true,
    "DELETE_USER": false
  }
}
```

**Response:**

```json
{
	"data": {
		"successCount": 2,
		"message": "Added 1, removed 1, skipped 1 function(s)"
	}
}
```

#### Step 5: Show toast notification & Reload data (optional)

---

## Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Role Management                       │
│              (Group ↔ Function Mapping)                  │
└─────────────────────────────────────────────────────────┘
                              │
                              ▼
              ┌───────────────────────────┐
              │    SysRoleControllerV1    │
              └───────────────┬───────────┘
                              │
                              ▼
              ┌───────────────────────────┐
              │   SysRoleServiceImplV1    │
              │   (Batch Operations)      │
              └───────────────┬───────────┘
                              │
                 ┌────────────┴────────────┐
                 ▼                         ▼
     ┌───────────────────┐    ┌───────────────────┐
     │   SysRoleDaoV1    │    │ CacheServiceV1    │
     │   (CRUD Roles)    │    │ (Sync Cache)      │
     └─────────┬─────────┘    └───────────────────┘
               │
               ▼
     ┌───────────────────┐
     │ sys_role_v1       │
     │ (Table)           │
     │                   │
     │ Composite Key:    │
     │ - group_id        │
     │ - function_id     │
     │ - is_active       │
     └───────────────────┘
```

---

## Performance Optimizations

### 1. Batch Operations

-   ✅ **Batch Add/Remove**: N operations → 1 cache rebuild
-   ✅ **Toggle API**: Recommended cho UI với checkboxes
-   ✅ **Atomic Updates**: Tất cả DB operations xong → Rebuild cache 1 lần

### 2. Cache Strategy

```java
// ❌ ANTI-PATTERN (N+1 Cache Rebuild)
for (function : functions) {
    roleDao.save(role);
    cacheService.rebuild(); // N times!
}

// ✅ OPTIMIZED PATTERN
boolean hasChanges = false;
for (function : functions) {
    roleDao.save(role);
    hasChanges = true;
}
if (hasChanges) {
    cacheService.rebuild(); // 1 time!
}
```

### 3. Transaction Safety

-   ✅ All operations wrapped in `@Transactional`
-   ✅ Cache update AFTER successful DB commit
-   ✅ Rollback safety: Cache không bị dirty

---

## Business Rules

1. **Many-to-Many Relationship**: Group ↔ Function (through sys_role_v1)
2. **Active Status**: Roles có `isActive` flag (hiện tại chỉ dùng true)
3. **Unique Constraint**: Một Group chỉ có 1 mapping với 1 Function
4. **Cache Consistency**: Mọi thay đổi trigger cache rebuild
5. **Batch Performance**: Tối ưu cho operations với nhiều items
6. **Client Permissions**: Quyền của client = Union của tất cả functions trong các groups
