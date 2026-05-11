/** @typedef {'username' | 'roles' | 'user'} StorageKey */

const VALID_KEYS = ['username', 'roles', 'user'];

/**
 * Validates if a key is allowed
 * @param {string} key
 * @throws {Error} If key is not defined in VALID_KEYS
 */
const validateKey = (key) => {
	if (!VALID_KEYS.includes(key)) {
		throw new Error(`LocalStorage Error: Key "${key}" is not defined in the application structure.`);
	}
};

export const storage = {
	/**
	 * Sets an item in localStorage
	 * @param {StorageKey} key
	 * @param {any} value
	 */
	set(key, value) {
		validateKey(key);
		const stringValue = typeof value === 'string' ? value : JSON.stringify(value);
		localStorage.setItem(key, stringValue);
	},

	/**
	 * Gets an item from localStorage
	 * @param {StorageKey} key
	 * @returns {string|null}
	 */
	get(key) {
		validateKey(key);
		return localStorage.getItem(key);
	},

	/**
	 * Gets an item and parses it as JSON
	 * @template T
	 * @param {StorageKey} key
	 * @returns {T|null}
	 */
	getJson(key) {
		const value = this.get(key);
		if (!value) return null;
		try {
			return JSON.parse(value);
		} catch (e) {
			console.error(`LocalStorage Error: Failed to parse key "${key}" as JSON`, e);
			return null;
		}
	},

	/**
	 * Removes an item from localStorage
	 * @param {StorageKey} key
	 */
	remove(key) {
		validateKey(key);
		localStorage.removeItem(key);
	},

	/**
	 * Clears specific keys instead of everything (safer)
	 * @param {StorageKey[]} keys
	 */
	removeMany(keys) {
		keys.forEach((key) => this.remove(key));
	},
};
