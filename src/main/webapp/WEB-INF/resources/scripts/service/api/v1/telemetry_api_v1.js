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

	/**
	 * Fetch dữ liệu nhiệt độ từ cảm biến
	 * @param {string} naturalId - Mã định danh cảm biến nhiệt độ
	 * @returns {Promise<Object>} Response từ server
	 */
	async fetchTemperature(naturalId) {
		try {
			return await window.http.post(this.api.TEMPERATURE(naturalId));
		} catch (error) {
			this.#handleError(`fetch temperature data for sensor ${naturalId}`, error);
		}
	}

	/**
	 * Fetch dữ liệu tiêu thụ điện năng từ cảm biến
	 * @param {string} naturalId - Mã định danh cảm biến tiêu thụ điện
	 * @returns {Promise<Object>} Response từ server
	 */
	async fetchPowerConsumption(naturalId) {
		try {
			return await window.http.post(this.api.POWER_CONSUMPTION(naturalId));
		} catch (error) {
			this.#handleError(`fetch power consumption data for sensor ${naturalId}`, error);
		}
	}

	#handleError(action, error) {
		console.error(`[TelemetryApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

if (typeof window == 'undefined') throw new Error('TelemetryApiV1Service can only be initialized in a browser environment');
window.telemetryApiV1Service = new TelemetryApiV1Service();
