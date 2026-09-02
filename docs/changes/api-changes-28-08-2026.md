# Lịch sử thay đổi API

## 2026-08-28 — Bổ sung Room Event Configuration & Sensor Event API

Bổ sung API quản lý cấu hình sự kiện phòng (`RoomEvent`) và API tiếp nhận sự kiện từ cảm biến (`SensorEvent`):

---

### 1. Bổ sung Room Event Configuration API (`/api/v1/rooms/{roomId}/events`)

Cung cấp CRUD và Sub-resource helpers (`conditions`, `actions`) để quản lý cấu hình sự kiện theo phòng:

| Method | URL | Trạng thái | Mô tả |
| :--- | :--- | :--- | :--- |
| POST | `/api/v1/rooms/{roomId}/events` | **Mới** | Khởi tạo cấu hình sự kiện mới cho phòng. |
| GET | `/api/v1/rooms/{roomId}/events` | **Mới** | Lấy danh sách cấu hình sự kiện trong phòng. |
| GET | `/api/v1/rooms/{roomId}/events/{configId}` | **Mới** | Lấy chi tiết cấu hình sự kiện phòng theo ID. |
| PUT | `/api/v1/rooms/{roomId}/events/{configId}` | **Mới** | Cập nhật cấu hình sự kiện phòng (`isActive`, `cooldownSeconds`). |
| DELETE | `/api/v1/rooms/{roomId}/events/{configId}` | **Mới** | Xóa cấu hình sự kiện phòng (tự động xóa Condition & Action liên thuộc). |
| GET | `/api/v1/rooms/{roomId}/events/{configId}/conditions` | **Mới** | Lấy danh sách điều kiện lọc của sự kiện phòng. |
| POST | `/api/v1/rooms/{roomId}/events/{configId}/conditions` | **Mới** | Thêm điều kiện lọc cho sự kiện phòng. |
| PUT | `/api/v1/rooms/{roomId}/events/{configId}/conditions` | **Mới** | Bulk Replace danh sách điều kiện lọc của sự kiện phòng. |
| GET | `/api/v1/rooms/{roomId}/events/{configId}/actions` | **Mới** | Lấy danh sách hành động thực thi của sự kiện phòng. |
| POST | `/api/v1/rooms/{roomId}/events/{configId}/actions` | **Mới** | Thêm hành động thực thi cho sự kiện phòng. |
| PUT | `/api/v1/rooms/{roomId}/events/{configId}/actions` | **Mới** | Bulk Replace danh sách hành động thực thi của sự kiện phòng. |

---

### 2. Bổ sung Sensor Event Ingestion API (`/api/v1/sensors/{naturalId}/event`)

Endpoint nhận dữ liệu sự kiện từ cảm biến:

| Method | URL | Trạng thái | Mô tả |
| :--- | :--- | :--- | :--- |
| POST | `/api/v1/sensors/{naturalId}/event` | **Mới** | Gửi sự kiện từ cảm biến theo mã tự nhiên (`naturalId`). |

- **Hỗ trợ cảm biến**: `MOTION_DETECTOR` (payload `{"motion_detected": true/false}`).

---

### 3. Cập nhật Tài liệu

- [Room Event API](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/docs/api/room_event.md)
- [Sensor Event API](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/docs/api/sensor_event.md)
- [API Index](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/docs/api/README.md)
