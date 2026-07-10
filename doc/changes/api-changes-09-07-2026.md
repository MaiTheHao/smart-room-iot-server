# API Changes - Sensor Metadata & Telemetry History API

## 1. Mục tiêu

Tài liệu này ghi nhận các thay đổi ở tầng RESTful API phục vụ cho hai nhóm tính năng mới của cảm biến (Sensor):
1. **Sensor Metadata API**: API thống nhất lấy danh sách cảm biến (`Temperature`, `PowerConsumption`) theo phòng, theo loại, đếm số lượng cảm biến trong phòng.
2. **Device Telemetry History API**: API hợp nhất lấy lịch sử đo đạc của cảm biến theo `sensorId` hoặc `naturalId`.

---

## 2. Danh sách API mới

| Method | Endpoint | Mô tả |
| :----- | :------- | :---- |
| **GET** | `/api/v1/rooms/{roomId}/sensors` | Lấy danh sách cảm biến của một phòng |
| **GET** | `/api/v1/rooms/{roomId}/sensors/count` | Đếm số lượng cảm biến trong một phòng |
| **GET** | `/api/v1/sensors/all` | Lấy tất cả cảm biến hệ thống |
| **GET** | `/api/v1/sensors/{sensorId}/history` | Lấy lịch sử đo đạc của cảm biến theo Sensor ID |
| **GET** | `/api/v1/sensors/natural/{naturalId}/history` | Lấy lịch sử đo đạc của cảm biến theo Natural ID |

---

## 3. Chi tiết API Contract & Tham số

### A. Nhóm Sensor Metadata

#### 1. Lấy danh sách cảm biến (`GET /api/v1/rooms/{roomId}/sensors` và `GET /api/v1/sensors/all`)
*   **Tham số Path (nếu lấy theo phòng):**
    *   `roomId` (Long, bắt buộc)
*   **Tham số Query:**
    *   `category` (string, không bắt buộc, nhận giá trị: `TEMPERATURE` | `POWER_CONSUMPTION`): Lọc theo loại cảm biến. Nếu bỏ trống, trả về danh sách gộp cả hai loại. Nếu truyền loại actuator (ví dụ: `LIGHT`, `FAN`, `AIR_CONDITION`), trả về lỗi `400 Bad Request`.

#### 2. Đếm số lượng cảm biến (`GET /api/v1/rooms/{roomId}/sensors/count`)
*   **Tham số Path:** `roomId` (Long, bắt buộc)
*   **Trả về:** Số lượng cảm biến đo đạc (Long).

---

### B. Nhóm Sensor Telemetry History

#### 1. Lấy lịch sử đo đạc cảm biến (`GET /api/v1/sensors/{sensorId}/history` và `GET /api/v1/sensors/natural/{naturalId}/history`)
*   **Tham số Path:**
    *   `sensorId` (Long, bắt buộc) hoặc `naturalId` (string, bắt buộc)
*   **Tham số Query (Bắt buộc - REQUIRED):**
    *   `category` (string, bắt buộc, nhận giá trị: `TEMPERATURE` | `POWER_CONSUMPTION`): Loại dữ liệu lịch sử muốn lấy.
    *   `from` (string ISO-8601 Instant, bắt buộc): Thời gian bắt đầu.
    *   `to` (string ISO-8601 Instant, bắt buộc): Thời gian kết thúc.
*   **Ràng buộc tham số và hành vi REST:**
    *   **Bắt buộc truyền `category`**: Không có giá trị mặc định và không tự động suy luận. Truyền thiếu hoặc sai loại cảm biến sẽ trả về `400 Bad Request` để tránh xung đột dữ liệu chéo bảng (khi sensorId/naturalId trùng nhau giữa các bảng cảm biến).
    *   **Giới hạn khoảng thời gian**: Thời gian truy vấn tối đa giới hạn trong vòng 365 ngày (nếu khoảng thời gian vượt quá, hệ thống tự động điều chỉnh cắt ngắn về 365 ngày tính từ thời điểm `to`).
    *   **Gom nhóm thời gian (Time-bucketing)**: Hệ thống tự động tính toán kích thước bucket (divisor) dựa trên khoảng thời gian (`from` - `to`) để tối ưu hóa hiệu năng tải.
