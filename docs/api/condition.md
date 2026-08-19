# Smart Room IoT - Condition API Documentation

Tài nguyên `Condition` đại diện cho các điều kiện đánh giá kích hoạt, được thiết kế đa hình (Polymorphic) và dùng chung cho các domain: `RULE`, `AUTOMATION`, `SYSTEM`,...

---

## 1. Cấu trúc Thuộc tính Condition

| Tên trường | Loại | Bắt buộc (Create) | Mô tả |
| :--- | :--- | :--- | :--- |
| `ownerCategory` | string (Enum) | Có | Loại đối tượng sở hữu (`RULE`, `AUTOMATION`, `SYSTEM`) |
| `ownerId` | string | Có | ID của đối tượng sở hữu (Rule ID, Automation ID...) |
| `sourceCategory` | string (Enum) | Có | Nguồn dữ liệu (`SYSTEM`, `ROOM`, `DEVICE`, `SENSOR`) |
| `sourceTargetId` | string | Có | ID nguồn (Ví dụ: sensorId, deviceId, roomId) |
| `sourceTargetType`| string (Enum) | Không | Phân loại thiết bị nguồn (`TEMPERATURE`, `LIGHT`, `FAN`, `AIR_CONDITION`...) |
| `property` | string | Có | Thuộc tính kiểm tra (Xem bảng chuẩn bên dưới) |
| `operator` | string (Enum) | Có | Toán tử (`EQ`, `NEQ`, `GT`, `LT`, `GTE`, `LTE`) |
| `value` | string | Có | Giá trị ngưỡng so sánh |
| `extraParams` | Json | Không | Tham số mở rộng dạng JSON |
| `sortOrder` | integer | Không | Thứ tự đánh giá (Mặc định: `0`) |
| `nextLogic` | string (Enum) | Không | Logic liên kết kế tiếp (`AND`, `OR`) |

### Danh mục `property` Chuẩn theo `sourceCategory`
* **`SYSTEM`**: `current_time`, `day_of_week`, `day_of_month`.
* **`ROOM`**: `avg_temperature`, `sum_watt`, `avg_humidity`, `avg_lux`, `avg_co2`, `max_co2`.
* **`SENSOR`**:
  * `TEMPERATURE` -> `temperature`
  * `POWER_CONSUMPTION` -> `watt`
  * `HUMIDITY` -> `humidity`
  * `SENSOR_CO2` -> `co2`
  * `SENSOR_LUX` -> `lux`
* **`DEVICE`**:
  * `LIGHT` -> `power`, `level`
  * `FAN` -> `power`, `speed`, `mode`, `swing`, `light`
  * `AIR_CONDITION` -> `power`, `temp`, `mode`, `fan_speed`, `swing`

---

## 2. Danh sách Endpoints

<details>
<summary><b>POST</b> <code>/api/v1/conditions</code> - Tạo mới Điều kiện</summary>

### Request Body
```json
{
  "ownerCategory": "RULE",
  "ownerId": "10",
  "sourceCategory": "SENSOR",
  "sourceTargetId": "1",
  "sourceTargetType": "TEMPERATURE",
  "property": "temperature",
  "operator": "GT",
  "value": "28",
  "sortOrder": 0,
  "nextLogic": "AND"
}
```

### Response (201 Created)
```json
{
  "status": 201,
  "message": "Created successfully",
  "data": {
    "id": 100,
    "ownerCategory": "RULE",
    "ownerId": "10",
    "sourceCategory": "SENSOR",
    "sourceTargetId": "1",
    "sourceTargetType": "TEMPERATURE",
    "property": "temperature",
    "operator": "GT",
    "value": "28",
    "sortOrder": 0,
    "nextLogic": "AND",
    "createdAt": "2026-03-26T15:00:00Z",
    "updatedAt": "2026-03-26T15:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/conditions/{id}</code> - Chi tiết Điều kiện</summary>

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 100,
    "ownerCategory": "RULE",
    "ownerId": "10",
    "sourceCategory": "SENSOR",
    "sourceTargetId": "1",
    "sourceTargetType": "TEMPERATURE",
    "property": "temperature",
    "operator": "GT",
    "value": "28",
    "sortOrder": 0,
    "nextLogic": "AND",
    "createdAt": "2026-03-26T15:00:00Z",
    "updatedAt": "2026-03-26T15:00:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>PATCH</b> <code>/api/v1/conditions/{id}</code> - Cập nhật Điều kiện</summary>

### Request Body
```json
{
  "value": "30",
  "operator": "GTE"
}
```

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": 100,
    "ownerCategory": "RULE",
    "ownerId": "10",
    "sourceCategory": "SENSOR",
    "sourceTargetId": "1",
    "sourceTargetType": "TEMPERATURE",
    "property": "temperature",
    "operator": "GTE",
    "value": "30",
    "sortOrder": 0,
    "nextLogic": "AND",
    "createdAt": "2026-03-26T15:00:00Z",
    "updatedAt": "2026-03-26T15:30:00Z"
  }
}
```
</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/conditions/{id}</code> - Xóa Điều kiện</summary>

### Response (204 No Content)
</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/conditions</code> - Tra cứu Điều kiện theo Tiêu chí</summary>

### Query Parameters
* Lọc theo Owner: `?ownerCategory=RULE&ownerId=10`
* Lọc theo Source: `?sourceCategory=SENSOR&sourceTargetId=1&sourceTargetType=TEMPERATURE`

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
      "operator": "GT",
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
<summary><b>DELETE</b> <code>/api/v1/conditions/by-owner</code> - Xóa tất cả Điều kiện theo Owner</summary>

### Query Parameters
* `ownerCategory` (Bắt buộc): `RULE`, `AUTOMATION`, `SYSTEM`
* `ownerId` (Bắt buộc): ID của đối tượng

### Response (200 OK)
```json
{
  "status": 200,
  "message": "Success",
  "data": 2
}
```
</details>
