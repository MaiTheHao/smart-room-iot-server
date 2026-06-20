package com.iviet.ivshs.shared.enumeration;

/**
 * System Function Enumeration
 * 
 * Enum này định nghĩa các chứng năng hệ thống chung cho việc phân quyền truy
 * cập.
 */
public enum SysFunctionEnum {
	// ========== MANAGEMENT FUNCTIONS ==========
	F_MANAGE_CLIENT("F_MANAGE_CLIENT"),
	F_MANAGE_FLOOR("F_MANAGE_FLOOR"),
	F_MANAGE_ROOM("F_MANAGE_ROOM"),
	F_MANAGE_DEVICE("F_MANAGE_DEVICE"),
	F_MANAGE_ALL("F_MANAGE_ALL"),
	F_MANAGE_SOME("F_MANAGE_SOME"),
	F_MANAGE_FUNCTION("F_MANAGE_FUNCTION"),
	F_MANAGE_GROUP("F_MANAGE_GROUP"),
	F_MANAGE_ROLE("F_MANAGE_ROLE"),
	F_MANAGE_AUTOMATION("F_MANAGE_AUTOMATION"),
	F_MANAGE_RULE("F_MANAGE_RULE"),

	// ========== ACCESS FUNCTIONS ==========
	F_ACCESS_FLOOR_ALL("F_ACCESS_FLOOR_ALL"),
	F_ACCESS_ROOM_ALL("F_ACCESS_ROOM_ALL"),

	// ========== ALERT ACCESS FUNCTIONS ==========
	F_ACCESS_ALERT_ALL("F_ACCESS_ALERT_ALL"),       // G_ADMIN: xem toàn bộ alerts
	F_ACCESS_ALERT_GROUP("F_ACCESS_ALERT_GROUP"),   // G_MAINTENANCE: xem alerts của group
	F_ACCESS_ALERT_OWN("F_ACCESS_ALERT_OWN");       // G_USER: chỉ xem "My Alerts"

	private String code;

	SysFunctionEnum(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	@Override
	public String toString() {
		return this.getCode();
	}
}
