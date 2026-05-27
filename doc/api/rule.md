# Rule Module

## Rule API Documentation
Hệ thống tự động hóa dựa trên chu kỳ giây độc lập, hỗ trợ đa hành động và cấu hình Interval riêng biệt cho từng quy tắc.

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
  "intervalSeconds": 120,
  "conditions": [
    {
      "sortOrder": 0,
      "dataSource": "SENSOR",
      "resourceParam": { "sensorId": 1, "property": "temperature" },
      "operator": ">",
      "value": "28"
    }
  ],
  "actions": [
    {
      "targetDeviceId": 10,
      "targetDeviceCategory": "AIR_CONDITION",
      "actionParams": { "power": "ON", "temperature": 24 },
      "executionOrder": 0
    }
  ]
}
```

### Response (201 Created)
```json
{
  "status": 201,
  "message": "Created",
  "data": {
    "id": 10,
    "name": "Auto AC & Light",
    "isActive": true,
    "intervalSeconds": 120,
    "conditions": [ ... ],
    "actions": [ ... ],
    "createdAt": "2026-03-26T15:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rules/all</code> - Lấy tất cả quy tắc đang hoạt động</summary>

> Danh sách tất cả các Rule có `isActive=true` (trả về đầy đủ cấu trúc RuleDto).

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 10,
      "name": "Auto AC & Light",
      "priority": 1,
      "isActive": true,
      "intervalSeconds": 120,
      "conditions": [
        {
          "id": 100,
          "sortOrder": 0,
          "dataSource": "SENSOR",
          "resourceParam": { "sensorId": 1, "property": "temperature" },
          "operator": ">",
          "value": "28",
          "nextLogic": "AND"
        }
      ],
      "actions": [
        {
          "id": 200,
          "targetDeviceId": 10,
          "targetDeviceCategory": "AIR_CONDITION",
          "actionParams": { "power": "ON", "temperature": 24 },
          "executionOrder": 0,
          "targetName": "Điều hòa Phòng Khách"
        }
      ],
      "createdAt": "2026-03-26T15:00:00Z",
      "updatedAt": "2026-03-26T15:00:00Z"
    }
  ]
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
| size | int | 10 | Kích thước trang |

