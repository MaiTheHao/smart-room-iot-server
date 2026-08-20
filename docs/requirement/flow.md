## Tổng quan luồng


1. **Nhận & định tuyến sự kiện**: `POST` event → định tuyến theo category qua [Event Telemetry Strategy](../../src/main/java/com/iviet/ivshs/service/registry/EventTelemetryStrategyRegistry.java) → validate + cập nhật sensor → ghi metric [Motion Detect Metric Service](../../src/main/java/com/iviet/ivshs/service/impl/MotionMetricServiceImpl.java).
2. **Phát sự kiện nội bộ**: Publish `RoomMotionDetectedEvent` *(trong transaction)*.
3. **Kiểm tra cấu hình phòng**: Sau `COMMIT` → tìm config (`roomId` + `eventCode`) → kiểm tra `active` / `cooldown`. __ TODO
4. **Thực thi hành động**: Đánh dấu `lastTriggeredAt` → thực thi actions *(điều khiển thiết bị song song)*. __ TODO
5. **Kích hoạt cảnh báo**: Kích hoạt alerts → publish `AlertNotificationEvent`. __ TODO
6. **Gửi thông báo**: Sau `COMMIT` → gửi FCM notification tới các thiết bị nhận. __ TODO

## Sequence diagram

```mermaid
sequenceDiagram
    autonumber
    participant Sensor as "Cảm biến (Thiết bị IoT)"
    participant Api as "API nhận sự kiện"
    participant Router as "Bộ định tuyến loại cảm biến"
    participant Motion as "Xử lý dữ liệu chuyển động"
    participant Db as "Cơ sở dữ liệu"
    participant Handler as "Xử lý sự kiện phòng"
    participant Action as "Thực thi hành động"
    participant Device as "Thiết bị đích"
    participant Alert as "Kích hoạt cảnh báo"
    participant Notify as "Gửi thông báo"

    Sensor->>Api: POST /v1/sensors/{naturalId}/event<br/>(category, data)
    Api->>Router: Chuyển request theo loại cảm biến
    Router->>Motion: Định tuyến tới bộ xử lý chuyển động
    Motion->>Db: Tìm cảm biến theo naturalId
    Db-->>Motion: Thông tin cảm biến (hoặc không tìm thấy)
    Motion->>Db: Cập nhật trạng thái cảm biến
    Motion->>Db: Ghi metric thời gian thực
    alt Có chuyển động và cảm biến gắn với phòng
        Motion->>Handler: Phát sự kiện chuyển động (roomId, payload)
        Note over Handler: Chạy bất đồng bộ sau khi giao dịch hoàn tất
        Handler->>Db: Tìm cấu hình sự kiện phòng
        alt Không có cấu hình hoặc tắt hoặc trong cooldown
            Handler->>Handler: Bỏ qua
        else Cấu hình hợp lệ
            Handler->>Db: Đánh dấu thời điểm kích hoạt gần nhất
            Handler->>Action: Thực thi các hành động đã cấu hình
            Action->>Device: Điều khiển thiết bị đích
            Device-->>Action: Kết quả điều khiển
            Handler->>Alert: Kích hoạt các cảnh báo đã cấu hình
            alt Đã có cảnh báo đang mở
                Alert->>Db: Kiểm tra cooldown và tăng số lần kích hoạt
            else Chưa có cảnh báo
                Alert->>Db: Tạo cảnh báo mới
            end
            Alert->>Db: Ghi nhật ký cảnh báo
            Alert->>Notify: Phát sự kiện thông báo
            Note over Notify: Chạy bất đồng bộ sau khi giao dịch hoàn tất
            Notify->>Db: Tìm nhóm và thiết bị nhận
            Notify->>Notify: Gửi thông báo qua kênh cấu hình
        end
    end
    Api-->>Sensor: 200 OK (xử lý thành công)
```

## Flow chi tiết (nghiệp vụ, input → output)

**Giai đoạn 1 — Nhận & xử lý sự kiện cảm biến (đồng bộ, trong 1 transaction)**

| # | Bước | Input | Output |
|---|---|---|---|
| 1 | Nhận sự kiện từ cảm biến | `naturalId` (mã cảm biến), body `{category, data}` | Request hợp lệ, nếu thiếu category/data → lỗi 400 |
| 2 | Định tuyến theo loại cảm biến | `category` | Tìm được bộ xử lý tương ứng; loại không hỗ trợ → lỗi |
| 3 | Kiểm tra dữ liệu payload | `data` (JSON) | Trường `motion_detected` phải là boolean, sai format → lỗi |
| 4 | Tìm cảm biến | `naturalId` | Thông tin cảm biến; không tồn tại → lỗi 404 |
| 5 | Cập nhật trạng thái cảm biến | Trạng thái chuyển động mới, thời điểm | Cảm biến đã lưu (currentMotion, lastEventAt) |
| 6 | Ghi metric thời gian thực | Id cảm biến, trạng thái, thời điểm | Bản ghi `MotionMetric` |
| 7 | Phát sự kiện phòng | `roomId`, `naturalId`, payload, timestamp | Sự kiện chuyển động được phát **chỉ khi** có chuyển động **và** cảm biến gắn với phòng |

**Giai đoạn 2 — Xử lý sự kiện phòng (bất đồng bộ, sau COMMIT)**

| # | Bước | Input | Output |
|---|---|---|---|
| 8 | Tìm cấu hình sự kiện | `roomId` + mã sự kiện (MOTION_DETECTED) | Cấu hình sự kiện phòng; không có → dừng |
| 9 | Kiểm tra trạng thái & cooldown | `isActive`, `cooldownSeconds`, `lastTriggeredAt` | Quyết định xử lý tiếp hoặc bỏ qua |
| 10 | Đánh dấu đã kích hoạt | Cấu hình sự kiện | `lastTriggeredAt` = thời điểm hiện tại |
| 11 | Thực thi các hành động | Id cấu hình sự kiện | Danh sách hành động, sắp xếp theo thứ tự, chạy song song → điều khiển thiết bị đích, trả về kết quả từng hành động |
| 12 | Kích hoạt các cảnh báo | Id cấu hình sự kiện + dữ liệu sự kiện (room, eventCode, timestamp, payload) | Cảnh báo mới (hoặc tăng số lần kích hoạt nếu đang mở, sau khi qua cooldown), ghi nhật ký incident |

**Giai đoạn 3 — Gửi thông báo (bất đồng bộ, sau COMMIT)**

| # | Bước | Input | Output |
|---|---|---|---|
| 13 | Tìm người nhận | Id cảnh báo | Nhóm nhận → các thiết bị (client) thuộc nhóm |
| 14 | Gửi thông báo | Thiết bị nhận, kênh cấu hình, tiêu đề/nội dung, dữ liệu FCM | Thông báo FCM đến các thiết bị (kèm deepLink tới màn hình chi tiết alert) |

**Điểm cần lưu ý:** Controller trả `200 OK` ngay sau khi transaction của giai đoạn 1 commit, **không chờ** giai đoạn 2-3 — các listener dùng `@Async` + `@TransactionalEventListener