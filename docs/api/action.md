# Smart Room IoT - Action API Documentation

Tài nguyên `Action` đại diện cho các hành động thực thi điều khiển phần cứng hoặc gửi tín hiệu, được thiết kế đa hình (Polymorphic) và dùng chung cho các domain: `RULE`, `AUTOMATION`, `SYSTEM`, `ROOM_EVENT`,...

---

## 1. Cấu trúc Thuộc tính Action

| Tên trường | Loại | Bắt buộc (Create) | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | long | Không | ID của Action (chỉ dùng khi Bulk Replace: có `id` → update, không có → insert) |
| `ownerCategory` | string (Enum) | Có | Loại đối tượng sở hữu (`RULE`, `AUTOMATION`, `SYSTEM`, `ROOM_EVENT`) |
| `ownerId` | string | Có | ID của đối tượng sở hữu (Rule ID, Automation ID...) |
| `targetCategory` | string (Enum) | Có | Phân loại thiết bị đích (`LIGHT`, `FAN`, `AIR_CONDITION`) |
| `targetId` | string | Có | ID của thiết bị đích |
| `params` | Json | Có | Payload tham số điều khiển thiết bị (JSON) |
| `executionOrder`| integer | Không | Thứ tự thực hiện (Mặc định: `0`) |

> [!NOTE]
> Khi gọi qua sub-resource helper, các trường `ownerCategory`/`ownerId` trong body sẽ bị backend ghi đè (override) theo path parameter. Với Bulk Replace, body không cần truyền 2 trường này (backend tự gán).

### Cấu trúc `params` theo `targetCategory`
* **`LIGHT`**:
  * `power`: `"ON"`, `"OFF"`
  * `level`: `0` đến `100` (độ sáng)
* **`FAN`**:
  * `power`: `"ON"`, `"OFF"`
  * `speed`: `1` đến `3`
  * `mode`: `"COOL"`, `"HEAT"`, `"DRY"`, `"FAN"`, `"AUTO"`, `"NORMAL"`, `"SLEEP"`, `"NATURAL"`
  * `swing`: `"ON"`, `"OFF"`, `"AUTO"`, `"HORIZONTAL"`, `"VERTICAL"`
* **`AIR_CONDITION`**:
  * `power`: `"ON"`, `"OFF"`
  * `temperature`: `16` đến `32` (nhiệt độ thiết lập)
  * `mode`: `"COOL"`, `"HEAT"`, `"DRY"`, `"FAN"`, `"AUTO"`, `"NORMAL"`, `"SLEEP"`, `"NATURAL"`
  * `fanSpeed`: `0` đến `5`
  * `swing`: `"ON"`, `"OFF"`, `"AUTO"`, `"HORIZONTAL"`, `"VERTICAL"`

---

## 2. Danh sách Endpoints

<details>
<summary><b>POST</b> <code>/api/v1/actions</code> - Tạo mới Hành động</summary>

> Kiểm tra ràng buộc DTO (`@Valid`) trước khi lưu.

### Request Body
```json
{
  "ownerCategory": "RULE",
  "ownerId": "10",
  "targetCategory": "AIR_CONDITION",
  "targetId": "10",
  "params": {
    "power": "ON",
    "temperature": 24
  },
  "executionOrder": 0
}
```

### Response (201 Created)
```json
{
  "status": 201,
  "message": "Created successfully",
  "data": {
    "id": 200,
    "ownerCategory": "RULE",
    "ownerId": "10",
    "targetCategory": "AIR_CONDITION",
    "targetId": "10",
    "params": {
      "power": "ON",
      "temperature": 24
    },
    "executionOrder": 0,
    "createdAt": "2026-03-26T15:00:00Z",
    "updatedAt": "2026-03-26T15:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/actions/{id}</code> - Chi tiết Hành động</summary>

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 200,
    "ownerCategory": "RULE",
    "ownerId": "10",
    "targetCategory": "AIR_CONDITION",
    "targetId": "10",
    "params": {
      "power": "ON",
      "temperature": 24
    },
    "executionOrder": 0,
    "createdAt": "2026-03-26T15:00:00Z",
    "updatedAt": "2026-03-26T15:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/actions/{id}</code> - Xóa Hành động</summary>

### Response (204 No Content)
</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/actions</code> - Tra cứu Hành động theo Tiêu chí</summary>

### Query Parameters
* Lọc theo Owner: `?ownerCategory=RULE&ownerId=10`
* Lọc theo Target Device: `?targetCategory=AIR_CONDITION&targetId=10`

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 200,
      "ownerCategory": "RULE",
      "ownerId": "10",
      "targetCategory": "AIR_CONDITION",
      "targetId": "10",
      "params": {
        "power": "ON",
        "temperature": 24
      },
      "executionOrder": 0,
      "createdAt": "2026-03-26T15:00:00Z"
    }
  ]
}
```
</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/actions/by-owner</code> - Xóa tất cả Hành động theo Owner</summary>

### Query Parameters
* `ownerCategory` (Bắt buộc): `RULE`, `AUTOMATION`, `SYSTEM`, `ROOM_EVENT`
* `ownerId` (Bắt buộc): ID của đối tượng

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": 1
}
```
</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/actions/by-owner</code> - Thay thế toàn bộ Hành động theo Owner (Bulk Replace / Upsert)</summary>

### Query Parameters
* `ownerCategory` (Bắt buộc): `RULE`, `AUTOMATION`, `SYSTEM`, `ROOM_EVENT`
* `ownerId` (Bắt buộc): ID của đối tượng

> Upsert theo `id`: item có `id` → cập nhật; không có `id` → tạo mới.

### Request Body
```json
[
  {
    "id": 200,
    "targetCategory": "AIR_CONDITION",
    "targetId": "10",
    "params": {
      "power": "ON",
      "temperature": 26
    },
    "executionOrder": 0
  },
  {
    "targetCategory": "LIGHT",
    "targetId": "5",
    "params": {
      "power": "ON",
      "level": 70
    },
    "executionOrder": 1
  }
]
```

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 200,
      "ownerCategory": "RULE",
      "ownerId": "10",
      "targetCategory": "AIR_CONDITION",
      "targetId": "10",
      "params": {
        "power": "ON",
        "temperature": 26
      },
      "executionOrder": 0,
      "createdAt": "2026-03-26T15:00:00Z"
    },
    {
      "id": 204,
      "ownerCategory": "RULE",
      "ownerId": "10",
      "targetCategory": "LIGHT",
      "targetId": "5",
      "params": {
        "power": "ON",
        "level": 70
      },
      "executionOrder": 1,
      "createdAt": "2026-03-26T15:30:00Z"
    }
  ]
}
```
</details>
