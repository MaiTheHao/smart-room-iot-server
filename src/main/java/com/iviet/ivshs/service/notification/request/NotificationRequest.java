package com.iviet.ivshs.service.notification.request;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.service.notification.channel.NotificationChannel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Value Object encapsulating toàn bộ context của một yêu cầu gửi thông báo.
 *
 * Ưu điểm so với truyền 5 tham số rời:
 * - OCP: Thêm field mới (vd: priority, ttl) chỉ sửa class này,
 *   KHÔNG sửa NotificationService interface hay tất cả call-site như AlertServiceImpl.
 * - Readability: Dùng Builder → self-documenting, không nhầm thứ tự tham số.
 * - Immutability: Lombok @Builder tạo object bất biến sau khi build().
 *
 * Usage example:
 * <pre>
 *   NotificationRequest request = NotificationRequest.builder()
 *       .recipients(clients)
 *       .channels(List.of(NotificationChannel.PUSH, NotificationChannel.EMAIL))
 *       .title("Cảnh báo nhiệt độ")
 *       .body("Nhiệt độ phòng 101 đạt 42°C")
 *       .data(Map.of("type", "ALERT_TRIGGERED", "entityId", "123"))
 *       .build();
 * </pre>
 */
@Getter
@Builder
public class NotificationRequest {

    /**
     * Tập hợp Client nhận thông báo.
     * QUAN TRỌNG: clientDevices phải được load (không LAZY) trước khi build request,
     * để FcmNotificationStrategy có thể đọc FCM tokens mà không cần transaction.
     */
    @NonNull
    private final Set<Client> recipients;

    /**
     * Danh sách các kênh cần gửi.
     * Mỗi channel sẽ được dispatch tới NotificationStrategy tương ứng.
     */
    @NonNull
    private final List<NotificationChannel> channels;

    /** Tiêu đề hiển thị trên thiết bị người dùng. */
    @NonNull
    private final String title;

    /** Nội dung hiển thị trên thiết bị người dùng. */
    @NonNull
    private final String body;

    /**
     * Extra data payload cho logic xử lý phía mobile app.
     * TẤT CẢ values phải là String theo FCM v1 API spec.
     * Ví dụ: {"type": "ALERT_TRIGGERED", "entityId": "123", "deepLink": "smartroom://alert/123"}
     */
    private final Map<String, String> data;
}
