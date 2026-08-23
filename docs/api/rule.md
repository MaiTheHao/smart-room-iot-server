# Smart Room IoT - Rule API Documentation

Hệ thống tự động hóa dựa trên chu kỳ giây độc lập (`Rule`). Bản ghi `Rule` được thiết kế độc lập (decoupled), quản lý metadata của quy tắc và chu kỳ kích hoạt của Quartz Scheduler.

> [!NOTE]
> Các điều kiện (`Condition`) và hành động (`Action`) được quản lý qua các endpoints chuyên biệt hoặc qua các Helper Sub-Resource endpoints (`/api/v1/rules/{id}/conditions`, `/api/v1/rules/{id}/actions`). Khi xóa `Rule`, toàn bộ Conditions và Actions liên quan sẽ tự động được xóa sạch (Cascade Delete).

---

<details>
<summary><b>POST</b> <code>/api/v1/rules</code> - Tạo mới quy tắc</summary>

> Khởi tạo Rule mới với chu kỳ chạy riêng.

### Request Body
| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| name | string | Có | Tên duy nhất, không rỗng |
| priority | integer | Có | Độ ưu tiên (>= 0) |
| intervalSeconds | integer | Có | Chu kỳ lặp lại (Min: 60) |
| conditions | array | Có | Danh sách điều kiện |
| actions | array | Có | Danh sách hành động |

#### Cấu trúc Condition
| Tên trường | Loại | Mô tả |
| :--- | :--- | :--- |
| sortOrder | int | Thứ tự đánh giá (0, 1, 2...) |
| dataSource | string | `SYSTEM`, `ROOM`, `DEVICE`, `SENSOR` |
| resourceParam | Json | Tham số tài nguyên (JSON) |
| operator | string | `>`, `<`, `=`, `!=`, `>=`, `<=` |
| value | string | Giá trị so sánh |
| nextLogic | string | `AND`, `OR` (Mặc định: `AND`) |

> **Lưu ý:** Khi `dataSource` là `ROOM` và `property` là `sum_watt`, hệ thống sẽ tính tổng watt từ bảng `energy_metric`. Khi `dataSource` là `SENSOR` và `category` là `POWER_CONSUMPTION`, giá trị `watt` cũng được lấy từ `energy_metric` thay vì trường `currentWatt` của entity `PowerConsumption`.

#### Cấu trúc Action
| Tên trường | Loại | Mô tả |
| :--- | :--- | :--- |
| targetDeviceId | Long | ID thiết bị thực thi |
| targetDeviceCategory | string | Loại thiết bị (LIGHT, AC...) |
| actionParams | Json | Lệnh điều khiển (JSON) |
| executionOrder | int | Thứ tự thực hiện (0, 1, 2...) |

### Request Example
```json
{
  "name": "Auto AC & Light",
  "priority": 1,
  "intervalSeconds": 120
}
```

### Response (201 Created)
```json
{
  "status": 201,
  "message": "Created successfully",
  "data": {
    "id": 10,
    "name": "Auto AC & Light",
    "priority": 1,
    "isActive": true,
    "intervalSeconds": 120,
    "createdAt": "2026-03-26T15:00:00Z",
    "updatedAt": "2026-03-26T15:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rules</code> - Danh sách quy tắc (Phân trang)</summary>

### Query Parameters
| Tên | Loại | Mặc định | Mô tả |
| :--- | :--- | :--- | :--- |
| page | int | 0 | Chỉ số trang |
| limit | int | 10 | Số lượng bản ghi mỗi trang |

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 10,
        "name": "Auto AC & Light",
        "priority": 1,
        "isActive": true,
        "intervalSeconds": 120,
        "createdAt": "2026-03-26T15:00:00Z",
        "updatedAt": "2026-03-26T15:00:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 50,
    "totalPages": 5
  }
}
```
</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rules/{id}</code> - Chi tiết quy tắc</summary>

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 10,
    "name": "Auto AC & Light",
    "priority": 1,
    "isActive": true,
    "intervalSeconds": 120,
    "createdAt": "2026-03-26T15:00:00Z",
    "updatedAt": "2026-03-26T15:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>PATCH</b> <code>/api/v1/rules/{id}</code> - Cập nhật quy tắc</summary>

