# Smart Room IoT - Room Event API Documentation

---

## 1. Xác thực & Phân quyền (Authentication & Authorization)

- **Cơ chế xác thực:** JWT Bearer Token qua Header:
  ```http
  Authorization: Bearer <token>
  ```
- **Quyền hạn truy cập (Authorities):** Người dùng cần có ít nhất một trong hai quyền sau:
  - `F_MANAGE_ALL` (Quản trị toàn quyền hệ thống)
  - `F_MANAGE_ROOM` (Quản lý phòng)
- **Kiểm soát phạm vi phòng:** Đối với các API thuộc `/api/v1/rooms/{roomId}/...`, người dùng bắt buộc phải có quyền truy cập vào `roomId` được chỉ định (được kiểm tra qua `permissionService.requireAccessRoom(roomId)`).

---

## 2. Cấu trúc Response chuẩn (ApiResponse Envelope)

Tất cả các API đều bọc dữ liệu trong cấu trúc chuẩn `ApiResponse<T>`:

| Tên trường | Loại | Mô tả |
| :--- | :--- | :--- |
| `status` | Integer | HTTP Status code tương ứng |
| `message` | String | Thông điệp kết quả |
| `data` | `T` | Dữ liệu trả về (Object, List, hoặc `null`) |
| `timestamp` | String (ISO-8601) | Thời điểm xử lý phản hồi |
| `traceId` | String | Mã định danh truy vết log hệ thống (nếu có) |
| `scenarioId` | String | Mã ngữ cảnh kiểm thử/kịch bản (nếu có) |

---

## 3. Cấu trúc Dữ liệu

### 3.1. RoomEventCodeDto
| Tên trường | Loại | Mô tả |
| :--- | :--- | :--- |
| `id` | Long | ID của loại sự kiện |
| `code` | String (Enum) | Mã sự kiện phòng (Ví dụ: `MOTION_DETECTED`) |
| `description` | String | Mô tả loại sự kiện phòng |

### 3.2. RoomEventConfigDto
| Tên trường | Loại | Mô tả |
| :--- | :--- | :--- |
| `id` | Long | ID của cấu hình sự kiện phòng |
| `roomId` | Long | ID phòng sở hữu |
| `roomName` | String | Mã phòng sở hữu (`room.code`) |
| `roomEventId` | Long | ID định danh loại sự kiện phòng |
| `eventCode` | String (Enum) | Mã sự kiện phòng (`MOTION_DETECTED`) |
| `eventDescription` | String | Mô tả sự kiện |
| `isActive` | Boolean | Trạng thái bật/tắt kích hoạt (Mặc định: `true`) |
| `cooldownSeconds` | Integer | Thời gian chờ tối thiểu giữa 2 lần kích hoạt (giây, Mặc định: `0`) |
| `lastTriggeredAt` | String (ISO-8601) | Thời điểm kích hoạt thành công gần nhất |
| `createdAt` | String (ISO-8601) | Thời điểm tạo |
| `updatedAt` | String (ISO-8601) | Thời điểm cập nhật cuối |

### 3.3. ConditionDto
| Tên trường | Loại | Mô tả |
| :--- | :--- | :--- |
| `id` | Long | ID của điều kiện |
| `ownerCategory` | String (Enum) | Danh mục sở hữu (`ROOM_EVENT`) |
| `ownerId` | String | ID của thực thể sở hữu (ví dụ: `configId`) |
| `sourceCategory` | String (Enum) | Nguồn dữ liệu kiểm tra (`SENSOR`, `DEVICE`, ...) |
| `sourceTargetId` | String | Định danh nguồn dữ liệu |
| `sourceTargetType` | String (Enum) | Loại thiết bị / cảm biến (`SENSOR_LUX`, `SENSOR_PIR`, ...) |
| `property` | String | Thuộc tính cần so sánh (ví dụ: `lux`, `state`) |
| `operator` | String (Enum) | Toán tử (`<`, `<=`, `>`, `>=`, `=`, `!=`) |
| `value` | String | Giá trị ngưỡng so sánh |
| `extraParams` | Object (JSON) | Tham số bổ sung nếu có |
| `sortOrder` | Integer | Thứ tự đánh giá điều kiện (Mặc định: `0`) |
| `nextLogic` | String (Enum) | Toán tử logic kết nối với điều kiện kế tiếp (`AND`, `OR`) |
| `createdAt` | String (ISO-8601) | Thời điểm tạo |
| `updatedAt` | String (ISO-8601) | Thời điểm cập nhật |

