package com.iviet.ivshs.enumeration;

public enum SysGroupEnumV1 {
	G_ADMIN,
	G_MANAGER,
	G_USER,
	G_HARDWARE_GATEWAY;

	@Override
	public String toString() {
		return name();
	}
}