### Response (200 OK)
```json
{
  "status": 200,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 10,
    "totalElements": 50
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
  "data": {
    "id": 10,
    "name": "Auto AC & Light",
    "priority": 1,
    "intervalSeconds": 120,
    "conditions": [
      {
        "id": 100,
        "sortOrder": 0,
        "dataSource": "SENSOR",
        "resourceParam": { "sensorId": 1, "property": "temperature" },
        "operator": ">",
        "value": "28",
        "nextLogic": "AND"
      }
    ],
    "actions": [
      {
        "id": 200,
        "targetDeviceId": 10,
        "targetDeviceCategory": "AIR_CONDITION",
        "actionParams": { "power": "ON", "temperature": 24 },
        "executionOrder": 0,
        "targetName": "Điều hòa Phòng Khách"
      }
    ],
    "createdAt": "2026-03-26T15:00:00Z",
    "updatedAt": "2026-03-26T15:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>PATCH</b> <code>/api/v1/rules/{id}</code> - Cập nhật quy tắc</summary>

> **Lưu ý quan trọng về cơ chế cập nhật (Partial Update / Replace Arrays):**
> - **Các trường thuộc tính đơn lẻ (name, priority, intervalSeconds, isActive):** Nếu truyền giá trị `null` hoặc không truyền thì giá trị cũ trong DB được **giữ nguyên**. Nếu truyền giá trị khác `null` thì sẽ ghi đè giá trị cũ.
> - **Các trường dạng mảng (conditions, actions):**
>   - Nếu không truyền (hoặc truyền `null`): Giữ nguyên danh sách cũ trong DB.
>   - Nếu truyền mảng (kể cả mảng rỗng `[]`): Hệ thống sẽ **xóa sạch toàn bộ** các bản ghi cũ của mảng đó trong DB và **REPLACE** hoàn toàn bằng mảng mới được truyền lên.

### Request Body
| Tên trường | Loại | Bắt buộc | Mô tả & Ràng buộc |
| :--- | :--- | :--- | :--- |
| name | string | Không | Tên quy tắc (Nếu truyền, không được rỗng) |
| priority | integer | Không | Độ ưu tiên (>= 0) |
| isActive | boolean | Không | Trạng thái kích hoạt (true/false) |
| intervalSeconds | integer | Không | Chu kỳ quét lặp lại (Tối thiểu: 60 giây) |
| conditions | array | Không | Danh sách điều kiện cập nhật (Xem cấu trúc bên dưới) |
| actions | array | Không | Danh sách hành động cập nhật (Xem cấu trúc bên dưới) |

#### Cấu trúc phần tử trong mảng `conditions` (UpdateRuleConditionDto)
| Tên trường | Loại | Bắt buộc | Mô tả & Ràng buộc |
| :--- | :--- | :--- | :--- |
| sortOrder | int | Có | Thứ tự đánh giá (>= 0) |
| dataSource | string | Có | Nguồn dữ liệu (`SYSTEM`, `ROOM`, `DEVICE`, `SENSOR`) |
| resourceParam | Json | Có | Tham số tài nguyên (ví dụ: `{ "sensorId": 1, "property": "temperature" }`) |
| operator | string | Có | Toán tử so sánh (`>`, `<`, `=`, `!=`, `>=`, `<=`) |
| value | string | Có | Giá trị so sánh (Không được trống) |
| nextLogic | string | Không | Logic liên kết kế tiếp (`AND`, `OR`) |

#### Cấu trúc phần tử trong mảng `actions` (UpdateRuleActionDto)
| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| targetDeviceId | Long | Không | ID của thiết bị đích thực thi hành động |
| targetDeviceCategory | string | Không | Loại thiết bị thực thi (`LIGHT`, `FAN`, `AIR_CONDITION`...) |
| actionParams | Json | Không | Các tham số điều khiển thiết bị dạng JSON (ví dụ: `{ "power": "ON", "temp": 24 }`) |
| executionOrder | int | Không | Thứ tự thực thi hành động (0, 1, 2...) |

### Request Example
```json
{
  "name": "Updated Rule 1 - Auto Air Condition",
  "intervalSeconds": 180,
  "isActive": true,
  "conditions": [
    {
      "sortOrder": 0,
      "dataSource": "SENSOR",
      "resourceParam": { "sensorId": 1, "property": "temperature" },
      "operator": ">",
      "value": "30",
      "nextLogic": "AND"
    }
  ],
  "actions": [
    {
      "targetDeviceId": 3,
      "targetDeviceCategory": "AIR_CONDITION",
      "actionParams": { "power": "ON", "temperature": 24 },
      "executionOrder": 0
    }
  ]
}
```

### Response Example (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Updated Rule 1 - Auto Air Condition",
    "priority": 1,
    "intervalSeconds": 180,
    "isActive": true,
    "conditions": [
      {
        "id": 101,
        "sortOrder": 0,
        "dataSource": "SENSOR",
        "resourceParam": { "sensorId": 1, "property": "temperature" },
        "operator": ">",
        "value": "30",
        "nextLogic": "AND"
      }
    ],
    "actions": [
      {
        "id": 201,
        "targetDeviceId": 3,
        "targetDeviceCategory": "AIR_CONDITION",
        "actionParams": { "power": "ON", "temperature": 24 },
        "executionOrder": 0
      }
    ],
    "createdAt": "2026-05-26T10:00:00Z",
    "updatedAt": "2026-05-26T11:30:00Z"
  },
  "timestamp": "2026-05-26T11:30:00Z"
}
```
</details>

<details>
<summary><b>DELETE</b> <code>/api/v1/rules/{id}</code> - Xóa quy tắc</summary>

> Gỡ hoàn toàn lập lịch trên Quartz và xóa cứng trong database.

