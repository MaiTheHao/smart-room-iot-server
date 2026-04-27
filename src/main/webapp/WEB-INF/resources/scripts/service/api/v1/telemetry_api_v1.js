class TelemetryApiV1Service {
	static instance;

	constructor() {
		if (TelemetryApiV1Service.instance) return TelemetryApiV1Service.instance;
		TelemetryApiV1Service.instance = this;

		this.api = SMRC_API_V1.TELEMETRY;
	}

	/**
	 * Fetch dữ liệu telemetry từ gateway theo username
	 * @param {string} gatewayUsername - Tên đăng nhập của gateway
	 * @returns {Promise<Object>} Response từ server
	 */
	async fetchByGateway(gatewayUsername) {
		try {
			return await window.http.post(this.api.BY_GATEWAY(gatewayUsername));
		} catch (error) {
			this.#handleError(`fetch telemetry by gateway ${gatewayUsername}`, error);
		}
	}

	/**
	 * Fetch dữ liệu telemetry từ tất cả gateway trong phòng
	 * @param {string} roomCode - Mã phòng
	 * @returns {Promise<Object>} Response từ server
	 */
	async fetchByRoom(roomCode) {
		try {
			return await window.http.post(this.api.BY_ROOM(roomCode));
		} catch (error) {
			this.#handleError(`fetch telemetry by room ${roomCode}`, error);
		}
	}

	#handleError(action, error) {
		console.error(`[TelemetryApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

if (typeof window == 'undefined') throw new Error('TelemetryApiV1Service can only be initialized in a browser environment');
window.telemetryApiV1Service = new TelemetryApiV1Service();