### 3.4. ActionDto
| Tên trường | Loại | Mô tả |
| :--- | :--- | :--- |
| `id` | Long | ID của hành động |
| `ownerCategory` | String (Enum) | Danh mục sở hữu (`ROOM_EVENT`) |
| `ownerId` | String | ID của thực thể sở hữu (ví dụ: `configId`) |
| `targetCategory` | String (Enum) | Danh mục thiết bị đích (`LIGHT`, `AIR_CONDITION`, ...) |
| `targetId` | String | ID định danh thiết bị thực thi |
| `params` | Object (JSON) | Tham số điều khiển (ví dụ: `{"state": true, "brightness": 80}`) |
| `executionOrder` | Integer | Thứ tự thực thi hành động (Mặc định: `0`) |
| `createdAt` | String (ISO-8601) | Thời điểm tạo |
| `updatedAt` | String (ISO-8601) | Thời điểm cập nhật |

---

## 4. Danh sách Endpoints

### 4.1. Danh mục Mã Sự kiện Phòng (Master Data)

<details open>
<summary><b>GET</b> <code>/api/v1/room-events/codes</code> - Lấy danh sách các mã sự kiện phòng khả dụng</summary>

> Lấy toàn bộ danh sách các loại sự kiện phòng được cấu hình sẵn trong hệ thống.

#### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "code": "MOTION_DETECTED",
      "description": "Phát hiện chuyển động trong phòng"
    }
  ],
  "timestamp": "2026-09-12T07:00:00Z"
}
```
</details>

---

### 4.2. Quản lý Cấu hình Sự kiện (CRUD Config)

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
  },
  "timestamp": "2026-08-29T02:00:00Z"
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
  ],
  "timestamp": "2026-08-29T02:15:00Z"
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
  },
  "timestamp": "2026-08-29T02:15:00Z"
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
| Tên trường | Loại | Bắt buộc | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `isActive` | Boolean | Không | - | Trạng thái kích hoạt |
| `cooldownSeconds` | Integer | Không | - | Thời gian cooldown (giây, >= 0) |

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
  },
  "timestamp": "2026-08-29T02:20:00Z"
}
```
</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/rooms/{roomId}/events/{configId}</code> - Xóa cấu hình sự kiện</summary>

> Xóa cấu hình sự kiện phòng, đồng thời tự động xóa toàn bộ các `Condition` và `Action` phụ thuộc (`ConditionOwnerCategory.ROOM_EVENT` và `ActionOwnerCategory.ROOM_EVENT`).

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
  "data": null,
  "timestamp": "2026-08-29T02:22:00Z"
}
```
</details>

---

### 4.3. Quản lý Điều kiện lọc (Conditions Sub-resource)

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/events/{configId}/conditions</code> - Danh sách điều kiện</summary>

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
  ],
  "timestamp": "2026-08-29T02:00:00Z"
}
```
</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rooms/{roomId}/events/{configId}/conditions</code> - Thêm điều kiện mới</summary>

> **Lưu ý về validation (`CreateConditionDto`):** Mặc dù Controller sẽ tự động gán lại `ownerCategory = ROOM_EVENT` và `ownerId = configId`, tầng Validation của Spring Boot yêu cầu 2 trường này không được để trống (`@NotNull` và `@NotBlank`). Client nên truyền giá trị tương ứng (`ownerCategory: "ROOM_EVENT"` và `ownerId: "<configId>"`).

#### Path Parameters
| Tên | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `roomId` | Long | Có | ID của phòng |
| `configId` | Long | Có | ID của cấu hình sự kiện |

#### Request Body
| Tên trường | Loại | Bắt buộc | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `ownerCategory` | String (Enum) | Có | - | Cố định: `"ROOM_EVENT"` |
| `ownerId` | String | Có | - | ID của cấu hình sự kiện (`configId`) |
| `sourceCategory` | String (Enum) | Có | - | Nguồn dữ liệu: `SENSOR`, `DEVICE`, ... |
| `sourceTargetId` | String | Có | - | ID nguồn dữ liệu |
| `sourceTargetType` | String (Enum) | Không | - | Loại thiết bị / cảm biến |
| `property` | String | Có | - | Tên thuộc tính đo lường (`lux`, ...) |
| `operator` | String (Enum) | Có | - | Toán tử: `<`, `<=`, `>`, `>=`, `=`, `!=` |
| `value` | String | Có | - | Giá trị ngưỡng |
| `extraParams` | Object (JSON) | Không | `null` | Tham số bổ sung |
| `sortOrder` | Integer | Không | `0` | Thứ tự ưu tiên |
| `nextLogic` | String (Enum) | Không | `null` | Toán tử logic kế tiếp (`AND`, `OR`) |

```json
{
  "ownerCategory": "ROOM_EVENT",
  "ownerId": "1",
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
  },
  "timestamp": "2026-08-29T02:00:00Z"
}
```
</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/rooms/{roomId}/events/{configId}/conditions</code> - Thay thế toàn bộ danh sách điều kiện (Bulk Replace)</summary>

