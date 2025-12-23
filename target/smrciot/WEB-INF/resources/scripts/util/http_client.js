const BASE_URL = '/';
const BASE_API_URL = BASE_URL + 'api/v1/';

/**
 * @typedef {Object} ApiResponse
 * @property {number} status - HTTP Status Code
 * @property {string} message - Message từ server
 * @property {any} data - Dữ liệu trả về (Payload)
 * @property {string} timestamp - Thời gian phản hồi
 */

/**
 * Base HTTP Client
 * Wrapper cho $.ajax để chuẩn hóa RESTful API Calls
 */
class HttpClient {
	/**
	 * @param {string} baseURL - Context Path (VD: '/smrciot/') lấy từ Thymeleaf
	 */
	constructor(baseURL) {
		this.baseURL = baseURL ? (baseURL.endsWith('/') ? baseURL : baseURL + '/') : BASE_API_URL;
		this.headers = {
			Accept: 'application/json',
			'Content-Type': 'application/json; charset=utf-8',
		};
	}

	/**
	 * GET Request
	 * @param {string} endpoint - API path (VD: 'api/v1/rooms')
	 * @param {Object} [params] - Query Parameters (VD: { page: 0, size: 10 })
	 * @returns {Promise<ApiResponse>}
	 */
	async get(endpoint, params = {}) {
		return this._request('GET', endpoint, params);
	}

	/**
	 * POST Request
	 * @param {string} endpoint
	 * @param {Object} body - Request Body (JSON object)
	 * @returns {Promise<ApiResponse>}
	 */
	async post(endpoint, body = {}) {
		return this._request('POST', endpoint, null, body);
	}

	/**
	 * PUT Request
	 * @param {string} endpoint
	 * @param {Object} body
	 * @returns {Promise<ApiResponse>}
	 */
	async put(endpoint, body = {}) {
		return this._request('PUT', endpoint, null, body);
	}

	/**
	 * DELETE Request
	 * @param {string} endpoint
	 * @param {Object} [params]
	 * @returns {Promise<ApiResponse>}
	 */
	async delete(endpoint, params = {}) {
		return this._request('DELETE', endpoint, params);
	}

	/**
	 * Internal Request Handler
	 * @private
	 */
	_request(method, endpoint, params = null, body = null) {
		const cleanEndpoint = endpoint.startsWith('/') ? endpoint.substring(1) : endpoint;
		let url = this.baseURL + cleanEndpoint;

		// Serialize params for GET/DELETE
		if ((method === 'GET' || method === 'DELETE') && params && Object.keys(params).length > 0) {
			const query = $.param(params);
			url += (url.includes('?') ? '&' : '?') + query;
		}

		const config = {
			url: url,
			method: method,
			headers: this.headers,
			dataType: 'json',
		};

		if (method === 'POST' || method === 'PUT') {
			config.data = JSON.stringify(body);
		}

		return new Promise((resolve, reject) => {
			$.ajax(config)
				.done((response) => {
					resolve(response);
				})
				.fail((xhr, textStatus, errorThrown) => {
					const errorRes = xhr.responseJSON || {
						status: xhr.status,
						message: errorThrown || 'Unknown Error',
						timestamp: new Date().toISOString(),
					};
					console.error(`[API] ${method} ${url} FAILED:`, errorRes);
					reject(errorRes);
				});
		});
	}
}

window.HttpClient = HttpClient;
