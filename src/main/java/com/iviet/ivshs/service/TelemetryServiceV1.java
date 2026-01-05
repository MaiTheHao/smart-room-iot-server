package com.iviet.ivshs.service;

public interface TelemetryServiceV1 {
	void takeByGateway(String gatewayUsername);
	void takeByGateway(Long gatewayId);
	void takeByIpAddress(String gatewayIpAddress);
	void takeByRoom(String roomCode);
	void takeByRoom(Long roomId);
	void takeTemperatureData(String roomCode);
	void takePowerConsumptionData(String roomCode);
}