### Request Body
| Tên trường | Loại | Bắt buộc | Mô tả & Ràng buộc |
| :--- | :--- | :--- | :--- |
| name | string | Không | Tên quy tắc mới (Nếu truyền, không được rỗng) |
| priority | integer | Không | Độ ưu tiên (>= 0) |
| isActive | boolean | Không | Trạng thái kích hoạt (true/false) |
| intervalSeconds | integer | Không | Chu kỳ quét lặp lại (Tối thiểu: 60 giây) |

### Request Example
```json
{
  "name": "Updated Auto AC & Light",
  "intervalSeconds": 180,
  "priority": 2
}
```

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 10,
    "name": "Updated Auto AC & Light",
    "priority": 2,
    "isActive": true,
    "intervalSeconds": 180,
    "createdAt": "2026-03-26T15:00:00Z",
    "updatedAt": "2026-03-26T16:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/rules/{id}</code> - Xóa quy tắc</summary>

> Gỡ hoàn toàn lập lịch trên Quartz, xóa Rule, đồng thời cascade xóa toàn bộ Condition và Action liên thuộc.

### Response (204 No Content)
</details>

<br>

<details>
<summary><b>PATCH</b> <code>/api/v1/rules/{id}/status</code> - Bật/Tắt nhanh trạng thái</summary>

### Request Body
```json
{
  "isActive": false
}
```

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Rule status updated: false"
}
```
</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rules/reload</code> - Đồng bộ Quartz Job</summary>

> Xóa và nạp lại toàn bộ lập lịch Rule từ DB lên Quartz Scheduler.

### Response (200 OK)
```json
{
  "status": 200,
  "message": "All rules reloaded in Quartz"
}
```
</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rules/{id}/execute</code> - Kích nổ ngay lập tức</summary>

> Đánh giá điều kiện và chạy Rule ngay lập tức mà không đợi chu kỳ kế tiếp.

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Rule execution triggered immediately"
}
```
</details>

<br>

---

## Helper Sub-Resource Endpoints (Conditions & Actions của Rule)

