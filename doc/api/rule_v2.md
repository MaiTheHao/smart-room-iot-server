# Rule Module V2

## RuleV2 API Documentation
Hệ thống tự động hóa dựa trên chu kỳ giây độc lập, hỗ trợ đa hành động và cấu hình Interval riêng biệt cho từng quy tắc.

---

<details>
<summary><b>POST</b> <code>/api/v2/rules</code> - Tạo mới quy tắc</summary>

> Khởi tạo RuleV2 mới với chu kỳ chạy riêng.

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
| resourceParam | Json | Tham số tài nguyên (xem [RuleV1 Doc](rule.md)) |
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
      "actionParams": { "power": "ON", "temp": 24 },
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
<summary><b>GET</b> <code>/api/v2/rules/all</code> - Lấy tất cả quy tắc đang hoạt động</summary>

> Danh sách rút gọn các Rule có `isActive=true`.

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 10,
      "name": "Auto AC & Light",
      "isActive": true,
      "intervalSeconds": 120
    }
  ]
}
```
</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v2/rules</code> - Danh sách quy tắc (Phân trang)</summary>

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
<summary><b>GET</b> <code>/api/v2/rules/{id}</code> - Chi tiết quy tắc</summary>

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
        "operator": "GT",
        "value": "28"
      }
    ],
    "actions": [
      {
        "id": 200,
        "targetDeviceId": 10,
        "targetDeviceCategory": "AIR_CONDITION",
        "executionOrder": 0
      }
    ]
  }
}
```
</details>

<br>

<details>
<summary><b>PATCH</b> <code>/api/v2/rules/{id}</code> - Cập nhật quy tắc</summary>

> **Lưu ý quan trọng về mảng:**
> - Nếu `conditions` hoặc `actions` được truyền (kể cả mảng rỗng `[]`), hệ thống sẽ **REPLACE** hoàn toàn mảng cũ bằng mảng mới.
> - Các trường khác (name, interval...) nếu `null` sẽ được giữ nguyên.

### Request Body
| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| intervalSeconds | int | Không | Đổi chu kỳ (Min: 60) |
| isActive | boolean | Không | Đổi trạng thái |
| actions | array | Không | Thay thế toàn bộ hành động |

### Request Example
```json
{
  "intervalSeconds": 180,
  "actions": [
    {
      "targetDeviceId": 11,
      "targetDeviceCategory": "LIGHT",
      "actionParams": { "power": "ON" },
      "executionOrder": 0
    }
  ]
}
```

### Response (200 OK)
Trả về object RuleV2 đầy đủ sau khi update.
</details>

<br>

<details>
<summary><b>PATCH</b> <code>/api/v2/rules/{id}/status</code> - Bật/Tắt nhanh</summary>

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
<summary><b>POST</b> <code>/api/v2/rules/reload</code> - Đồng bộ Quartz Job</summary>

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
<summary><b>POST</b> <code>/api/v2/rules/{id}/execute</code> - Kích nổ ngay lập tức</summary>

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
<summary>Xem chi tiết Enumerations & Constants</summary>

### Ràng buộc bổ sung
| Tên | Giá trị | Mô tả |
| :--- | :--- | :--- |
| intervalSeconds | 60 - 86400 | Giới hạn từ 1 phút đến 24 giờ |
| executionOrder | 0, 1, 2... | Thứ tự thực hiện các hành động |

> Mọi hằng số khác kế thừa hoàn toàn từ RuleV1. Vui lòng xem tài liệu: [Rule V1 Enums](rule.md#xem-chi-tiết-các-hằng-số-và-ràng-buộc-enumerations--constraints)
</details>
