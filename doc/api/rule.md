# Rule Module

## Rule API Documentation

Quy tắc (Rule) là cơ chế để tự động điều khiển các thiết bị dựa trên các điều kiện được định nghĩa. Module Rule cho phép tạo các kịch bản quét định kỳ (Global Scan) theo lịch của hệ thống. Nếu các điều kiện (Conditions) thỏa mãn, hệ thống sẽ thực thi hành động (Action) lên thiết bị mục tiêu (Target Device).

Mỗi quy tắc có:
- **Điều kiện (Conditions)**: Các tiêu chí cần được đáp ứng (ví dụ: nhiệt độ > 30°C)
- **Hành động (Action)**: Lệnh được thực thi khi điều kiện được đáp ứng (ví dụ: bật điều hòa)
- **Độ ưu tiên (Priority)**: Được sử dụng để chọn quy tắc khi có nhiều quy tắc thỏa mãn

---

<details>
<summary><b>POST</b> <code>/api/v1/rules</code> - Tạo mới quy tắc</summary>

> Tạo mới một quy tắc điều khiển thiết bị tự động.

### Request Body

| Tên trường            | Loại     | Bắt buộc | Mô tả                                                                      |
| :-------------------- | :------- | :------- | :------------------------------------------------------------------------- |
| name                  | string   | Có       | Tên quy tắc (không rỗng, unique)                                          |
| priority              | integer  | Có       | Độ ưu tiên (>= 0, giá trị cao hơn = ưu tiên cao hơn)                     |
| roomId                | Long     | Có       | ID của phòng áp dụng quy tắc (dùng để xác định ngữ cảnh dữ liệu)         |
| targetDeviceId        | Long     | Có       | ID của thiết bị mục tiêu sẽ bị tác động khi quy tắc thỏa mãn             |
| targetDeviceCategory  | string   | Có       | Loại thiết bị mục tiêu (xem DeviceCategory dưới Enumerations)            |
| actionParams          | JsonNode | Có       | Tham số điều khiển gửi xuống thiết bị mục tiêu (JSON format, phụ thuộc vào targetDeviceCategory) |
| conditions            | array    | Có       | Danh sách điều kiện (ít nhất 1 điều kiện, xem chi tiết bên dưới)         |

#### Cấu trúc chi tiết của mỗi phần tử trong `conditions`

