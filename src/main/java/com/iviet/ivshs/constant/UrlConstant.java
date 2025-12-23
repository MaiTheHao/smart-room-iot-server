package com.iviet.ivshs.constant;

public class UrlConstant {
	
	public static final String URL_C_HTTP = "http://";
	public static final String URL_C_SATELLITE = "/smrs";
	
	/* --- Start URL for Controller ---------------------------------------------------------------- */ 
	
	// Light
	public static final String URL_C_LIGHT = "/light";
	public static final String URL_C_LOAD_ALL_LIGHT = "/load-all-light";
	public static final String URL_C_CONTROL_ALL_LIGHT = "/control-all-light";
	public static final String URL_C_CONTROL_LIGHT_STATE_LEVEL = "/control-light-state-level";
	
	public static final String URL_C_FAN = "/fan";
	public static final String URL_C_LOAD_ALL_FAN = "/load-all-fan";
	public static final String URL_C_CONTROL_ALL_FAN_ROTATE = "/control-all-fan-rotate";
	public static final String URL_C_CONTROL_FAN_ROTATE = "/control-fan-rotate";
	public static final String URL_C_CONTROL_ALL_FAN_LEVEL = "/control-all-fan-level";
	public static final String URL_C_CONTROL_FAN_LEVEL = "/control-fan-level";
	
	public static final String URL_C_DOOR = "/door";
	public static final String URL_C_LOAD_ALL_DOOR = "/load-all-door";
	public static final String URL_C_CONTROL_ALL_DOOR_STATE = "/control-all-door-state";
	public static final String URL_C_CONTROL_DOOR_STATE = "/control-door-state";
	public static final String URL_C_CONTROL_ALL_DOOR_LOCK = "/control-all-door-lock";
	public static final String URL_C_CONTROL_DOOR_LOCK = "/control-door-lock";
	
	public static final String URL_C_WINDOWS = "/windows";
	public static final String URL_C_LOAD_ALL_WINDOWS = "/load-all-windows";
	public static final String URL_C_CONTROL_ALL_WINDOWS_STATE = "/control-all-windows-state";
	public static final String URL_C_CONTROL_ALL_WINDOWS_BLIND = "/control-all-windows-blind";
	public static final String URL_C_CONTROL_ALL_WINDOWS_LOCK = "/control-all-windows-lock";
	public static final String URL_C_CONTROL_WINDOWS_STATE = "/control-windows-state";
	public static final String URL_C_CONTROL_WINDOWS_BLIND = "/control-windows-blind";
	public static final String URL_C_CONTROL_WINDOWS_LOCK = "/control-windows-lock";
	
	public static final String URL_C_MEDIA = "/media";
	public static final String URL_C_LOAD_ALL_MEDIA = "/load-all-media";
	public static final String URL_C_CONTROL_MEDIA = "/control-media";
	
	public static final String URL_C_SECURITY = "/security";
	public static final String URL_C_LOAD_ALL_SECURITY = "/load-all-security";
	public static final String URL_C_UPDATE_SECURITY_STATE = "/update-security-state";
	public static final String URL_C_UPDATE_SECURITY_ALARM = "/update-security-alarm";
	public static final String URL_C_UPDATE_ALL_SECURITY_STATE = "/update-all-security-state";
	public static final String URL_C_UPDATE_INDOOR_OUTDOOR_STATE = "/update-indoor-outdoor-state";
	public static final String URL_C_FIND_A_SECURITY = "/find-a-security";
	public static final String URL_C_NOTIFY_SECURITY = "/notify-security";
	
	public static final String URL_C_THERMOSTAT = "/thermostat";
	public static final String URL_C_LOAD_ALL_THERMOSTAT = "/load-all-thermostat";
	public static final String URL_C_CONTROL_THERMOSTAT = "/control-thermostat";
	
	public static final String URL_C_TEMPERATURE = "/temperature";
	public static final String URL_C_LOAD_ALL_TEMPERATURE = "/load-all-temperature";
	public static final String URL_C_UPDATE_TEMPERATURE = "/update-temperature";
	
	public static final String URL_C_CAMERA = "/camera";
	public static final String URL_C_LOAD_ALL_CAMERA = "/load-all-camera";
	public static final String URL_C_CONTROL_CAMERA_STATE = "/control-camera-state";
	
	public static final String URL_C_INTERCOM = "/intercom";
	public static final String URL_C_LOAD_ALL_INTERCOM = "/load-all-intercom";
	public static final String URL_C_LOAD_MONITOR_CLIENT = "/load-monitor-client";
	public static final String URL_C_TRANSFER_AUDIO_TO_CLIENT = "/transfer-audio-to-client";
	public static final String URL_C_NOTIFICATION_TO_MONITOR = "/notification-to-monitor";
	public static final String URL_C_LOAD_ALL_CLIENT_RECORD = "/load-all-client-record";
	public static final String URL_C_REMOVE_INTERCOM_RECORD = "/remove-intercom-record";
	
	public static final String URL_C_GATE = "/gate";
	public static final String URL_C_LOAD_GATE = "/load-gate";
	public static final String URL_C_CONTROL_GATE_STATE = "/control-gate-state";
	public static final String URL_C_CONTROL_GATE_LOCK = "/control-gate-lock";
	public static final String URL_C_CONTROL_GATE_CAMERA = "/control-gate-camera";
	public static final String URL_C_CONTROL_GATE_INTERCOM = "/control-gate-intercom";
	
	public static final String URL_C_GARAGE = "/garage";
	public static final String URL_C_LOAD_ALL_GARAGE = "/load-all-garage";
	public static final String URL_C_CONTROL_GARAGE_STATE = "/control-garage-state";
	public static final String URL_C_CONTROL_GARAGE_LOCK = "/control-garage-lock";
	public static final String URL_C_CONTROL_GARAGE_CAMERA = "/control-garage-camera";
	public static final String URL_C_CONTROL_GARAGE_INTERCOM = "/control-garage-intercom";
	
	public static final String URL_C_SETTING = "/setting";
	public static final String URL_C_LOAD_LANGUAGE = "/load-language";
	public static final String URL_C_UPDATE_LANGUAGE = "/update-language";
	
	public static final String URL_C_AUTOMATION = "/automation";
	public static final String URL_C_LOAD_ALL_AUTOMATION = "/load-all-automation";
	public static final String URL_C_FIND_AUTOMATION_LIST = "/find-automation-list";
	public static final String URL_C_CONTROL_AUTOMATION = "/control-automation";
	public static final String URL_C_ONOFF_AUTOMATION = "/onoff-automation";
	public static final String URL_C_CREATE_AUTOMATION = "/create-automation";
	public static final String URL_C_UPDATE_AUTOMATION = "/update-automation";
	public static final String URL_C_DELETE_AUTOMATION = "/delete-automation";
	
	
	public static final String URL_C_SETUP = "/setup";
	/* --- End URL for Controller ---------------------------------------------------------------- */
	
	
	/* --- Start URL for Satellite ---------------------------------------------------------------- */
	public static final String SLASH_SYMBOL = "/";
	public static final String URL_SATELLITE_GPIO_STATE = "/gpio/state";
	public static final String URL_SATELLITE_BLE_ARDUINO_RELAY = "/ble-arduino-relay";
	/* --- End URL for Satellite ---------------------------------------------------------------- */
}
