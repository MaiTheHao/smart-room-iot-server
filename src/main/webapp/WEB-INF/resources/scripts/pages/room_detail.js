class RoomDashboardSSR {
	constructor(roomId, initialTempData, initialPowerData) {
		this.roomId = roomId;
		this.initialTempData = initialTempData || [];
		this.initialPowerData = initialPowerData || [];

		this.charts = {
			temp: ChartFactory.createTemperatureChart('tempChart'),
			power: ChartFactory.createPowerChart('powerChart'),
		};

		this.dateRange = {
			start: moment().subtract(15, 'minutes'),
			end: moment(),
		};

		this.init();
	}

	init() {
		this.initDateRangePicker();
		this.renderInitialData();
		this.bindEvents();
	}

	renderInitialData() {
		try {
			// --- Temperature Chart (Dữ liệu từ Record) ---
			if (this.initialTempData && this.initialTempData.length > 0) {
				this.charts.temp.render(this.initialTempData, 'avgTempC');
			}

			// --- Power Chart (Dữ liệu từ Class) ---
			if (this.initialPowerData && this.initialPowerData.length > 0) {
				this.charts.power.render(this.initialPowerData, 'sumWatt');
			}
		} catch (error) {
			console.error('[SSR] Error rendering initial data:', error);
		}
	}

	bindEvents() {
		$('#btn-refresh-chart').on('click', () => {
			this.handleRefresh();
		});
		this.bindQuickControls();
	}

	bindQuickControls() {
		$('.light-toggle').on('change', function () {
			const lightId = $(this).data('light-id');
			const isOn = $(this).is(':checked');
			console.log('Light ID:', lightId, 'is now:', isOn);
		});
	}

	handleRefresh() {
		const $refreshIcon = $('#btn-refresh-chart i');
		$refreshIcon.addClass('fa-spin');

		const startedAtIso = this.dateRange.start.toISOString();
		const endedAtIso = this.dateRange.end.toISOString();

		window.location.href = `/room/${this.roomId}?startedAt=${encodeURIComponent(startedAtIso)}&endedAt=${encodeURIComponent(endedAtIso)}`;
	}

	initDateRangePicker() {
		$('#date-range-picker').daterangepicker(
			{
				timePicker: true,
				timePicker24Hour: true,
				startDate: this.dateRange.start,
				endDate: this.dateRange.end,
				locale: { format: 'HH:mm DD/MM/YYYY' },
			},
			(start, end) => {
				this.dateRange.start = start;
				this.dateRange.end = end;
				this.handleRefresh();
			},
		);
	}
}

window.RoomDashboardSSR = RoomDashboardSSR;
