export const CronUtils = {
	/**
	 * Parse Quartz Cron into a structured object without relying on moment.js
	 * @param {String} cron 
	 * @returns {Object} { type: 'DAILY'|'WEEKLY'|'MONTHLY', timeStr: 'HH:mm:ss', daysOfWeek: [], dayOfMonth: '1' }
	 */
	fromCron(cron) {
		const defaultTimeStr = '00:00:00';
		if (!cron) return { type: 'DAILY', timeStr: defaultTimeStr };

		const parts = cron.split(' ');
		if (parts.length < 6) return { type: 'DAILY', timeStr: defaultTimeStr };

		const [s, min, h, dom, mon, dow] = parts;
		
		const pad = (n) => String(n).padStart(2, '0');
		const timeStr = `${pad(h)}:${pad(min)}:${pad(s)}`;

		if (dow !== '?' && dow !== '*') {
			return {
				type: 'WEEKLY',
				timeStr: timeStr,
				daysOfWeek: dow.split(','),
			};
		}

		if (dom !== '*' && dom !== '?') {
			return {
				type: 'MONTHLY',
				timeStr: timeStr,
				dayOfMonth: dom,
			};
		}

		return { type: 'DAILY', timeStr: timeStr };
	},

	/**
	 * Generate Quartz Cron String from structured config
	 * @param {Object} config - { type: 'DAILY'|'WEEKLY'|'MONTHLY', timeStr: 'HH:mm:ss', daysOfWeek: [], dayOfMonth: '1' }
	 * @returns {String}
	 */
	toCron(config) {
		if (!config || !config.timeStr) return '';

		const [h, min, s] = config.timeStr.split(':').map(n => parseInt(n, 10));

		if (config.type === 'DAILY') {
			return `${s} ${min} ${h} * * ?`;
		}

		if (config.type === 'WEEKLY') {
			const days = config.daysOfWeek && config.daysOfWeek.length > 0 ? config.daysOfWeek.join(',') : 'MON';
			return `${s} ${min} ${h} ? * ${days}`;
		}

		if (config.type === 'MONTHLY') {
			const days = config.dayOfMonth ? config.dayOfMonth : '1';
			return `${s} ${min} ${h} ${days} * ?`;
		}

		return '0 0 0 * * ?'; // Fallback
	}
};
