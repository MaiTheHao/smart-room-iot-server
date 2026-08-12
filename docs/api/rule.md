# Smart Room IoT - Rule Engine API Documentation

Hệ thống tự động hóa dựa trên chu kỳ giây độc lập, hỗ trợ đa hành động và cấu hình Interval riêng biệt cho từng quy tắc.

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

## Cấu trúc Tham số Cấu hình theo Đối Tượng

Tham số cấu hình chia làm 2 loại:
*   **`resourceParam` (Dùng trong Điều kiện - Condition):** Các thuộc tính để hệ thống lấy trạng thái và so sánh.
*   **`actionParams` (Dùng trong Hành động - Action):** Các lệnh điều khiển thiết bị đích. *(SYSTEM, ROOM, SENSOR chỉ hỗ trợ đọc, không có actionParams).*

### 1. Data Source: SYSTEM (Hệ thống)
**Dùng cho Điều kiện (`resourceParam`):**
*   `property` (String - Bắt buộc): Thuộc tính cần lấy. Bao gồm:
    *   `current_time`: Thời gian hiện tại theo giờ thập phân (ví dụ: `14.5` là 14h30) theo UTC.
    *   `day_of_week`: Ngày trong tuần (Từ `1` đến `7` tương ứng Thứ 2 đến Chủ nhật) theo UTC.
    *   `day_of_month`: Ngày trong tháng (Từ `1` đến `31`) theo UTC.

**Dùng cho Hành động (`actionParams`):**
*   *(Không hỗ trợ điều khiển)*

### 2. Data Source: ROOM (Phòng)
**Dùng cho Điều kiện (`resourceParam`):**
*   `roomId` (Long - Bắt buộc): ID của phòng cần kiểm tra.
*   `property` (String - Bắt buộc): Thuộc tính cần kiểm tra. Bao gồm:
    *   `avg_temperature`: Nhiệt độ trung bình trong khoảng thời gian cấu hình hệ thống (`lookbackMinutes`, cấu hình global phía server).
    *   `sum_watt`: Tổng điện năng tiêu thụ trong khoảng thời gian cấu hình hệ thống (`lookbackMinutes`, cấu hình global phía server).
    *   `avg_humidity`: Độ ẩm trung vị (Median) trong phòng, tổng hợp từ tất cả cảm biến độ ẩm đang hoạt động.
    *   `avg_lux`: Cường độ ánh sáng trung vị (Median) trong phòng, tổng hợp từ tất cả cảm biến ánh sáng đang hoạt động.
    *   `avg_co2`: Nồng độ CO2 trung bình (Mean) trong phòng, tổng hợp từ tất cả cảm biến CO2 đang hoạt động.
    *   `max_co2`: Nồng độ CO2 lớn nhất (Max) trong phòng, tổng hợp từ tất cả cảm biến CO2 đang hoạt động. Dùng cho Automation kích hoạt thiết bị thông gió.

**Dùng cho Hành động (`actionParams`):**
*   *(Không hỗ trợ điều khiển)*

### 3. Data Source: SENSOR (Cảm biến)
**Dùng cho Điều kiện (`resourceParam`):**
*   `category` (String - Bắt buộc): Loại cảm biến (`TEMPERATURE`, `POWER_CONSUMPTION`, `HUMIDITY`, `SENSOR_CO2`, `SENSOR_LUX`).
*   `sensorId` (Long - Bắt buộc): ID của cảm biến.
*   `property` (String - Bắt buộc): Tùy thuộc vào `category`:
    *   Với `TEMPERATURE`: `temperature` (Nhiệt độ hiện tại).
    *   Với `POWER_CONSUMPTION`: `watt` (Công suất tiêu thụ hiện tại).
    *   Với `HUMIDITY`: `humidity` (Độ ẩm hiện tại).
    *   Với `SENSOR_CO2`: `co2` (Nồng độ CO2 hiện tại).
    *   Với `SENSOR_LUX`: `lux` (Cường độ ánh sáng hiện tại).

**Dùng cho Hành động (`actionParams`):**
*   *(Không hỗ trợ điều khiển)*

### 4. Data Source: DEVICE (Thiết bị)
Đối với thiết bị, cần cấu hình cả `resourceParam` (nếu dùng làm điều kiện) và `actionParams` (nếu dùng làm hành động).

#### 4.1. Thiết bị chiếu sáng (LIGHT)
*   **Dùng cho Điều kiện (`resourceParam`):**
    *   `category`: `LIGHT`
    *   `deviceId`: ID của thiết bị
    *   `property`: `level`, `power`
