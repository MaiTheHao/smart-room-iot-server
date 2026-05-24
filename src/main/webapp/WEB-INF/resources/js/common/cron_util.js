import { UTCUtils } from './utc_util.js';

export const CronUtils = {
  /**
   * Chuyển đổi Quartz Cron string (UTC) thành structured object (Local).
   * @param {string} cron - Định dạng Quartz Cron: "s min h dom month dow"
   * @returns {{type: string, hour: number, minute: number, second: number, daysOfWeek?: string[], dayOfMonth?: string}}
   */
  fromCron(cron) {
    const defaultResult = { type: 'DAILY', hour: 0, minute: 0, second: 0 };
    if (!cron) return defaultResult;

    const parts = cron.split(' ');
    if (parts.length < 6) return defaultResult;

    const [s, min, h, dom, mon, dow] = parts;

    const utcSecond = parseInt(s, 10) || 0;
    const utcMinute = parseInt(min, 10) || 0;
    const utcHour = parseInt(h, 10) || 0;

    const localTime = UTCUtils.utcToLocal(utcHour, utcMinute, utcSecond);

    const parsed = {
      type: 'DAILY',
      hour: localTime.hour,
      minute: localTime.minute,
      second: localTime.second,
    };

    const dayNames = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];

    if (dow !== '?' && dow !== '*') {
      parsed.type = 'WEEKLY';

      const dayOffset = UTCUtils.OffsetUTCToLocal(utcHour, utcMinute, utcSecond);

      parsed.daysOfWeek = dow.split(',').map((day) => {
        const utcIdx = dayNames.indexOf(day.trim());
        if (utcIdx !== -1) {
          const localIdx = (utcIdx + dayOffset + 7) % 7;
          return dayNames[localIdx];
        }
        return day;
      });
    } else if (dom !== '*' && dom !== '?') {
      parsed.type = 'MONTHLY';

      const utcDateOfMonth = parseInt(dom, 10) || 1;

      const utcDate = new Date();
      utcDate.setUTCHours(utcHour, utcMinute, utcSecond, 0);
      utcDate.setUTCDate(utcDateOfMonth);

      parsed.dayOfMonth = String(utcDate.getDate());
    }

    return parsed;
  },

  /**
   * Sinh Quartz Cron string (UTC) từ cấu hình (Local).
   * @param {{type: string, hour: number|string, minute: number|string, second: number|string, daysOfWeek?: string[], dayOfMonth?: string|number}} config
   * @returns {string} Quartz Cron expression
   */
  toCron({ type, hour, minute, second, daysOfWeek, dayOfMonth }) {
    const localHour = parseInt(hour, 10) || 0;
    const localMinute = parseInt(minute, 10) || 0;
    const localSecond = parseInt(second, 10) || 0;

    const utcTime = UTCUtils.localToUTC(localHour, localMinute, localSecond);

    if (type === 'DAILY') {
      return `${utcTime.second} ${utcTime.minute} ${utcTime.hour} * * ?`;
    }

    const dayNames = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];

    if (type === 'WEEKLY') {
      let utcDaysOfWeek = [];

      const dayOffset = UTCUtils.OffsetLocalToUTC(localHour, localMinute, localSecond);

      if (daysOfWeek && daysOfWeek.length > 0) {
        daysOfWeek.forEach((day) => {
          const localIdx = dayNames.indexOf(day.trim());
          if (localIdx !== -1) {
            const utcIdx = (localIdx + dayOffset + 7) % 7;
            utcDaysOfWeek.push(dayNames[utcIdx]);
          }
        });
      }
      const dow = utcDaysOfWeek.length > 0 ? utcDaysOfWeek.join(',') : 'MON';
      return `${utcTime.second} ${utcTime.minute} ${utcTime.hour} ? * ${dow}`;
    }

    if (type === 'MONTHLY') {
      const localDateOfMonth = parseInt(dayOfMonth, 10) || 1;

      const utcDateOfMonth = UTCUtils.calculateUTCDateOfMonth(
        localDateOfMonth,
        localHour,
        localMinute,
        localSecond
      );

      return `${utcTime.second} ${utcTime.minute} ${utcTime.hour} ${utcDateOfMonth} * ?`;
    }

    return '';
  },
};
