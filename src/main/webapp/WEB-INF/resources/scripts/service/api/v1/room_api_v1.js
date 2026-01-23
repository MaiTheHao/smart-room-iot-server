class RoomApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
		this.api = SMRC_API_V1.ROOM;
	}

	async getByFloor(floorId, page = 0, size = 10) {
		try {
			return await this.client.get(SMRC_API_V1.FLOOR.ROOMS(floorId), { page, size });
		} catch (error) {
			this._handleError(`get rooms for floor ${floorId}`, error);
		}
	}

	async getById(roomId) {
		try {
			return await this.client.get(this.api.DETAIL(roomId));
		} catch (error) {
			this._handleError(`get room ${roomId}`, error);
		}
	}

	async create(floorId, roomData) {
		try {
			return await this.client.post(SMRC_API_V1.FLOOR.ROOMS(floorId), roomData);
		} catch (error) {
			this._handleError(`create room on floor ${floorId}`, error);
		}
	}

	async update(roomId, roomData) {
		try {
			return await this.client.put(this.api.DETAIL(roomId), roomData);
		} catch (error) {
			this._handleError(`update room ${roomId}`, error);
		}
	}

	async delete(roomId) {
		try {
			return await this.client.delete(this.api.DETAIL(roomId));
		} catch (error) {
			this._handleError(`delete room ${roomId}`, error);
		}
	}

	_handleError(action, error) {
		console.error(`[RoomApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

window.RoomApiV1Service = RoomApiV1Service;
