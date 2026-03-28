(() => {
	'use strict';

	const _config = {
		enabled: true,
		showTimestamp: true,
		colors: {
			info: '#2ecc71', // Green
			debug: '#95a5a6', // Gray
			warn: '#f1c40f', // Yellow
			error: '#e74c3c', // Red
			topic: '#3498db', // Blue
		},
	};

	class Logger {
		constructor(topic) {
			this.topic = topic || 'App';
		}

		#print(level, msg, data) {
			if (!_config.enabled) return;

			const color = _config.colors[level] || '#000';
			const time = _config.showTimestamp ? `${new Date().toLocaleTimeString()} ` : '';

			const labelStyle = `color: #fff; background: ${color}; padding: 1px 4px; border-radius: 2px; font-size: 10px; font-weight: bold;`;
			const topicStyle = `color: ${_config.colors.topic}; font-weight: bold;`;
			const reset = 'color: inherit;';

			const format = `%c${level.toUpperCase()}%c %c${this.topic}%c ${time}${msg}`;

			const args = [format, labelStyle, reset, topicStyle, reset];
			if (data !== undefined) args.push(data);

			console.log(...args);
		}

		info(msg, data) {
			this.#print('info', msg, data);
		}
		debug(msg, data) {
			this.#print('debug', msg, data);
		}
		warn(msg, data) {
			this.#print('warn', msg, data);
		}
		error(msg, data) {
			this.#print('error', msg, data);
		}

		static config(options) {
			if (options.enabled !== undefined) _config.enabled = !!options.enabled;
			if (options.showTimestamp !== undefined) _config.showTimestamp = !!options.showTimestamp;
		}
	}

	const loggerFactory = (topic) => new Logger(topic);
	loggerFactory.config = Logger.config;

	window.logger = loggerFactory;
})();
