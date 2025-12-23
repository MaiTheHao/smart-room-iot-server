const ChartThemes = Object.freeze({
	TEMPERATURE: {
		borderColor: '#dc3545',
		backgroundColor: 'rgba(220, 53, 69, 0.1)',
		spinnerColor: 'text-danger',
		emptyIcon: 'fas fa-thermometer-empty',
	},
	POWER: {
		borderColor: '#ffc107',
		backgroundColor: 'rgba(255, 193, 7, 0.1)',
		spinnerColor: 'text-warning',
		emptyIcon: 'fas fa-plug',
	},
	HUMIDITY: {
		borderColor: '#0dcaf0',
		backgroundColor: 'rgba(13, 202, 240, 0.1)',
		spinnerColor: 'text-info',
		emptyIcon: 'fas fa-tint',
	},
	PRIMARY: {
		borderColor: '#0d6efd',
		backgroundColor: 'rgba(13, 110, 253, 0.1)',
		spinnerColor: 'text-primary',
		emptyIcon: 'fas fa-chart-line',
	},
});

const ChartUIState = Object.freeze({
	LOADING: 'LOADING',
	EMPTY: 'EMPTY',
	DATA: 'DATA',
	ERROR: 'ERROR',
});

class ChartUtils {
	static deepMerge(target, source) {
		const result = { ...target };
		for (const key in source) {
			if (Object.prototype.hasOwnProperty.call(source, key)) {
				if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key]) && target[key] && typeof target[key] === 'object') {
					result[key] = ChartUtils.deepMerge(target[key], source[key]);
				} else {
					result[key] = source[key];
				}
			}
		}
		return result;
	}

	static getTimeSeriesOptions(overrides = {}) {
		const defaults = {
			responsive: true,
			maintainAspectRatio: false,
			interaction: { intersect: false, mode: 'index' },
			plugins: {
				legend: { display: true, position: 'top' },
				tooltip: {
					enabled: true,
					callbacks: {
						title: (ctx) => (ctx.length > 0 ? moment(ctx[0].parsed.x).format('YYYY-MM-DD HH:mm') : ''),
					},
				},
			},
			scales: {
				x: {
					type: 'time',
					time: { unit: 'hour', displayFormats: { hour: 'HH:mm', day: 'MMM DD' } },
					title: { display: true, text: 'Time' },
				},
				y: { beginAtZero: true, title: { display: true, text: 'Value' } },
			},
		};
		return ChartUtils.deepMerge(defaults, overrides);
	}

	static createLineDataset(label, data, colors = ChartThemes.PRIMARY, overrides = {}) {
		return {
			label: label,
			data: data,
			borderColor: colors.borderColor,
			backgroundColor: colors.backgroundColor,
			fill: true,
			tension: 0.4,
			pointRadius: 3,
			borderWidth: 2,
			...overrides,
		};
	}

	static transformData(rawData, timestampKey = 'timestamp', valueKey = 'value') {
		if (!Array.isArray(rawData)) {
			console.warn('[ChartUtils] Data is not an array:', rawData);
			return [];
		}

		return rawData
			.filter((item) => {
				const hasTimestamp = item && item[timestampKey] != null;
				const hasValue = item && item[valueKey] != null;

				if (!hasTimestamp || !hasValue) {
					console.debug(`[ChartUtils] Skipping item missing keys: ${timestampKey} or ${valueKey}`, item);
				}
				return hasTimestamp && hasValue;
			})
			.map((item) => ({
				x: new Date(item[timestampKey]),
				y: parseFloat(item[valueKey]) || 0,
			}));
	}
}

class ChartManager {
	constructor(canvasId, options = {}) {
		this.canvasId = canvasId;
		this.options = {
			loadingText: options.loadingText || 'Loading Data...',
			emptyText: options.emptyText || 'No data available.',
			theme: options.theme || ChartThemes.PRIMARY,
		};
		this.chart = null;
		this.currentState = null;
		this._init();
	}