| Tên trường    | Loại     | Bắt buộc | Mô tả                                                                                     |
| :------------ | :------- | :------- | :---------------------------------------------------------------------------------------- |
| sortOrder     | integer  | Có       | Thứ tự đánh giá điều kiện (bắt đầu từ 0)                                                 |
| dataSource    | string   | Có       | Nguồn dữ liệu: `SYSTEM`, `ROOM`, `DEVICE`, `SENSOR` (xem chi tiết [RuleDataSource](#chi-tiết-cấu-trúc-resourceparam-theo-từng-data-source))       |
| resourceParam | JsonNode | Có       | Tham số tài nguyên (JSON format, cấu trúc phụ thuộc vào dataSource, xem chi tiết bên dưới) |
| operator      | string   | Có       | Toán tử so sánh: `>`, `<`, `=`, `!=`, `>=`, `<=` (xem Operators dưới Enumerations)      |
| value         | string   | Có       | Giá trị mốc dùng để so sánh (hệ thống tự parse sang số hoặc chuỗi)                      |
| nextLogic     | string   | Không    | Toán tử logic kết nối với điều kiện tiếp theo: `AND`, `OR` (mặc định: `AND`)            |

### Request Example

```json
{
  "name": "Bật điều hòa phòng khách khi nhiệt độ > 30",
  "priority": 10,
  "roomId": 1,
  "targetDeviceId": 101,
  "targetDeviceCategory": "AIR_CONDITION",
  "actionParams": {
    "power": "ON",
    "temp": 24,
    "mode": "COOL"
  },
  "conditions": [
    {
      "sortOrder": 0,
      "dataSource": "SENSOR",
      "resourceParam": {
        "category": "TEMPERATURE",
        "sensorId": 50,
        "property": "temperature"
      },
      "operator": ">",
      "value": "30",
      "nextLogic": "AND"
    },
    {
      "sortOrder": 1,
      "dataSource": "DEVICE",
      "resourceParam": {
        "category": "AIR_CONDITION",
        "deviceId": 101,
        "property": "power"
      },
      "operator": "=",
      "value": "OFF",
      "nextLogic": null
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
    "id": 1,
    "name": "Bật điều hòa phòng khách khi nhiệt độ > 30",
    "priority": 10,
    "isActive": true,
    "roomId": 1,
    "targetDeviceId": 101,
    "targetDeviceCategory": "AIR_CONDITION",
    "actionParams": {
      "power": "ON",
      "temp": 24,
      "mode": "COOL"
    },
    "conditions": [
      {
        "id": 1,
        "sortOrder": 0,
        "dataSource": "SENSOR",
        "resourceParam": {
          "category": "TEMPERATURE",
          "sensorId": 50,
          "property": "temperature"
        },
        "operator": ">",
        "value": "30",
        "nextLogic": "AND",
        "createdAt": "2026-03-01T09:00:00Z",
        "updatedAt": "2026-03-01T09:00:00Z"
      },
      {
        "id": 2,
        "sortOrder": 1,
        "dataSource": "DEVICE",
        "resourceParam": {
          "category": "AIR_CONDITION",
          "deviceId": 101,
          "property": "power"
        },
        "operator": "=",
        "value": "OFF",
        "nextLogic": null,
        "createdAt": "2026-03-01T09:00:00Z",
        "updatedAt": "2026-03-01T09:00:00Z"
      }
    ],
    "createdAt": "2026-03-01T09:00:00Z",
    "updatedAt": "2026-03-01T09:00:00Z"
  },
  "timestamp": "2026-03-01T09:00:00Z"
}
```

### Response (400 Bad Request)

```json
{
  "status": 400,
  "message": "Bad Request",
  "errors": ["Rule name already exists"],
  "timestamp": "2026-03-01T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rules/all</code> - Lấy danh sách tất cả quy tắc</summary>

> Lấy toàn bộ danh sách quy tắc (không phân trang), thường dùng cho dropdown hoặc cache nội bộ.

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "Bật điều hòa khi nóng",
      "priority": 10,
      "isActive": true,
      "roomId": 5,
      "targetDeviceId": 101,
      "targetDeviceCategory": "AIR_CONDITION",
      "actionParams": {
        "power": "ON",
        "temp": 22,
        "mode": "COOL"
      },
      "conditions": [
        {
          "id": 1,
          "sortOrder": 0,
          "dataSource": "ROOM",
          "resourceParam": {
            "property": "temperature"
          },
          "operator": ">",
          "value": "30",
          "nextLogic": "AND",
          "createdAt": "2026-03-01T09:00:00Z",
          "updatedAt": "2026-03-01T09:00:00Z"
        }
      ],
      "createdAt": "2026-03-01T09:00:00Z",
      "updatedAt": "2026-03-01T09:00:00Z"
    }
  ],
  "timestamp": "2026-03-01T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rules</code> - Lấy danh sách quy tắc (Phân trang)</summary>

> Lấy danh sách quy tắc với hỗ trợ phân trang.

### Query Parameters

| Tên  | Loại    | Mô tả               | Bắt buộc/Mặc định |
| :--- | :------ | :------------------ | :---------------- |
| page | integer | Trang cần lấy       | Mặc định: 0       |
| size | integer | Số lượng trên trang | Mặc định: 10      |

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Bật điều hòa khi nóng",
        "priority": 10,
        "isActive": true,
        "roomId": 5,
        "targetDeviceId": 101,
        "targetDeviceCategory": "AIR_CONDITION",
        "actionParams": {
          "power": "ON",
          "temp": 22,
          "mode": "COOL"
        },
        "conditions": [
          {
            "id": 1,
            "sortOrder": 0,
            "dataSource": "ROOM",
            "resourceParam": {
              "property": "temperature"
            },
            "operator": ">",
            "value": "30",
            "nextLogic": "AND",
            "createdAt": "2026-03-01T09:00:00Z",
            "updatedAt": "2026-03-01T09:00:00Z"
          }
        ],
        "createdAt": "2026-03-01T09:00:00Z",
        "updatedAt": "2026-03-01T09:00:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2026-03-01T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rules/{id}</code> - Lấy chi tiết quy tắc</summary>

> Lấy thông tin chi tiết của một quy tắc theo ID.

### Path Parameters

| Tên | Loại | Mô tả                   | Bắt buộc |
| :-- | :--- | :---------------------- | :------- |
| id  | Long | ID của quy tắc cần lấy  | Có       |

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Bật điều hòa khi nóng",
    "priority": 10,
    "isActive": true,
    "roomId": 5,
    "targetDeviceId": 101,
    "targetDeviceCategory": "AIR_CONDITION",
    "actionParams": {
      "power": "ON",
      "temp": 22,
      "mode": "COOL"
    },
    "conditions": [
      {
        "id": 1,
        "sortOrder": 0,
        "dataSource": "ROOM",
        "resourceParam": {
          "property": "temperature"
        },
        "operator": ">",
        "value": "30",
        "nextLogic": "AND",
        "createdAt": "2026-03-01T09:00:00Z",
        "updatedAt": "2026-03-01T09:00:00Z"
      },
      {
        "id": 2,
        "sortOrder": 1,
        "dataSource": "DEVICE",
        "resourceParam": {
          "category": "AIR_CONDITION",
          "deviceId": 101,
          "property": "power"
        },
        "operator": "=",
        "value": "OFF",
        "nextLogic": null,
        "createdAt": "2026-03-01T09:00:00Z",
        "updatedAt": "2026-03-01T09:00:00Z"
      }
    ],
    "createdAt": "2026-03-01T09:00:00Z",
    "updatedAt": "2026-03-01T09:00:00Z"
  },
  "timestamp": "2026-03-01T09:00:00Z"
}
```

### Response (404 Not Found)

```json
{
  "status": 404,
  "message": "Not Found",
  "errors": ["Rule not found"],
  "timestamp": "2026-03-01T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/rules/{id}</code> - Cập nhật quy tắc</summary>

> Cập nhật thông tin của một quy tắc hiện có. Lưu ý: Không cho phép cập nhật `roomId`.

### Path Parameters

| Tên | Loại | Mô tả                        | Bắt buộc |
| :-- | :--- | :--------------------------- | :------- |
| id  | Long | ID của quy tắc cần cập nhật  | Có       |

### Request Body

| Tên trường            | Loại     | Bắt buộc | Mô tả                                                               |
| :-------------------- | :------- | :------- | :------------------------------------------------------------------ |
| name                  | string   | Có       | Tên quy tắc (không rỗng)                                           |
| priority              | integer  | Có       | Độ ưu tiên (>= 0)                                                  |
| targetDeviceId        | Long     | Có       | ID của thiết bị mục tiêu                                           |
| targetDeviceCategory  | string   | Có       | Loại thiết bị. Nếu đổi, bắt buộc gửi kèm `actionParams` mới       |
| actionParams          | JsonNode | Có       | Tham số điều khiển thiết bị mục tiêu (JSON format)                |
| isActive              | boolean  | Không    | Trạng thái hoạt động của quy tắc                                   |
| conditions            | array    | Có       | Danh sách điều kiện mới/cập nhật (ít nhất 1 điều kiện)            |

#### Cấu trúc chi tiết của mỗi phần tử trong `conditions` (khi Update)

| Tên trường    | Loại     | Bắt buộc | Mô tả                                                                                    |
| :------------ | :------- | :------- | :--------------------------------------------------------------------------------------- |
| id            | Long     | Không    | ID của condition nếu muốn cập nhật điều kiện đã có (bỏ qua nếu tạo mới)                 |
| sortOrder     | integer  | Có       | Thứ tự đánh giá điều kiện (bắt đầu từ 0)                                                |
| dataSource    | string   | Có       | Nguồn dữ liệu: `SYSTEM`, `ROOM`, `DEVICE`, `SENSOR` (xem chi tiết [RuleDataSource](#chi-tiết-cấu-trúc-resourceparam-theo-từng-data-source))       |
| resourceParam | JsonNode | Có       | Tham số tài nguyên (JSON format, phụ thuộc vào dataSource)                              |
| operator      | string   | Có       | Toán tử so sánh: `>`, `<`, `=`, `!=`, `>=`, `<=`                                        |
| value         | string   | Có       | Giá trị so sánh                                                                          |
| nextLogic     | string   | Không    | Logic kết nối: `AND`, `OR` (mặc định: `AND`)                                            |

### Request Example

```json
{
  "name": "Bật điều hòa khi nóng (cập nhật)",
  "priority": 15,
  "targetDeviceId": 101,
  "targetDeviceCategory": "AIR_CONDITION",
  "actionParams": {
    "power": "ON",
    "temp": 20,
    "mode": "COOL"
  },
  "isActive": true,
  "conditions": [
    {
      "id": 1,
      "sortOrder": 0,
      "dataSource": "ROOM",
      "resourceParam": {
        "property": "temperature"
      },
      "operator": ">",
      "value": "32",
      "nextLogic": "AND"
    }
  ]
}
```

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Bật điều hòa khi nóng (cập nhật)",
    "priority": 15,
    "isActive": true,
    "roomId": 5,
    "targetDeviceId": 101,
    "targetDeviceCategory": "AIR_CONDITION",
    "actionParams": {
      "power": "ON",
      "temp": 20,
      "mode": "COOL"
    },
    "conditions": [
      {
        "id": 1,
        "sortOrder": 0,
        "dataSource": "ROOM",
        "resourceParam": {
          "property": "temperature"
        },
        "operator": ">",
        "value": "32",
        "nextLogic": "AND",
        "createdAt": "2026-03-01T09:00:00Z",
        "updatedAt": "2026-03-01T10:00:00Z"
      }
    ],
    "createdAt": "2026-03-01T09:00:00Z",
    "updatedAt": "2026-03-01T10:00:00Z"
  },
  "timestamp": "2026-03-01T10:00:00Z"
}
```

### Response (404 Not Found)

```json
{
  "status": 404,
  "message": "Not Found",
  "errors": ["Rule not found"],
  "timestamp": "2026-03-01T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/rules/{id}</code> - Xóa quy tắc</summary>

> Xóa một quy tắc khỏi hệ thống.

### Path Parameters

| Tên | Loại | Mô tả                  | Bắt buộc |
| :-- | :--- | :--------------------- | :------- |
| id  | Long | ID của quy tắc cần xóa | Có       |

### Response (204 No Content)

```json
{
  "status": 204,
  "message": "Rule deleted successfully",
  "data": null,
  "timestamp": "2026-03-01T09:00:00Z"
}
```

### Response (404 Not Found)

```json
{
  "status": 404,
  "message": "Not Found",
  "errors": ["Rule not found"],
  "timestamp": "2026-03-01T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>PATCH</b> <code>/api/v1/rules/{id}/status</code> - Đổi trạng thái quy tắc</summary>

> Thay đổi trạng thái hoạt động (active/inactive) của một quy tắc.

### Path Parameters

| Tên | Loại | Mô tả          | Bắt buộc |
| :-- | :--- | :------------- | :------- |
| id  | Long | ID của quy tắc | Có       |

### Request Body

| Tên      | Loại    | Bắt buộc | Mô tả          |
| :------- | :------ | :------- | :------------- |
| isActive | boolean | Có       | Trạng thái mới |

### Request Example

```json
{
  "isActive": false
}
```

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Rule status updated: false",
  "data": null,
  "timestamp": "2026-03-01T09:00:00Z"
}
```

### Response (404 Not Found)

```json
{
  "status": 404,
  "message": "Not Found",
  "errors": ["Rule not found"],
  "timestamp": "2026-03-01T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rules/scan</code> - Quét quy tắc (toàn cục)</summary>

> Thực thi quét toàn cục tất cả các quy tắc đang hoạt động.

**Quá trình quét:**
1. Lấy tất cả các quy tắc đang hoạt động (`isActive = true`)
2. Nhóm các quy tắc theo thiết bị mục tiêu (`category:id`)
3. Đánh giá điều kiện của từng quy tắc trong mỗi nhóm
4. Chọn quy tắc có độ ưu tiên cao nhất (Winner-Takes-All)
5. Thực thi hành động của quy tắc chiến thắng

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Global rule scan executed",
  "data": null,
  "timestamp": "2026-03-01T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rules/reload</code> - Tải lại cấu hình quy tắc</summary>

> Tái tải cấu hình tất cả các quy tắc từ cơ sở dữ liệu.

Sử dụng trong trường hợp cần đồng bộ hóa cấu hình quy tắc sau khi có thay đổi trong database.

### Response (200 OK)

```json
{
  "status": 200,
  "message": "All rules reloaded",
  "data": null,
  "timestamp": "2026-03-01T09:00:00Z"
}
```

</details>

<br>

---

<details>
<summary>Xem chi tiết các hằng số và ràng buộc (Enumerations & Constraints)</summary>

### RuleDataSource (Nguồn dữ liệu)

| Giá trị | Mô tả                                                                     |
| :------ | :------------------------------------------------------------------------ |
| SYSTEM  | Lấy dữ liệu từ hệ thống (thời gian, ngày, trạng thái hệ thống)          |
| ROOM    | Lấy dữ liệu từ phòng (nhiệt độ trung bình, tổng điện năng tiêu thụ)     |
| DEVICE  | Lấy dữ liệu từ thiết bị (trạng thái bật/tắt, chế độ hoạt động)          |
| SENSOR  | Lấy dữ liệu từ cảm biến (giá trị nhiệt độ, độ ẩm, ánh sáng ngay lúc đó) |

### DeviceCategory (Loại thiết bị)

| Giá trị           | Mô tả                          |
| :---------------- | :----------------------------- |
| LIGHT             | Thiết bị chiếu sáng (đèn)      |
| AIR_CONDITION     | Thiết bị điều hòa không khí    |
| TEMPERATURE       | Thiết bị/cảm biến đo nhiệt độ |
| POWER_CONSUMPTION | Thiết bị đo tiêu thụ điện năng |
| FAN               | Thiết bị quạt                  |

### Operators (Toán tử so sánh)

Sử dụng các ký tự toán học chuẩn sau (gửi dưới dạng chuỗi String trong trường `operator`):

| Toán tử | Database | Enum (Backend) | Mô tả                  |
| :------ | :------- | :------------- | :--------------------- |
| `=`     | `=`      | `EQ`           | Bằng                   |
| `!=`    | `!=`     | `NEQ`          | Khác                   |
| `>`     | `>`      | `GT`           | Lớn hơn                |
| `<`     | `<`      | `LT`           | Nhỏ hơn                |
| `>=`    | `>=`     | `GTE`          | Lớn hơn hoặc bằng      |
| `<=`    | `<=`     | `LTE`          | Nhỏ hơn hoặc bằng      |

**Lưu ý:** Frontend/Client chỉ cần gửi ký tự toán học (cột "Database"). Hệ thống tự động chuyển đổi sang Enum nội bộ.

### Logic Gates (Toán tử logic)

Sử dụng cho trường `nextLogic` để kết nối giữa các điều kiện:

| Giá trị | Mô tả                                       | Ví dụ                                    |
| :------ | :------------------------------------------ | :--------------------------------------- |
| `AND`   | Cả hai điều kiện phải đúng (mặc định)      | Nhiệt độ > 30 **AND** Đèn đang bật      |
| `OR`    | Ít nhất một điều kiện phải đúng            | Nhiệt độ > 30 **OR** Độ ẩm > 80         |

**Lưu ý:** Nếu không truyền `nextLogic` hoặc truyền `null`, hệ thống mặc định sử dụng `AND`.

### Chi tiết cấu trúc `resourceParam` theo từng Data Source

#### 1. SYSTEM (Hệ thống thời gian)

Sử dụng để tạo điều kiện liên quan đến thời gian thực tế của hệ thống.

**Cấu trúc:** `{"property": "<tên_thuộc_tính>"}`

**Các property hỗ trợ:**

| Property       | Mô tả                                                                                     | Ví dụ giá trị so sánh            |
| :------------- | :---------------------------------------------------------------------------------------- | :------------------------------- |
| current_time   | Thời gian hiện tại trong ngày. Cách tính: `Giờ + (Phút / 60.0)`                         | `10.5` (10h30), `14.75` (14h45)  |
| day_of_week    | Thứ trong tuần                                                                            | `1` (Thứ 2) đến `7` (Chủ nhật)   |
| day_of_month   | Ngày trong tháng                                                                          | `1` đến `31`                     |

**Ví dụ:** Nếu thời gian hiện tại là sau 18h30
```json
{
  "sortOrder": 0,
  "dataSource": "SYSTEM",
  "resourceParam": {
    "property": "current_time"
  },
  "operator": ">=",
  "value": "18.5",
  "nextLogic": "AND"
}
```

#### 2. ROOM (Thông số phòng)

Sử dụng để lấy thông số trung bình/tổng hợp của căn phòng (dựa vào `roomId` trong Rule).

**Cấu trúc:** `{"property": "<tên_thuộc_tính>"}`

**Các property hỗ trợ:**

| Property        | Mô tả                                                    |
| :-------------- | :------------------------------------------------------- |
| avg_temperature | Nhiệt độ trung bình (`avgTempC`) của phòng              |
| sum_watt        | Tổng điện năng tiêu thụ (`sumWatt`) của phòng           |

**Ví dụ:** Nếu nhiệt độ trung bình của phòng > 30°C
```json
{
  "sortOrder": 1,
  "dataSource": "ROOM",
  "resourceParam": {
    "property": "avg_temperature"
  },
  "operator": ">",
  "value": "30",
  "nextLogic": "AND"
}
```

#### 3. DEVICE (Trạng thái Thiết bị)

Sử dụng để kiểm tra trạng thái của một thiết bị cụ thể trong hệ thống.

**Cấu trúc:** `{"category": "<loại_thiết_bị>", "deviceId": <id>, "property": "<tên_thuộc_tính>"}`

**Các category và property hỗ trợ:**

| Category      | Properties hỗ trợ                             |
| :------------ | :-------------------------------------------- |
| AIR_CONDITION | `power`, `temp`, `mode`, `fan_speed`, `swing` |
| LIGHT         | `power`, `level`                              |
| FAN           | `power`, `mode`, `speed`, `swing`, `light`    |

**Ví dụ:** Nếu Trạng thái nguồn (power) của Điều hòa (ID: 15) đang là ON
```json
{
  "sortOrder": 2,
  "dataSource": "DEVICE",
  "resourceParam": {
    "category": "AIR_CONDITION",
    "deviceId": 15,
    "property": "power"
  },
  "operator": "=",
  "value": "ON",
  "nextLogic": "OR"
}
```

#### 4. SENSOR (Trạng thái Cảm biến)

Sử dụng để kiểm tra giá trị trực tiếp ngay lúc đó của một cảm biến cụ thể.

**Cấu trúc:** `{"category": "<loại_cảm_biến>", "sensorId": <id>, "property": "<tên_thuộc_tính>"}`

**Các category và property hỗ trợ:**

| Category          | Property     | Mô tả                                              |
| :---------------- | :----------- | :------------------------------------------------- |
| TEMPERATURE       | temperature  | Lấy `currentValue` của cảm biến nhiệt độ          |
| POWER_CONSUMPTION | watt         | Lấy `currentWatt` của cảm biến điện năng tiêu thụ |

**Ví dụ:** Nếu giá trị watt của Cảm biến điện năng (ID: 9) lớn hơn 1000W
```json
{
  "sortOrder": 3,
  "dataSource": "SENSOR",
  "resourceParam": {
    "category": "POWER_CONSUMPTION",
    "sensorId": 9,
    "property": "watt"
  },
  "operator": ">",
  "value": "1000"
}
```

### Ví dụ Request Body phức tạp

Ngữ cảnh: "Vào thứ 2 hàng tuần (SYSTEM), nếu nhiệt độ phòng > 28°C (ROOM) VÀ Đèn số 5 đang bật (DEVICE), thì hãy bật Điều hòa số 101 ở chế độ COOL 24 độ."

```json
{
  "name": "Làm mát phòng làm việc Thứ 2",
  "priority": 1,
  "roomId": 10,
  "targetDeviceId": 101,
  "targetDeviceCategory": "AIR_CONDITION",
  "actionParams": {
    "power": "ON",
    "temp": 24,
    "mode": "COOL"
  },
  "conditions": [
    {
      "sortOrder": 0,
      "dataSource": "SYSTEM",
      "resourceParam": {
        "property": "day_of_week"
      },
      "operator": "=",
      "value": "1",
      "nextLogic": "AND"
    },
    {
      "sortOrder": 1,
      "dataSource": "ROOM",
      "resourceParam": {
        "property": "temperature"
      },
      "operator": ">",
      "value": "28",
      "nextLogic": "AND"
    },
    {
      "sortOrder": 2,
      "dataSource": "DEVICE",
      "resourceParam": {
        "category": "LIGHT",
        "deviceId": 5,
        "property": "power"
      },
      "operator": "=",
      "value": "ON"
    }
  ]
}
```

</details>