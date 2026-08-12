# Lịch sử thay đổi API

## 2026-08-12 — Hoàn thiện Sensor Metadata Domain (phân trang + tra cứu theo ID)

Hoàn thiện domain **Sensor Metadata** theo hướng API đọc thống nhất cho cả 5 loại cảm biến (`TEMPERATURE`, `POWER_CONSUMPTION`, `HUMIDITY`, `SENSOR_CO2`, `SENSOR_LUX`), hỗ trợ phân trang và tra cứu theo `id`/`naturalId`. Đây là bước chuẩn bị cho kịch bản hệ thống chỉ còn **read-only** (CRUD sẽ được triển khai qua hệ thống update riêng), tiến tới dỡ bỏ các tài liệu `temperature.md` và `power_consumption.md`.

### Endpoints

| Method | URL | Trạng thái | Mô tả |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/rooms/{roomId}/sensors?category&page&size` | Cập nhật | Danh sách cảm biến theo phòng, thêm phân trang. Response chuyển từ `List` → `PaginatedResponse`. |
| GET | `/api/v1/rooms/{roomId}/sensors/count` | Giữ nguyên | Đếm số lượng cảm biến theo phòng. |
| GET | `/api/v1/sensors?category&page&size` | **Mới** | Canonical endpoint lấy tất cả cảm biến, phân trang. |
| GET | `/api/v1/sensors/all?category&page&size` | **Deprecated alias** | Giữ để tương thích ngược, trỏ cùng handler với `/api/v1/sensors`. |
| GET | `/api/v1/sensors/{sensorId}?category=` | **Mới** | Tra cứu cảm biến theo ID. `category` **bắt buộc** (ID có thể trùng giữa các bảng). |
| GET | `/api/v1/sensors/natural/{naturalId}?category=` | **Mới** | Tra cứu cảm biến theo naturalId. `category` **bắt buộc**. |

- Mặc định phân trang: `page=0`, `size=20`.

### Tài liệu

- `docs/api/sensor_metadata.md` — cập nhật theo scheme mới (pagination, by-ID, by-naturalId, alias).
- `docs/api/sensor_telemetry.md` — thêm cross-reference tới metadata endpoints để tra cứu cảm biến.
- `docs/api/temperature.md`, `docs/api/power_consumption.md` — **chưa thay đổi**; vẫn còn hiệu lực cho các endpoint CRUD/read per-type hiện tại. Sẽ được dỡ bỏ/gộp khi hệ thống update được triển khai và endpoint per-type bị gỡ.