	_init() {
		const $canvas = $(`#${this.canvasId}`);
		if (!$canvas.length) return;
		if (!$canvas.parent().hasClass('chart-manager-wrapper')) {
			$canvas.wrap('<div class="chart-manager-wrapper" style="position:relative; min-height:200px; width:100%;"></div>');
			this._createOverlays($canvas.parent());
		}
		this.$canvas = $canvas;
	}

	_createOverlays($wrapper) {
		const overlayStyles =
			'position:absolute; top:0; left:0; width:100%; height:100%; display:none; flex-direction:column; justify-content:center; align-items:center; z-index:10; background:rgba(255,255,255,0.9);';

		this.$loading = $(
			`<div style="${overlayStyles}"><div class="spinner-border ${this.options.theme.spinnerColor}"></div><p class="mt-2 text-muted">${this.options.loadingText}</p></div>`,
		);
		this.$empty = $(
			`<div style="${overlayStyles}"><i class="${this.options.theme.emptyIcon} fa-2x text-muted mb-2"></i><p class="text-muted">${this.options.emptyText}</p></div>`,
		);

		$wrapper.append(this.$loading, this.$empty);
	}

	setUIState(state) {
		this.currentState = state;
		this.$loading?.hide();
		this.$empty?.hide();
		this.$canvas?.hide();

		if (state === ChartUIState.LOADING) this.$loading.show().css('display', 'flex');
		else if (state === ChartUIState.EMPTY) this.$empty.show().css('display', 'flex');
		else if (state === ChartUIState.DATA) this.$canvas.show();
	}

	destroy() {
		if (this.chart) {
			this.chart.destroy();
			this.chart = null;
		}
	}

	renderLineChart({ label, data, colors }) {
		const ctx = document.getElementById(this.canvasId)?.getContext('2d');
		if (!ctx) return;

		this.destroy();
		const options = ChartUtils.getTimeSeriesOptions({ scales: { y: { title: { text: label } } } });
		const dataset = ChartUtils.createLineDataset(label, data, colors);

		this.chart = new Chart(ctx, {
			type: 'line',
			data: { datasets: [dataset] },
			options: options,
		});
		this.setUIState(ChartUIState.DATA);
	}
}

class ChartFactory {
	static createTemperatureChart(canvasId) {
		const manager = new ChartManager(canvasId, { theme: ChartThemes.TEMPERATURE });
		return {
			manager,
			render(data) {
				const chartData = ChartUtils.transformData(data, 'timestamp', 'avgTempC');
				if (!chartData.length) return manager.setUIState(ChartUIState.EMPTY);
				manager.renderLineChart({ label: 'Temp (°C)', data: chartData, colors: ChartThemes.TEMPERATURE });
			},
		};
	}

	static createPowerChart(canvasId) {
		const manager = new ChartManager(canvasId, { theme: ChartThemes.POWER });
		return {
			manager,
			render(data) {
				const chartData = ChartUtils.transformData(data, 'timestamp', 'sumWatt');
				if (!chartData.length) return manager.setUIState(ChartUIState.EMPTY);
				manager.renderLineChart({ label: 'Power (W)', data: chartData, colors: ChartThemes.POWER });
			},
		};
	}

	static createHumidityChart(canvasId) {
		const manager = new ChartManager(canvasId, { theme: ChartThemes.HUMIDITY });
		return {
			manager,
			render(data) {
				const chartData = ChartUtils.transformData(data, 'timestamp', 'avgHumidity');
				if (!chartData.length) return manager.setUIState(ChartUIState.EMPTY);
				manager.renderLineChart({ label: 'Humidity (%)', data: chartData, colors: ChartThemes.HUMIDITY });
			},
		};
	}
}

// Global Exports
window.ChartUtils = ChartUtils;
window.ChartManager = ChartManager;
window.ChartFactory = ChartFactory;
window.ChartUIState = ChartUIState;
window.ChartThemes = ChartThemes;
