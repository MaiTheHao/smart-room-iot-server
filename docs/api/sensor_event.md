# Sensor Event Module

## API tiếp nhận sự kiện từ cảm biến

API dùng để tiếp nhận và ghi nhận các sự kiện phát sinh từ cảm biến về hệ thống (ví dụ: trạng thái phát hiện chuyển động).

---

<details>
<summary><b>POST</b> <code>/api/v1/sensors/{naturalId}/event</code> - Gửi sự kiện cảm biến</summary>

> Gửi dữ liệu sự kiện từ cảm biến lên server theo mã tự nhiên (`naturalId`).

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| `naturalId` | string | Mã tự nhiên của cảm biến (ví dụ: `MOTION_P101_01`) | Có |

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `category` | string (Enum) | Có | Loại cảm biến (`MOTION_DETECTOR`) |
| `data` | Object (JSON) | Có | Dữ liệu chi tiết của sự kiện |

#### Chi tiết Payload theo từng loại cảm biến:

- **`MOTION_DETECTOR`**:
  - `motion_detected` (boolean, bắt buộc): Trạng thái phát hiện chuyển động (`true` nếu có chuyển động, `false` nếu không có chuyển động).

### Request Example

```json
{
  "category": "MOTION_DETECTOR",
  "data": {
    "motion_detected": true
  }
}
```

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Success",
  "data": null,
  "timestamp": "2026-08-28T10:00:00Z"
}
```

### Error Responses

- **400 Bad Request**: Khi thiếu `category`, `data`, hoặc payload `data` không đúng định dạng yêu cầu.
  ```json
  {
    "status": 400,
    "message": "Field 'motion_detected' must be a boolean",
    "data": null,
    "timestamp": "2026-08-28T10:00:00Z"
  }
  ```
- **404 Not Found**: Khi không tìm thấy cảm biến với `naturalId` tương ứng.
  ```json
  {
    "status": 404,
    "message": "Motion detector not found with naturalId: MOTION_P101_01",
    "data": null,
    "timestamp": "2026-08-28T10:00:00Z"
  }
  ```

</details>

<br>
