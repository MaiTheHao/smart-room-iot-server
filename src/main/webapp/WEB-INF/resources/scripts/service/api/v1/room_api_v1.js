class RoomApiV1Service {
	static instance;

	constructor() {
		if (RoomApiV1Service.instance) return RoomApiV1Service.instance;
		RoomApiV1Service.instance = this;

		this.api = SMRC_API_V1.ROOM;
	}

	async getByFloor(floorId, page = 0, size = 10) {
		try {
			return await window.http.get(SMRC_API_V1.ROOM.BY_FLOOR(floorId), { page, size });
		} catch (error) {
			this.#handleError(`get rooms for floor ${floorId}`, error);
		}
	}

	async getAllByFloor(floorId) {
		try {
			return await window.http.get(SMRC_API_V1.ROOM.BY_FLOOR_ALL(floorId));
		} catch (error) {
			this.#handleError(`get all rooms for floor ${floorId}`, error);
		}
	}

	async getById(roomId) {
		try {
			return await window.http.get(this.api.DETAIL(roomId));
		} catch (error) {
			this.#handleError(`get room ${roomId}`, error);
		}
	}

	async create(floorId, roomData) {
		try {
			return await window.http.post(SMRC_API_V1.ROOM.BY_FLOOR(floorId), roomData);
		} catch (error) {
			this.#handleError(`create room on floor ${floorId}`, error);
		}
	}

	async update(roomId, roomData) {
		try {
			return await window.http.put(this.api.DETAIL(roomId), roomData);
		} catch (error) {
			this.#handleError(`update room ${roomId}`, error);
		}
	}

	async delete(roomId) {
		try {
			return await window.http.delete(this.api.DETAIL(roomId));
		} catch (error) {
			this.#handleError(`delete room ${roomId}`, error);
		}
	}

	#handleError(action, error) {
		console.error(`[RoomApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

if (typeof window == 'undefined') throw new Error('RoomApiV1Service can only be initialized in a browser environment');
window.roomApiV1Service = new RoomApiV1Service();
