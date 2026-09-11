const BASE_URL = '';

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
			const error = new Error(errorMessage);
			error.status = response.status;
			error.data = result;
			return [error, null];
		}

		return [null, result];
	} catch (error) {
		console.error(`API Request Error [${endpoint}]:`, error);
		return [error, null];
	}
};

