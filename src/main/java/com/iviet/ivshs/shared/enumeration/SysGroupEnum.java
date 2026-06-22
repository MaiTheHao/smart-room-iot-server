package com.iviet.ivshs.shared.enumeration;

/**
 * System Group Enumeration
 * 
 * Enum này định nghĩa các nhóm người dùng hệ thống chung cho việc phân quyền
 * truy cập.
 */
public enum SysGroupEnum {
	G_ADMIN("G_ADMIN"),
	G_USER("G_USER"),
	G_MANAGER("G_MANAGER"),
	G_MAINTENANCE("G_MAINTENANCE"),
	G_HARDWARE_GATEWAY("G_HARDWARE_GATEWAY"),
	G_ALERT("G_ALERT");

	private String code;

	SysGroupEnum(String code) {
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
