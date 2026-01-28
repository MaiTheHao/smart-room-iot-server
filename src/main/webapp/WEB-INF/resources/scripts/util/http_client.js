(() => {
	'use strict';

	class HttpClient {
		#logger;

		constructor(
			options = {
				ip: null,
				port: null,
				baseUrl: null,
				headers: null,
			},
		) {
			this.headers = {
				Accept: 'application/json',
				'Content-Type': 'application/json; charset=utf-8',
				...(options.headers || {}),
			};

			this.ip = options.ip || window.location.hostname;
			this.port = options.port || '8080';

			const isLocal = this.ip === 'localhost' || this.ip === '127.0.0.1';
			const protocol = isLocal ? 'http' : window.location.protocol.replace(':', '');

			this.baseUrl = options.baseUrl || `${protocol}://${this.ip}:${this.port}`;

			this.#logger = window.logger('HttpClient') || console;
		}

		#normalizeEndpoint(endpoint) {
			if (typeof endpoint !== 'string') return '';
			return endpoint.trim().replace(/^\/+/, '');
		}

		get(endpoint, params = {}) {
			return this.request('GET', endpoint, params);
		}

		post(endpoint, body = {}) {
			return this.request('POST', endpoint, null, body);
		}

		put(endpoint, body = {}) {
			return this.request('PUT', endpoint, null, body);
		}

		delete(endpoint, params = {}) {
			return this.request('DELETE', endpoint, params);
		}

		async request(method, endpoint, params = null, body = null) {
			const cleanEndpoint = this.#normalizeEndpoint(endpoint);
			let url = `${this.baseUrl}/${cleanEndpoint}`;

			if ((method === 'GET' || method === 'DELETE') && params && Object.keys(params).length) {
				const query = new URLSearchParams(params).toString();
				url += `${url.includes('?') ? '&' : '?'}${query}`;
			}

			this.#logger.debug(`${method} ${url}`, body || params || '');

			const config = {
				method,
				headers: this.headers,
				credentials: 'include',
			};

			if (method === 'POST' || method === 'PUT') {
				config.body = JSON.stringify(body);
			}

			try {
				const response = await fetch(url, config);

				if (response.status === 204) {
					return {};
				}

				let result;
				const contentType = response.headers.get('content-type');

				if (contentType && contentType.includes('application/json')) {
					result = await response.json();
				} else {
					const text = await response.text();
					result = text ? { message: text } : {};
				}

				if (!response.ok) {
					throw {
						status: response.status,
						message: result.message || result.error || 'Server Error',
						data: result,
						timestamp: new Date().toISOString(),
					};
				}

				return result;
			} catch (error) {
				const safeError = error.status
					? error
					: {
							status: 0,
							message: error.message || 'Network Error',
							timestamp: new Date().toISOString(),
						};

				this.#logger.error(`${method} ${url} FAILED`, safeError);
				throw safeError;
			}
		}
	}

	window.HttpClient = HttpClient;
	window.http = new HttpClient();
})();
