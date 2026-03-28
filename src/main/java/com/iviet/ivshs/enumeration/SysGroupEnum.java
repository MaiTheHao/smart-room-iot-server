package com.iviet.ivshs.enumeration;

/**
 * System Group Enumeration
 * 
 * Enum này định nghĩa các nhóm người dùng hệ thống chung cho việc phân quyền truy cập.
 */
public enum SysGroupEnum {
	G_ADMIN("G_ADMIN"),
	G_USER("G_USER"),
	G_MANAGER("G_MANAGER"),
	G_HARDWARE_GATEWAY("G_HARDWARE_GATEWAY");

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
