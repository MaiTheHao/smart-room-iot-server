# Smart Room IoT - Room Event API Documentation

## 1. Cấu trúc dữ liệu RoomEventConfig

### RoomEventConfigDto
| Tên trường | Loại | Mô tả |
| :--- | :--- | :--- |
| `id` | Long | ID của cấu hình sự kiện phòng |
| `roomId` | Long | ID phòng sở hữu |
| `roomName` | String | Mã / Tên phòng |
| `roomEventId` | Long | ID định danh loại sự kiện phòng |
| `eventCode` | String (Enum) | Mã sự kiện phòng (`MOTION_DETECTED`) |
| `eventDescription` | String | Mô tả sự kiện |
| `isActive` | Boolean | Trạng thái bật/tắt kích hoạt (Mặc định: `true`) |
| `cooldownSeconds` | Integer | Thời gian chờ tối thiểu giữa 2 lần kích hoạt (giây, Mặc định: `0`) |
| `lastTriggeredAt` | String (ISO-8601) | Thời điểm kích hoạt thành công gần nhất |
| `createdAt` | String (ISO-8601) | Thời điểm tạo |
| `updatedAt` | String (ISO-8601) | Thời điểm cập nhật cuối |

---

## 2. Danh sách Endpoints

### 2.1. Quản lý Cấu hình Sự kiện (CRUD Config)

<details>
<summary><b>POST</b> <code>/api/v1/rooms/{roomId}/events</code> - Tạo cấu hình sự kiện mới cho phòng</summary>

> Khởi tạo cấu hình sự kiện mới cho một phòng. Mỗi phòng chỉ được có tối đa 1 cấu hình cho cùng một `eventCode`.

#### Path Parameters
| Tên | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `roomId` | Long | Có | ID của phòng |

#### Request Body
| Tên trường | Loại | Bắt buộc | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `eventCode` | String (Enum) | Có | - | Mã sự kiện phòng (`MOTION_DETECTED`) |
| `isActive` | Boolean | Không | `true` | Trạng thái kích hoạt |
| `cooldownSeconds` | Integer | Không | `0` | Thời gian cooldown (giây, >= 0) |

```json
{
  "eventCode": "MOTION_DETECTED",
  "isActive": true,
  "cooldownSeconds": 60
}
```

#### Response (201 Created)
```json
{
  "status": 201,
  "message": "Created successfully",
  "data": {
    "id": 1,
    "roomId": 10,
    "roomName": "P.101",
    "roomEventId": 1,
    "eventCode": "MOTION_DETECTED",
    "eventDescription": "Phát hiện chuyển động trong phòng",
    "isActive": true,
    "cooldownSeconds": 60,
    "lastTriggeredAt": null,
    "createdAt": "2026-08-29T02:00:00Z",
    "updatedAt": "2026-08-29T02:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/events</code> - Lấy tất cả cấu hình sự kiện của phòng</summary>

#### Path Parameters
| Tên | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `roomId` | Long | Có | ID của phòng |

#### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "roomId": 10,
      "roomName": "P.101",
      "roomEventId": 1,
      "eventCode": "MOTION_DETECTED",
      "eventDescription": "Phát hiện chuyển động trong phòng",
      "isActive": true,
      "cooldownSeconds": 60,
      "lastTriggeredAt": "2026-08-29T02:15:00Z",
      "createdAt": "2026-08-29T02:00:00Z",
      "updatedAt": "2026-08-29T02:15:00Z"
    }
  ]
}
```
</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/events/{configId}</code> - Chi tiết cấu hình sự kiện</summary>

#### Path Parameters
| Tên | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `roomId` | Long | Có | ID của phòng |
| `configId` | Long | Có | ID của cấu hình sự kiện |

#### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "roomId": 10,
    "roomName": "P.101",
    "roomEventId": 1,
    "eventCode": "MOTION_DETECTED",
    "eventDescription": "Phát hiện chuyển động trong phòng",
    "isActive": true,
    "cooldownSeconds": 60,
    "lastTriggeredAt": "2026-08-29T02:15:00Z",
    "createdAt": "2026-08-29T02:00:00Z",
    "updatedAt": "2026-08-29T02:15:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/rooms/{roomId}/events/{configId}</code> - Cập nhật cấu hình sự kiện</summary>

#### Path Parameters
| Tên | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `roomId` | Long | Có | ID của phòng |
| `configId` | Long | Có | ID của cấu hình sự kiện |

#### Request Body
| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `isActive` | Boolean | Không | Trạng thái kích hoạt |
| `cooldownSeconds` | Integer | Không | Thời gian cooldown (giây, >= 0) |

```json
{
  "isActive": true,
  "cooldownSeconds": 120
}
```

#### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "roomId": 10,
    "roomName": "P.101",
    "roomEventId": 1,
    "eventCode": "MOTION_DETECTED",
    "eventDescription": "Phát hiện chuyển động trong phòng",
    "isActive": true,
    "cooldownSeconds": 120,
    "lastTriggeredAt": "2026-08-29T02:15:00Z",
    "createdAt": "2026-08-29T02:00:00Z",
    "updatedAt": "2026-08-29T02:20:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/rooms/{roomId}/events/{configId}</code> - Xóa cấu hình sự kiện</summary>

