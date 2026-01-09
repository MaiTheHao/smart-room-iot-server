class RoomDashboardSSR {
	CONFIG = {
		DATE_FORMAT: 'HH:mm DD/MM/YYYY',
		DEFAULT_RANGE_MONTHS: 1,
		ENDPOINTS: {
			ROOM_DETAIL: (id) => `/room/${id}`,
		},
	};

	constructor(roomId, initialTempData, initialPowerData) {
		this.roomId = roomId;
		this.initialTempData = initialTempData || [];
		this.initialPowerData = initialPowerData || [];

		this.state = this._parseUrlParams();

		if (this._shouldRedirect()) {
			this._navigateToCurrentRange();
			return;
		}

		this.charts = {
			temp: ChartFactory.createTemperatureChart('tempChart'),
			power: ChartFactory.createPowerChart('powerChart'),
		};

		this.init();
	}

	init() {
		try {
			this.initDateRangePicker();
			this.renderInitialData();
			this.bindEvents();
			console.log(`[Dashboard] Initialized for Room: ${this.roomId}`);
		} catch (error) {
			console.error('[Dashboard] Init failed:', error);
		}
	}

	_parseUrlParams() {
		const params = new URLSearchParams(window.location.search);
		const startedAt = params.get('startedAt');
		const endedAt = params.get('endedAt');

		return {
			isMissingParams: !startedAt || !endedAt,
			start: startedAt ? moment(startedAt) : moment().subtract(this.CONFIG.DEFAULT_RANGE_MONTHS, 'months'),
			end: endedAt ? moment(endedAt) : moment(),
		};
	}

	_shouldRedirect() {
		return this.state.isMissingParams;
	}

	_navigateToCurrentRange() {
		const query = new URLSearchParams({
			startedAt: this.state.start.toISOString(),
			endedAt: this.state.end.toISOString(),
		});
		window.location.href = `${this.CONFIG.ENDPOINTS.ROOM_DETAIL(this.roomId)}?${query.toString()}`;
	}

	renderInitialData() {
		const { temp, power } = this.charts;

		if (this.initialTempData.length) {
			temp.render(this.initialTempData, 'avgTempC');
		}

		if (this.initialPowerData.length) {
			power.render(this.initialPowerData, 'sumWatt');
		}
	}

	bindEvents() {
		$('#btn-refresh-chart').on('click', (e) => {
			$(e.currentTarget).find('i').addClass('fa-spin');
			this._navigateToCurrentRange();
		});

		$('.light-toggle').on('change', function () {
			const data = {
				id: $(this).data('light-id'),
				status: $(this).is(':checked'),
			};
			console.log('[Control] Light Update:', data);
		});
	}

	initDateRangePicker() {
		const $picker = $('#date-range-picker');

		$picker.daterangepicker(
			{
				timePicker: true,
				timePicker24Hour: true,
				startDate: this.state.start,
				endDate: this.state.end,
				locale: { format: this.CONFIG.DATE_FORMAT },
			},
			(start, end) => {
				this.state.start = start;
				this.state.end = end;
				this._navigateToCurrentRange();
			},
		);
	}
}

window.RoomDashboardSSR = RoomDashboardSSR;
