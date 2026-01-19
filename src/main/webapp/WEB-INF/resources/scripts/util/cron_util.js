class CronUtils {
	/**
	 * Generate Cron String từ Config Object
	 * @param {Object} config - { type, moment, daysOfWeek, dayOfMonth }
	 * @returns {String} Quartz Cron String
	 */
	static toCron(config) {
		if (!config || !config.moment) return '';

		const m = config.moment;
		const seconds = '0';
		const minutes = m.minute();
		const hours = m.hour();

		// 1. DAILY: 0 30 14 * * ?
		if (config.type === 'DAILY') {
			return `${seconds} ${minutes} ${hours} * * ?`;
		}

		// 2. WEEKLY: 0 30 14 ? * MON,WED
		if (config.type === 'WEEKLY') {
			const days = config.daysOfWeek && config.daysOfWeek.length > 0 ? config.daysOfWeek.join(',') : 'MON';
			return `${seconds} ${minutes} ${hours} ? * ${days}`;
		}

		// 3. MONTHLY: 0 30 14 15 * ?
		if (config.type === 'MONTHLY') {
			const day = config.dayOfMonth || 1;
			return `${seconds} ${minutes} ${hours} ${day} * ?`;
		}

		// 4. ONCE: 0 30 14 25 12 ? 2025
		const date = m.date();
		const month = m.month() + 1; // Moment(0-11) -> Cron(1-12)
		const year = m.year();
		return `${seconds} ${minutes} ${hours} ${date} ${month} ? ${year}`;
	}

	/**
	 * Parse Cron String thành Config Object để fill vào UI
	 * @param {String} cron
	 * @returns {Object} { type, moment, daysOfWeek, dayOfMonth }
	 */
	static fromCron(cron) {
		if (!cron) return { type: 'ONCE', moment: moment() };

		const parts = cron.split(' ');
		if (parts.length < 6) return { type: 'ONCE', moment: moment() };

		const [s, min, h, dom, mon, dow, year] = parts;
		const timeStr = `${h}:${min}`;
		const timeMoment = moment(timeStr, 'H:m');

		// DETECT TYPE
		// DAILY: dom=*, mon=*, dow=? (hoặc *)
		if (dom === '*' && mon === '*' && (dow === '?' || dow === '*')) {
			return { type: 'DAILY', moment: timeMoment };
		}

		// WEEKLY: dow có chứa chữ cái (MON, TUE...) hoặc list số
		if (dow !== '?' && dow !== '*' && (dow.includes(',') || /[A-Z]/.test(dow))) {
			return {
				type: 'WEEKLY',
				moment: timeMoment,
				daysOfWeek: dow.split(','),
			};
		}

		// MONTHLY: dom là số cụ thể, mon=*
		if (dom !== '*' && dom !== '?' && mon === '*') {
			return {
				type: 'MONTHLY',
				moment: timeMoment,
				dayOfMonth: dom,
			};
		}

		// ONCE: Fallback
		const currentNow = moment();
		const yearVal = year ? parseInt(year) : currentNow.year();
		const monthVal = parseInt(mon) - 1;
		const dayVal = parseInt(dom);

		const fullMoment = moment({
			year: yearVal,
			month: monthVal,
			date: dayVal,
			hour: parseInt(h),
			minute: parseInt(min),
		});

		return { type: 'ONCE', moment: fullMoment };
	}

	static formatDisplay(cron) {
		const parsed = this.fromCron(cron);
		const time = parsed.moment.format('HH:mm');

		if (parsed.type === 'DAILY') {
			return `<span class="badge badge-info"><i class="fas fa-redo"></i> Daily @ ${time}</span>`;
		}
		if (parsed.type === 'WEEKLY') {
			return `<span class="badge badge-primary"><i class="fas fa-calendar-week"></i> Weekly (${parsed.daysOfWeek}) @ ${time}</span>`;
		}
		if (parsed.type === 'MONTHLY') {
			return `<span class="badge badge-warning"><i class="fas fa-calendar"></i> Monthly (Day ${parsed.dayOfMonth}) @ ${time}</span>`;
		}

		// ONCE
		const fullTime = parsed.moment.format('DD/MM/YYYY HH:mm');
		return `<span class="badge badge-secondary"><i class="fas fa-clock"></i> ${fullTime}</span>`;
	}
}
window.CronUtils = CronUtils;
