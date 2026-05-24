export const UTCUtils = {
  /**
   * Chuyển đối tượng Date thành thông tin UTC chi tiết.
   * @param {Date} date
   * @returns {{dayOfMonth: number, month: number, year: number, hour: number, minute: number, second: number, isoString: string}}
   */
  toUTCMetadata(date) {
    const d = date instanceof Date && !isNaN(date.getTime()) ? date : new Date();
    return {
      dayOfMonth: d.getUTCDate(),
      month: d.getUTCMonth() + 1,
      year: d.getUTCFullYear(),
      hour: d.getUTCHours(),
      minute: d.getUTCMinutes(),
      second: d.getUTCSeconds(),
      isoString: d.toISOString(),
    };
  },

  /**
   * Chuyển đổi giờ UTC sang Local.
   * @param {number} utcHour
   * @param {number} utcMinute
   * @param {number} utcSecond
   * @returns {{hour: number, minute: number, second: number}}
   */
  utcToLocal(utcHour, utcMinute, utcSecond) {
    const d = new Date();
    d.setUTCHours(utcHour, utcMinute, utcSecond, 0);
    return {
      hour: d.getHours(),
      minute: d.getMinutes(),
      second: d.getSeconds(),
    };
  },

  /**
   * Chuyển đổi giờ Local sang UTC.
   * @param {number} localHour
   * @param {number} localMinute
   * @param {number} localSecond
   * @returns {{hour: number, minute: number, second: number}}
   */
  localToUTC(localHour, localMinute, localSecond) {
    const d = new Date();
    d.setHours(localHour, localMinute, localSecond, 0);
    return {
      hour: d.getUTCHours(),
      minute: d.getUTCMinutes(),
      second: d.getUTCSeconds(),
    };
  },

  /**
   * Tính toán độ lệch ngày từ Local sang UTC (-1, 0, hoặc +1).
   * @param {number} localHour
   * @param {number} localMinute
   * @param {number} localSecond
   * @returns {number}
   */
  OffsetLocalToUTC(localHour, localMinute, localSecond) {
    const d = new Date();
    d.setHours(localHour, localMinute, localSecond, 0);
    return d.getUTCDay() - d.getDay();
  },

  /**
   * Tính toán độ lệch ngày từ UTC sang Local (-1, 0, hoặc +1).
   * @param {number} utcHour
   * @param {number} utcMinute
   * @param {number} utcSecond
   * @returns {number}
   */
  OffsetUTCToLocal(utcHour, utcMinute, utcSecond) {
    const d = new Date();
    d.setUTCHours(utcHour, utcMinute, utcSecond, 0);
    return d.getDay() - d.getUTCDay();
  },

  /**
   * Tính ngày UTC từ ngày Local cho cấu hình chạy hàng tháng.
   * @param {number} localDateOfMonth
   * @param {number} localHour
   * @param {number} localMinute
   * @param {number} localSecond
   * @returns {number} Ngày trong tháng theo giờ UTC (1-31)
   */
  calculateUTCDateOfMonth(localDateOfMonth, localHour, localMinute, localSecond) {
    const d = new Date();
    d.setDate(localDateOfMonth);
    d.setHours(localHour, localMinute, localSecond, 0);
    return d.getUTCDate();
  },

  /**
   * Lấy múi giờ (timezone offset) hiện tại dưới dạng chi tiết.
   * @returns {{sign: string, hours: number, minutes: number, totalMinutes: number, formatted: string}}
   */
  getTimezoneOffset() {
    const d = new Date();
    const offsetMinutes = -d.getTimezoneOffset();

    const hours = Math.floor(Math.abs(offsetMinutes) / 60);
    const minutes = Math.abs(offsetMinutes) % 60;
    const sign = offsetMinutes >= 0 ? '+' : '-';

    return {
      sign,
      hours,
      minutes,
      totalMinutes: offsetMinutes,
      formatted: `${sign}${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`,
    };
  },
};