> Xóa cấu hình sự kiện phòng, đồng thời tự động xóa toàn bộ các `Condition` và `Action` phụ thuộc.

#### Path Parameters
| Tên | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `roomId` | Long | Có | ID của phòng |
| `configId` | Long | Có | ID của cấu hình sự kiện |

#### Response (204 No Content)
```json
{
  "status": 204,
  "message": "Room event config deleted successfully",
  "data": null
}
```
</details>

---

### 2.2. Quản lý Điều kiện lọc (Conditions Sub-resource)

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/events/{configId}/conditions</code> - Danh sách điều kiện</summary>

#### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 101,
      "ownerCategory": "ROOM_EVENT",
      "ownerId": "1",
      "sourceCategory": "SENSOR",
      "sourceTargetId": "5",
      "sourceTargetType": "SENSOR_LUX",
      "property": "lux",
      "operator": "<",
      "value": "20",
      "extraParams": null,
      "sortOrder": 0,
      "nextLogic": "AND",
      "createdAt": "2026-08-29T02:00:00Z",
      "updatedAt": "2026-08-29T02:00:00Z"
    }
  ]
}
```
</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rooms/{roomId}/events/{configId}/conditions</code> - Thêm điều kiện mới</summary>

#### Request Body
```json
{
  "sourceCategory": "SENSOR",
  "sourceTargetId": "5",
  "sourceTargetType": "SENSOR_LUX",
  "property": "lux",
  "operator": "<",
  "value": "20",
  "sortOrder": 0,
  "nextLogic": "AND"
}
```

#### Response (201 Created)
```json
{
  "status": 201,
  "message": "Created successfully",
  "data": {
    "id": 101,
    "ownerCategory": "ROOM_EVENT",
    "ownerId": "1",
    "sourceCategory": "SENSOR",
    "sourceTargetId": "5",
    "sourceTargetType": "SENSOR_LUX",
    "property": "lux",
    "operator": "<",
    "value": "20",
    "extraParams": null,
    "sortOrder": 0,
    "nextLogic": "AND",
    "createdAt": "2026-08-29T02:00:00Z",
    "updatedAt": "2026-08-29T02:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/rooms/{roomId}/events/{configId}/conditions</code> - Thay thế toàn bộ danh sách điều kiện (Bulk Replace)</summary>

#### Request Body
```json
[
  {
    "id": 101,
    "sourceCategory": "SENSOR",
    "sourceTargetId": "5",
    "sourceTargetType": "SENSOR_LUX",
    "property": "lux",
    "operator": "<",
    "value": "30",
    "sortOrder": 0,
    "nextLogic": "AND"
  }
]
```

#### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 101,
      "ownerCategory": "ROOM_EVENT",
      "ownerId": "1",
      "sourceCategory": "SENSOR",
      "sourceTargetId": "5",
      "sourceTargetType": "SENSOR_LUX",
      "property": "lux",
      "operator": "<",
      "value": "30",
      "extraParams": null,
      "sortOrder": 0,
      "nextLogic": "AND",
      "createdAt": "2026-08-29T02:00:00Z",
      "updatedAt": "2026-08-29T02:25:00Z"
    }
  ]
}
```
</details>

---

### 2.3. Quản lý Hành động (Actions Sub-resource)

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/events/{configId}/actions</code> - Danh sách hành động</summary>

#### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 201,
      "ownerCategory": "ROOM_EVENT",
      "ownerId": "1",
      "targetCategory": "LIGHT",
      "targetId": "12",
      "params": {
        "state": true,
        "brightness": 80
      },
      "executionOrder": 0,
      "createdAt": "2026-08-29T02:00:00Z",
      "updatedAt": "2026-08-29T02:00:00Z"
    }
  ]
}
```
</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rooms/{roomId}/events/{configId}/actions</code> - Thêm hành động mới</summary>

#### Request Body
```json
{
  "targetCategory": "LIGHT",
  "targetId": "12",
  "params": {
    "state": true,
    "brightness": 80
  },
  "executionOrder": 0
}
```

#### Response (201 Created)
```json
{
  "status": 201,
  "message": "Created successfully",
  "data": {
    "id": 201,
    "ownerCategory": "ROOM_EVENT",
    "ownerId": "1",
    "targetCategory": "LIGHT",
    "targetId": "12",
    "params": {
      "state": true,
      "brightness": 80
    },
    "executionOrder": 0,
    "createdAt": "2026-08-29T02:00:00Z",
    "updatedAt": "2026-08-29T02:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/rooms/{roomId}/events/{configId}/actions</code> - Thay thế toàn bộ danh sách hành động (Bulk Replace)</summary>

#### Request Body
```json
[
  {
    "id": 201,
    "targetCategory": "LIGHT",
    "targetId": "12",
    "params": {
      "state": true,
      "brightness": 100
    },
    "executionOrder": 0
  }
]
```

#### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 201,
      "ownerCategory": "ROOM_EVENT",
      "ownerId": "1",
      "targetCategory": "LIGHT",
      "targetId": "12",
      "params": {
        "state": true,
        "brightness": 100
      },
      "executionOrder": 0,
      "createdAt": "2026-08-29T02:00:00Z",
      "updatedAt": "2026-08-29T02:30:00Z"
    }
  ]
}
```
</details>
