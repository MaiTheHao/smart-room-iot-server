package com.iviet.ivshs.service.telemetry;

public interface TelemetryService {
	void takeByGateway(String gatewayUsername);

	void takeByGateway(Long gatewayId);

	void takeByIpAddress(String gatewayIpAddress);

	void takeByRoom(String roomCode);

	void takeByRoom(Long roomId);

	void takeGlobalTelemetry();
}
