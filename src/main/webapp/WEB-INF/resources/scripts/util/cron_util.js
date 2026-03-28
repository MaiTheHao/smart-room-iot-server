class CronUtils {
	/**
	 * Generate Quartz Cron String
	 * @param {Object} config - { type: 'DAILY'|'WEEKLY'|'MONTHLY', timeMoment, daysOfWeek, dayOfMonth }
	 * @returns {String}
	 */
	static toCron(config) {
		if (!config || !config.timeMoment) return '';

		const m = config.timeMoment;
		const s = m.format('s');
		const min = m.minute();
		const h = m.hour();

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

		return '';
	}

	static fromCron(cron) {
		const defaultTime = moment().startOf('minute');
		if (!cron) return { type: 'DAILY', timeMoment: defaultTime };

		const parts = cron.split(' ');
		if (parts.length < 6) return { type: 'DAILY', timeMoment: defaultTime };

		const [s, min, h, dom, mon, dow] = parts;

		const timeStr = `${h}:${min}:${s}`;
		const timeMoment = moment(timeStr, 'H:m:s');

		if (dow !== '?' && dow !== '*') {
			return {
				type: 'WEEKLY',
				timeMoment: timeMoment,
				daysOfWeek: dow.split(','),
			};
		}

		if (dom !== '*' && dom !== '?') {
			return {
				type: 'MONTHLY',
				timeMoment: timeMoment,
				dayOfMonth: dom,
			};
		}

		return { type: 'DAILY', timeMoment: timeMoment };
	}

	static formatDisplay(cron) {
		const parsed = this.fromCron(cron);
		const time = parsed.timeMoment.format('HH:mm:ss');

		if (parsed.type === 'DAILY') {
			return `
			<span class="badge badge-light text-dark d-inline-flex align-items-center px-2">
				<i class="fas fa-redo text-muted mr-2"></i>
				<i class="far fa-clock text-muted mr-1"></i>
				<span class="font-weight-normal">${time}</span>
			</span>
		`;
		}

		if (parsed.type === 'WEEKLY') {
			return `
			<span class="badge badge-light text-dark d-inline-flex align-items-center px-2">
				<i class="fas fa-calendar-week text-muted mr-2"></i>
				<span class="mr-2">${parsed.daysOfWeek}</span>
				<i class="far fa-clock text-muted mr-1"></i>
				<span>${time}</span>
			</span>
		`;
		}

		if (parsed.type === 'MONTHLY') {
			return `
			<span class="badge badge-light text-dark d-inline-flex align-items-center px-2">
				<i class="fas fa-calendar-alt text-muted mr-2"></i>
				<span class="mr-2">#${parsed.dayOfMonth}</span>
				<i class="far fa-clock text-muted mr-1"></i>
				<span>${time}</span>
			</span>
		`;
		}

		return cron;
	}
}

window.CronUtils = CronUtils;
