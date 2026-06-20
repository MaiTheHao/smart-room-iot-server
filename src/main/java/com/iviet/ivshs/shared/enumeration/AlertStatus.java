package com.iviet.ivshs.shared.enumeration;

/**
 * Trạng thái vòng đời của một AlertInstance.
 * ACTIVE       → Cảnh báo vừa được kích hoạt, chưa được xử lý.
 * ACKNOWLEDGED → Đã có người xác nhận đã biết về cảnh báo này.
 * RESOLVED     → Đã được giải quyết (thủ công hoặc tự động bởi hệ thống).
 * Được lưu dạng STRING trong cột VARCHAR(50) của DB.
 */
public enum AlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED
}
