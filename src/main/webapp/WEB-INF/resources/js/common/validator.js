/**
 * Common Validator Module
 *
 * Hệ thống validation dùng chung, cung cấp các hàm thuần (pure functions) trả về boolean.
 * Quy tắc chung: Hàm trả về true nếu HỢP LỆ (pass) hoặc không yêu cầu kiểm tra, trả về false nếu VI PHẠM (fail).
 * Thông báo lỗi sẽ được xử lý tại nơi sử dụng (usage side).
 */

/**
 * Factory tạo validator cho một field.
 * @param {Object} config Cấu hình mặc định cho field
 * @param {number} [config.min] Độ dài tối thiểu
 * @param {number} [config.max] Độ dài tối đa
 * @param {RegExp} [config.regex] Biểu thức chính quy để kiểm tra định dạng
 * @param {Function} [config.formatFn] Hàm kiểm tra định dạng tùy chỉnh (ưu tiên hơn regex)
 */
const createValidator = (config = {}) => {
	const { min, max, regex, formatFn } = config;

	const defaultRegex = regex instanceof RegExp ? regex : null;
	if (regex && !(regex instanceof RegExp)) {
		console.warn('Validator: Cấu hình "regex" phải là một instance của RegExp.');
	}

	return {
		/**
		 * Kiểm tra giá trị có tồn tại (không null/undefined).
		 * @param {any} val
		 * @returns {boolean} true nếu KHÔNG null/undefined.
		 */
		isNull: (val) => val !== null && val !== undefined,

		/**
		 * Kiểm tra giá trị có trống (chuỗi rỗng hoặc chỉ có khoảng trắng).
		 * @param {any} val
		 * @returns {boolean} true nếu KHÔNG trống.
		 */
		isBlank: (val) => {
			if (val === null || val === undefined) return false;
			return typeof val === 'string' ? val.trim() !== '' : true;
		},

		/**
		 * Kiểm tra độ dài tối thiểu.
		 * @param {any} val
		 * @param {number} [m] Giá trị min ghi đè
		 * @returns {boolean} true nếu độ dài >= min hoặc không kiểm tra.
		 */
		isLowerMin: (val, m = min) => {
			if (m === undefined || m === null) return true;
			if (val === null || val === undefined) return false;
			const length = String(val).length;
			return length >= m;
		},

		/**
		 * Kiểm tra độ dài tối đa.
		 * @param {any} val
		 * @param {number} [m] Giá trị max ghi đè
		 * @returns {boolean} true nếu độ dài <= max hoặc không kiểm tra.
		 */
		isHigherMax: (val, m = max) => {
			if (m === undefined || m === null) return true;
			if (val === null || val === undefined) return true;
			const length = String(val).length;
			return length <= max;
		},

		/**
		 * Kiểm tra định dạng. Ưu tiên formatFn > regex.
		 * @param {any} val
		 * @param {RegExp} [r] Regex ghi đè
		 * @returns {boolean} true nếu hợp lệ hoặc không kiểm tra.
		 */
		isValidFormat: (val, r = defaultRegex) => {
			if (formatFn) return formatFn(val);
			const activeRegex = r instanceof RegExp ? r : defaultRegex;
			if (!activeRegex) return true;
			if (val === null || val === undefined) return false;
			return activeRegex.test(String(val));
		},
	};
};

export const Validator = {
	// Generic field
	generic: createValidator(),

	// Username: 3-100 chars
	username: createValidator({ min: 3, max: 100 }),

	// Password: 6-100 chars
	password: createValidator({ min: 6, max: 100 }),

	// IP Address (IPv4 với optional port)
	ip: createValidator({
		regex: /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(:[0-9]{1,5})?$/,
	}),

	// MAC Address
	mac: createValidator({
		regex: /^([0-9A-Fa-f]{2}[:]){5}[0-9A-Fa-f]{2}$|^[0-9A-Fa-f]{12}$/,
	}),

	// URL (Avatar URL etc.)
	url: createValidator({
		formatFn: (val) => {
			if (!val || String(val).trim() === '') return true;
			try {
				new URL(String(val));
				return true;
			} catch (_) {
				return false;
			}
		},
	}),

	// Client Type (Bắt buộc)
	clientType: createValidator(),

	// Common Name: 1-100 chars
	name: createValidator({ min: 1, max: 100 }),

	// Common Code: max 256 chars
	code: createValidator({ max: 256 }),

	// Level (Integer)
	level: createValidator({
		formatFn: (val) => {
			if (val === null || val === undefined || val === '') return false;
			return !isNaN(parseInt(val)) && Number.isInteger(Number(val));
		},
	}),

	// Common Description: max 255 chars
	description: createValidator({ max: 255 }),

	// Generic ID (for foreign keys)
	id: createValidator(),
};
