const BASE_URL = '';

/**
 * @param {string} endpoint
 * @param {Object} options
 * @returns {Promise<[Error|null, any]>}
 */
export const httpClient = async (endpoint, options = {}) => {
	const config = {
		...options,
		credentials: 'include',
		headers: {
			'Content-Type': 'application/json',
			...options.headers,
		},
	};

	try {
		const response = await fetch(`${BASE_URL}${endpoint}`, config);

		const text = await response.text();
		const isJson = response.headers.get('content-type')?.includes('application/json');
		const result = (isJson && text) ? JSON.parse(text) : null;

		if (!response.ok) {
			const errorMessage = result?.message || `HTTP error! status: ${response.status}`;
			return [new Error(errorMessage), null];
		}

		return [null, result];
	} catch (error) {
		console.error(`API Request Error [${endpoint}]:`, error);
		return [error, null];
	}
};

