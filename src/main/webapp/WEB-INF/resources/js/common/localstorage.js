const PREFIX = 'seiot_';

/**
 * Mã hóa chuỗi sang Base64 (Hỗ trợ Unicode/Vietnamese)
 * @param {string} str
 * @returns {string}
 */
const encodeBase64 = (str) => {
	try {
		return btoa(
			encodeURIComponent(str).replace(/%([0-9A-F]{2})/g, (match, p1) => {
				return String.fromCharCode('0x' + p1);
			}),
		);
	} catch (e) {
		console.error('Storage: Lỗi mã hóa Base64', e);
		return str;
	}
};

/**
 * Giải mã chuỗi từ Base64 (Hỗ trợ Unicode/Vietnamese)
 * @param {string} str
 * @returns {string}
 */
const decodeBase64 = (str) => {
	try {
		return decodeURIComponent(
			atob(str)
				.split('')
				.map((c) => {
					return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
				})
				.join(''),
		);
	} catch (e) {
		console.error('Storage: Lỗi giải mã Base64', e);
		return str;
	}
};

export const Storage = {
	/**
	 * Lưu trữ dữ liệu vào localStorage.
	 * @param {string} key Khóa lưu trữ (không cần prefix)
	 * @param {any} value Giá trị cần lưu (Object, Array, String, Number...)
	 * @param {number} [ttl] Thời gian sống tính bằng mili-giây (tùy chọn)
	 */
	set: (key, value, ttl = null) => {
		try {
			const data = {
				value: value,
				expiry: ttl ? Date.now() + ttl : null,
			};

			const jsonStr = JSON.stringify(data);
			const base64Str = encodeBase64(jsonStr);

			localStorage.setItem(PREFIX + key, base64Str);
		} catch (e) {
			console.error(`Storage: Không thể lưu key "${key}"`, e);
		}
	},

	/**
	 * Lấy dữ liệu từ localStorage.
	 * @param {string} key Khóa lưu trữ
	 * @returns {any|null} Trả về giá trị đã lưu hoặc null nếu hết hạn/không tồn tại.
	 */
	get: (key) => {
		try {
			const base64Str = localStorage.getItem(PREFIX + key);
			if (!base64Str) return null;

			const jsonStr = decodeBase64(base64Str);
			const data = JSON.parse(jsonStr);

			if (data.expiry && Date.now() > data.expiry) {
				Storage.remove(key);
				return null;
			}

			return data.value;
		} catch (e) {
			console.error(`Storage: Lỗi khi lấy key "${key}"`, e);
			return null;
		}
	},

	/**
	 * Xóa một key cụ thể.
	 * @param {string} key
	 */
	remove: (key) => {
		localStorage.removeItem(PREFIX + key);
	},

	/**
	 * Kiểm tra xem một key có tồn tại và còn hạn hay không.
	 * @param {string} key
	 * @returns {boolean}
	 */
	has: (key) => {
		return Storage.get(key) !== null;
	},

	/**
	 * Xóa tất cả các entry có prefix của ứng dụng.
	 */
	clear: () => {
		const keysToRemove = [];
		for (let i = 0; i < localStorage.length; i++) {
			const key = localStorage.key(i);
			if (key && key.startsWith(PREFIX)) {
				keysToRemove.push(key);
			}
		}
		keysToRemove.forEach((key) => localStorage.removeItem(key));
	},

	/**
	 * Liệt kê danh sách các key của ứng dụng (đã bỏ prefix).
	 * @returns {string[]}
	 */
	keys: () => {
		const keys = [];
		for (let i = 0; i < localStorage.length; i++) {
			const key = localStorage.key(i);
			if (key && key.startsWith(PREFIX)) {
				keys.push(key.replace(PREFIX, ''));
			}
		}
		return keys;
	},
};
