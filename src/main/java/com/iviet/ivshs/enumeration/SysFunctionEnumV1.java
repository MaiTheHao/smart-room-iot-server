package com.iviet.ivshs.enumeration;

public enum SysFunctionEnumV1 {
	F_MANAGE_CLIENT,
	F_MANAGE_FLOOR,
	F_MANAGE_ROOM,
	F_MANAGE_DEVICE,
	F_MANAGE_ALL,
	F_ACCESS_FLOOR_ALL,
	F_ACCESS_ROOM_ALL,
	F_ACCESS_FLOOR_F00,
	F_ACCESS_ROOM_R_F00_Server;

	@Override
	public String toString() {
		switch (this) {
			case F_ACCESS_ROOM_R_F00_Server: return "F_ACCESS_ROOM_R-F00-Server";
			default: return name();
		}
	}
}
