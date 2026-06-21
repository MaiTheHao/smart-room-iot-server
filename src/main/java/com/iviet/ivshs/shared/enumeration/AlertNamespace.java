package com.iviet.ivshs.shared.enumeration;

/**
 * Phân vùng nghiệp vụ của Alert Config.
 * RULE = vi phạm điều kiện quy tắc cảm biến
 * GATEWAY = cổng phần cứng ngoại tuyến
 * SYSTEM = lỗi hệ thống nội bộ
 */
public enum AlertNamespace {
    RULE,
    GATEWAY,
    SYSTEM;
}