*   **Dùng cho Hành động (`actionParams`):**
    *   `power` (String - Không bắt buộc): `ON`, `OFF`
    *   `level` (Integer - Không bắt buộc): Giá trị độ sáng từ `0` đến `100`

#### 4.2. Quạt (FAN)
*   **Dùng cho Điều kiện (`resourceParam`):**
    *   `category`: `FAN`
    *   `deviceId`: ID của thiết bị
    *   `property`: `power`, `speed`, `mode`, `swing`, `light`
*   **Dùng cho Hành động (`actionParams`):**
    *   `power` (String - Không bắt buộc): `ON`, `OFF`
    *   `mode` (String - Không bắt buộc): `COOL`, `HEAT`, `DRY`, `FAN`, `AUTO`, `NORMAL`, `SLEEP`, `NATURAL`
    *   `speed` (Integer - Không bắt buộc): Tốc độ quạt từ `1` đến `3`
    *   `swing` (String - Không bắt buộc): `ON`, `OFF`, `AUTO`, `HORIZONTAL`, `VERTICAL`
    *   `light` (String - Không bắt buộc): `ON`, `OFF`

#### 4.3. Điều hòa không khí (AIR_CONDITION)
*   **Dùng cho Điều kiện (`resourceParam`):**
    *   `category`: `AIR_CONDITION`
    *   `deviceId`: ID của thiết bị
    *   `property`: `power`, `temp`, `mode`, `fan_speed`, `swing`
*   **Dùng cho Hành động (`actionParams`):**
    *   `power` (String - Không bắt buộc): `ON`, `OFF`
    *   `temperature` (Integer - Không bắt buộc): Nhiệt độ thiết lập từ `16` đến `32`
    *   `mode` (String - Không bắt buộc): `COOL`, `HEAT`, `DRY`, `FAN`, `AUTO`, `NORMAL`, `SLEEP`, `NATURAL`
    *   `fanSpeed` (Integer - Không bắt buộc): Tốc độ gió từ `0` đến `5`
    *   `swing` (String - Không bắt buộc): `ON`, `OFF`, `AUTO`, `HORIZONTAL`, `VERTICAL`
    *   `duration` (Integer - Không bắt buộc): Thời gian chạy (phút)

> [!WARNING]
> Bất kỳ thiết bị thuộc loại nào khác (ví dụ: các cảm biến chỉ đọc như `TEMPERATURE`, `POWER_CONSUMPTION`) nếu được gán vào phần Action (`targetDeviceCategory`) sẽ bị từ chối với lỗi `400 Bad Request` (ví dụ: `Unsupported category: TEMPERATURE`).

---

## Các hằng số (Enums)

### RuleDataSource (Data Source)

| Giá trị | Mô tả |
| :--- | :--- |
| SYSTEM | Dữ liệu từ hệ thống |
| ROOM | Dữ liệu từ trạng thái phòng |
| DEVICE | Dữ liệu từ trạng thái thiết bị |
| SENSOR | Dữ liệu từ cảm biến |

### DeviceCategory (Phân loại thiết bị / cảm biến)

| Giá trị | Mô tả |
| :--- | :--- |
| LIGHT | Thiết bị chiếu sáng |
| AIR_CONDITION | Điều hòa không khí |
| FAN | Quạt |
| TEMPERATURE | Cảm biến nhiệt độ (chỉ đọc) |
| POWER_CONSUMPTION | Cảm biến điện năng tiêu thụ (chỉ đọc) |
| HUMIDITY | Cảm biến độ ẩm (chỉ đọc) |
| SENSOR_CO2 | Cảm biến CO2 (chỉ đọc) |
| SENSOR_LUX | Cảm biến ánh sáng (chỉ đọc) |

### ActuatorPower (Trạng thái nguồn điều khiển)

| Giá trị | Mô tả |
| :--- | :--- |
| ON | Bật thiết bị |
| OFF | Tắt thiết bị |

### ActuatorMode (Chế độ quạt / điều hòa)

| Giá trị | Mô tả |
| :--- | :--- |
| COOL | Chế độ làm mát |
| HEAT | Chế độ làm ấm |
| DRY | Chế độ hút ẩm |
| FAN | Chế độ quạt gió |
| AUTO | Chế độ tự động |
| NORMAL | Chế độ bình thường |
| SLEEP | Chế độ ngủ |
| NATURAL | Chế độ gió tự nhiên |

### ActuatorSwing (Chế độ hướng gió/quay)

| Giá trị | Mô tả |
| :--- | :--- |
| ON | Bật quay/đảo gió |
| OFF | Tắt quay/đảo gió |
| AUTO | Tự động đảo gió |
| HORIZONTAL | Đảo gió theo chiều ngang |
| VERTICAL | Đảo gió theo chiều dọc |

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

---