### Response (204 No Content)
</details>

<br>

<details>
<summary><b>PATCH</b> <code>/api/v1/rules/{id}/status</code> - Bật/Tắt nhanh</summary>

### Request Body
```json
{ "isActive": false }
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

> Xóa và nạp lại toàn bộ lập lịch từ DB.

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

> Chạy Rule ngay bây giờ mà không đợi đến chu kỳ tiếp theo.

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

<details>
<summary>Xem chi tiết các hằng số (Enums)</summary>

### RuleDataSource (Data Source)

| Giá trị | Mô tả |
| :--- | :--- |
| SYSTEM | Dữ liệu từ hệ thống |
| ROOM | Dữ liệu từ trạng thái phòng |
| DEVICE | Dữ liệu từ trạng thái thiết bị |
| SENSOR | Dữ liệu từ cảm biến |

### ConditionOperator (Toán tử so sánh)

| Giá trị | Mô tả |
| :--- | :--- |
| `=` | Bằng |
| `!=` | Khác |
| `>` | Lớn hơn |
| `<` | Nhỏ hơn |
| `>=` | Lớn hơn hoặc bằng |
| `<=` | Nhỏ hơn hoặc bằng |

### ConditionLogic (Logic kết hợp)

| Giá trị | Mô tả |
| :--- | :--- |
| AND | Điều kiện kết hợp AND |
| OR | Điều kiện kết hợp OR |

### Ràng buộc bổ sung

| Tên | Mô tả |
| :--- | :--- |
| intervalSeconds | Chu kỳ quét lặp lại, giá trị hợp lệ từ 60 đến 86400 (giây) |
| executionOrder | Thứ tự thực hiện các hành động, giá trị từ 0, 1, 2... |

### Ràng buộc và cấu trúc của `actionParams` theo thiết bị
Tham số `actionParams` được kiểm tra hợp lệ tại tầng nghiệp vụ (Service layer) dựa trên loại thiết bị (`targetDeviceCategory`):

#### 1. Loại thiết bị LIGHT (Thiết bị chiếu sáng)
* **Các trường tham số:**
  * `power` (String - Không bắt buộc): `ON`, `OFF`
  * `level` (Integer - Không bắt buộc): Giá trị độ sáng từ `0` đến `100`

#### 2. Loại thiết bị FAN (Quạt)
* **Các trường tham số:**
  * `power` (String - Không bắt buộc): `ON`, `OFF`
  * `mode` (String - Không bắt buộc): `COOL`, `HEAT`, `DRY`, `FAN`, `AUTO`, `NORMAL`, `SLEEP`, `NATURAL`
  * `speed` (Integer - Không bắt buộc): Tốc độ quạt từ `1` đến `3`
  * `swing` (String - Không bắt buộc): `ON`, `OFF`, `AUTO`, `HORIZONTAL`, `VERTICAL`
  * `light` (String - Không bắt buộc): `ON`, `OFF`

#### 3. Loại thiết bị AIR_CONDITION (Điều hòa không khí)
* **Các trường tham số:**
  * `power` (String - Không bắt buộc): `ON`, `OFF`
  * `temperature` (Integer - Không bắt buộc): Nhiệt độ thiết lập từ `16` đến `32`
  * `mode` (String - Không bắt buộc): `COOL`, `HEAT`, `DRY`, `FAN`, `AUTO`, `NORMAL`, `SLEEP`, `NATURAL`
  * `fanSpeed` (Integer - Không bắt buộc): Tốc độ gió từ `0` đến `5`
  * `swing` (String - Không bắt buộc): `ON`, `OFF`, `AUTO`, `HORIZONTAL`, `VERTICAL`

> [!WARNING]
> Bất kỳ thiết bị thuộc loại nào khác (ví dụ: các cảm biến chỉ đọc như `TEMPERATURE`, `POWER_CONSUMPTION`) sẽ bị từ chối với lỗi `400 Bad Request` (ví dụ: `Unsupported category: TEMPERATURE`).

</details>