<details>
<summary><b>GET</b> <code>/api/v1/rules/{id}/conditions</code> - Danh sách Điều kiện của Rule</summary>

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 100,
      "ownerCategory": "RULE",
      "ownerId": "10",
      "sourceCategory": "SENSOR",
      "sourceTargetId": "1",
      "sourceTargetType": "TEMPERATURE",
      "property": "temperature",
      "operator": ">",
      "value": "28",
      "sortOrder": 0,
      "nextLogic": "AND",
      "createdAt": "2026-03-26T15:00:00Z"
    }
  ]
}
```
</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rules/{id}/conditions</code> - Thêm nhanh Điều kiện cho Rule</summary>
### 2. Data Source: ROOM (Phòng)
**Dùng cho Điều kiện (`resourceParam`):**
*   `roomId` (Long - Bắt buộc): ID của phòng cần kiểm tra.
*   `property` (String - Bắt buộc): Thuộc tính cần kiểm tra. Bao gồm:
    *   `avg_temperature`: Nhiệt độ trung bình trong khoảng thời gian cấu hình hệ thống (`lookbackMinutes`, cấu hình global phía server).
    *   `sum_watt`: Tổng điện năng tiêu thụ trong khoảng thời gian cấu hình hệ thống (`lookbackMinutes`, cấu hình global phía server) **tính bằng cách sum giá trị `power` từ bảng `energy_metric`**.
    *   `avg_humidity`: Độ ẩm trung vị (Median) trong phòng, tổng hợp từ tất cả cảm biến độ ẩm đang hoạt động.
    *   `avg_lux`: Cường độ ánh sáng trung vị (Median) trong phòng, tổng hợp từ tất cả cảm biến ánh sáng đang hoạt động.
    *   `avg_co2`: Nồng độ CO2 trung bình (Mean) trong phòng, tổng hợp từ tất cả cảm biến CO2 đang hoạt động.
    *   `max_co2`: Nồng độ CO2 lớn nhất (Max) trong phòng, tổng hợp từ tất cả cảm biến CO2 đang hoạt động. Dùng cho Automation kích hoạt thiết bị thông gió.

> Tự động gán `ownerCategory = "RULE"` và `ownerId = "{id}"`.

### Request Body
```json
{
  "ownerCategory": "RULE",
  "ownerId": "10",
  "sourceCategory": "SENSOR",
  "sourceTargetId": "1",
  "sourceTargetType": "TEMPERATURE",
  "property": "temperature",
  "operator": ">",
  "value": "28",
  "sortOrder": 0,
  "nextLogic": "AND"
}
```
### 3. Data Source: SENSOR (Cảm biến)
**Dùng cho Điều kiện (`resourceParam`):**
*   `category` (String - Bắt buộc): Loại cảm biến (`TEMPERATURE`, `POWER_CONSUMPTION`, `HUMIDITY`, `SENSOR_CO2`, `SENSOR_LUX`).
*   `sensorId` (Long - Bắt buộc): ID của cảm biến.
*   `property` (String - Bắt buộc): Tùy thuộc vào `category`:
    *   Với `TEMPERATURE`: `temperature` (Nhiệt độ hiện tại).
    *   Với `POWER_CONSUMPTION`: `watt` (Công suất tiêu thụ hiện tại, **được lấy từ bảng `energy_metric`**).
    *   Với `HUMIDITY`: `humidity` (Độ ẩm hiện tại).
    *   Với `SENSOR_CO2`: `co2` (Nồng độ CO2 hiện tại).
    *   Với `SENSOR_LUX`: `lux` (Cường độ ánh sáng hiện tại).

### Response (201 Created)
```json
{
  "status": 201,
  "message": "Created successfully",
  "data": {
    "id": 101,
    "ownerCategory": "RULE",
    "ownerId": "10",
    "sourceCategory": "SENSOR",
    "sourceTargetId": "1",
    "sourceTargetType": "TEMPERATURE",
    "property": "temperature",
    "operator": ">",
    "value": "28",
    "sortOrder": 0,
    "nextLogic": "AND",
    "createdAt": "2026-03-26T15:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/rules/{id}/conditions</code> - Thay thế toàn bộ Điều kiện của Rule (Bulk Replace)</summary>

> Thay thế toàn bộ Condition của Rule (Atomic).

### Request Body
```json
[
  {
    "id": 100,
    "sourceCategory": "SENSOR",
    "sourceTargetId": "1",
    "sourceTargetType": "TEMPERATURE",
    "property": "temperature",
    "operator": ">",
    "value": "28",
    "sortOrder": 0,
    "nextLogic": "AND"
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
      "id": 102,
      "ownerCategory": "RULE",
      "ownerId": "10",
      "sourceCategory": "SENSOR",
      "sourceTargetId": "1",
      "sourceTargetType": "TEMPERATURE",
      "property": "temperature",
      "operator": ">",
      "value": "28",
      "sortOrder": 0,
      "nextLogic": "AND",
      "createdAt": "2026-03-26T15:00:00Z"
    }
  ]
}
```
</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rules/{id}/actions</code> - Danh sách Hành động của Rule</summary>

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
<summary><b>POST</b> <code>/api/v1/rules/{id}/actions</code> - Thêm nhanh Hành động cho Rule</summary>

> Tự động gán `ownerCategory = "RULE"` và `ownerId = "{id}"`.

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
    "id": 201,
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
}
```
</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/rules/{id}/actions</code> - Thay thế toàn bộ Hành động của Rule (Bulk Replace)</summary>

> Thay thế toàn bộ Action của Rule (Atomic).

### Request Body
```json
[
  {
    "id": 200,
    "targetCategory": "AIR_CONDITION",
    "targetId": "10",
    "params": {
      "power": "ON",
      "temperature": 24
    },
    "executionOrder": 0
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
      "id": 202,
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
