const BASE_URL = '';

/**
 * Common request helper using ErrorFirst pattern [err, data]
 * @param {string} endpoint - API endpoint (e.g., '/api/v1/auth/signin')
 * @param {Object} options - Fetch options
 * @returns {Promise<[Error|null, any]>}
 */
const http_client = async (endpoint, options = {}) => {
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

		const isJson = response.headers.get('content-type')?.includes('application/json');
		const result = isJson ? await response.json() : null;

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
