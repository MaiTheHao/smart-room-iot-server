package com.iviet.ivshs.service.notification.strategy;

import com.iviet.ivshs.service.notification.channel.NotificationChannel;
import com.iviet.ivshs.service.notification.request.NotificationRequest;

/**
 * Strategy interface cho một kênh gửi thông báo cụ thể.
 *
 * Khác với phiên bản dùng supports(String channel):
 * - getChannel() trả về NotificationChannel enum → type-safe, không có magic string
 * - send() nhận NotificationRequest (Value Object) → không cần sửa signature khi thêm field
 *
 * Để thêm kênh mới (vd: Zalo OA): chỉ cần tạo @Component implements NotificationStrategy.
 * NotificationServiceImpl, NotificationStrategyRegistry KHÔNG cần thay đổi (OCP).
 *
 * Spring DI tự động collect tất cả @Component của interface này.
 */
public interface NotificationStrategy {

    /**
     * Trả về channel mà strategy này xử lý.
     * NotificationStrategyRegistry dùng giá trị này làm Map key.
     * Mỗi channel chỉ được có DUY NHẤT 1 strategy — registry sẽ fail-fast nếu trùng.
     */
    NotificationChannel getChannel();

    /**
     * Gửi thông báo qua kênh cụ thể của strategy này.
     *
     * @param request Value Object chứa recipients, title, body, data.
     *                Mỗi strategy chỉ đọc các field mình cần:
     *                - FcmStrategy: recipients → clientDevices → FCM tokens
     *                - EmailStrategy: recipients → username/email
     *                - SmsStrategy: recipients → phone number
     */
    void send(NotificationRequest request);
}