> Thay thế toàn bộ danh sách điều kiện hiện có của cấu hình bằng danh sách mới. Với API này (`ReplaceConditionDto`), client không cần truyền `ownerCategory` hay `ownerId`. Nếu phần tử có trường `id`, hệ thống sẽ cập nhật điều kiện tương ứng; nếu không có `id`, hệ thống sẽ tạo mới.

#### Path Parameters
| Tên | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `roomId` | Long | Có | ID của phòng |
| `configId` | Long | Có | ID của cấu hình sự kiện |

#### Request Body
Mảng danh sách các đối tượng điều kiện (`List<ReplaceConditionDto>`):

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | Long | Không | ID điều kiện hiện có (nếu muốn cập nhật) |
| `sourceCategory` | String (Enum) | Có | Nguồn dữ liệu (`SENSOR`, `DEVICE`, ...) |
| `sourceTargetId` | String | Có | ID nguồn dữ liệu |
| `sourceTargetType` | String (Enum) | Không | Loại thiết bị / cảm biến |
| `property` | String | Có | Thuộc tính so sánh |
| `operator` | String (Enum) | Có | Toán tử so sánh |
| `value` | String | Có | Giá trị ngưỡng |
| `extraParams` | Object (JSON) | Không | Tham số bổ sung |
| `sortOrder` | Integer | Không | Thứ tự ưu tiên |
| `nextLogic` | String (Enum) | Không | Toán tử logic kế tiếp (`AND`, `OR`) |

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
  ],
  "timestamp": "2026-08-29T02:25:00Z"
}
```
</details>

---

### 4.4. Quản lý Hành động (Actions Sub-resource)

<details>
<summary><b>GET</b> <code>/api/v1/rooms/{roomId}/events/{configId}/actions</code> - Danh sách hành động</summary>

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
  ],
  "timestamp": "2026-08-29T02:00:00Z"
}
```
</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rooms/{roomId}/events/{configId}/actions</code> - Thêm hành động mới</summary>

> **Lưu ý về validation (`CreateActionDto`):** Tương tự điều kiện, Controller yêu cầu DTO hợp lệ với `ownerCategory` (`@NotNull`) và `ownerId` (`@NotBlank`). Client cần truyền giá trị `ownerCategory: "ROOM_EVENT"` và `ownerId: "<configId>"` trong body để vượt qua bước xác thực dữ liệu đầu vào.

#### Path Parameters
| Tên | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `roomId` | Long | Có | ID của phòng |
| `configId` | Long | Có | ID của cấu hình sự kiện |

#### Request Body
| Tên trường | Loại | Bắt buộc | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| `ownerCategory` | String (Enum) | Có | - | Cố định: `"ROOM_EVENT"` |
| `ownerId` | String | Có | - | ID của cấu hình sự kiện (`configId`) |
| `targetCategory` | String (Enum) | Có | - | Danh mục thiết bị (`LIGHT`, ...) |
| `targetId` | String | Có | - | ID thiết bị đích |
| `params` | Object (JSON) | Có | - | Dữ liệu cấu hình thực thi (không được `null`) |
| `executionOrder` | Integer | Không | `0` | Thứ tự thực thi |

```json
{
  "ownerCategory": "ROOM_EVENT",
  "ownerId": "1",
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
  },
  "timestamp": "2026-08-29T02:00:00Z"
}
```
</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/rooms/{roomId}/events/{configId}/actions</code> - Thay thế toàn bộ danh sách hành động (Bulk Replace)</summary>

> Thay thế toàn bộ danh sách hành động hiện tại bằng danh sách mới. Với API này (`ReplaceActionDto`), client không cần gửi kèm `ownerCategory` hay `ownerId`. Nếu phần tử có `id`, hệ thống cập nhật hành động; nếu không có `id`, hệ thống tạo mới hành động.

#### Path Parameters
| Tên | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `roomId` | Long | Có | ID của phòng |
| `configId` | Long | Có | ID của cấu hình sự kiện |

#### Request Body
Mảng danh sách các đối tượng hành động (`List<ReplaceActionDto>`):

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | Long | Không | ID hành động hiện có (nếu cập nhật) |
| `targetCategory` | String (Enum) | Không | Danh mục thiết bị (`LIGHT`, ...) |
| `targetId` | String | Có | ID thiết bị đích |
| `params` | Object (JSON) | Có | Dữ liệu cấu hình thực thi (không được `null`) |
| `executionOrder` | Integer | Không | Thứ tự thực thi |

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
  ],
  "timestamp": "2026-08-29T02:30:00Z"
}
```
</details>
