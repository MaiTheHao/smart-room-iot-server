package com.iviet.ivshs.shared.enumeration;

/**
 * Loại hành động ghi vào alert incident log. TRIGGERED = lần kích hoạt đầu tiên RE_TRIGGERED = lặp lại trong cooldown
 * (không gửi FCM) ACKNOWLEDGED = user xác nhận RESOLVED = user resolve thủ công AUTO_RESOLVED = hệ thống tự động
 * resolve
 */
public enum AlertActionType {
    TRIGGERED, RE_TRIGGERED, ACKNOWLEDGED, RESOLVED, AUTO_RESOLVED;
}
