class RoomDetailPage {
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

		this.httpClient = new HttpClient();
		this.roomService = new RoomApiV1Service(this.httpClient);

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
			this.loadHealthScore();
			this.bindEvents();
			console.log(`[Dashboard] Initialized for Room: ${this.roomId}`);
		} catch (error) {
			console.error('[Dashboard] Init failed:', error);
			window.notify.error('Failed to initialize dashboard');
		}
	}

	renderInitialData() {
		const { temp, power } = this.charts;
		if (this.initialTempData.length) temp.render(this.initialTempData, 'avgTempC');
		if (this.initialPowerData.length) power.render(this.initialPowerData, 'sumWatt');
	}

	async loadHealthScore() {
		const $badge = $('#healthScoreBadge');
		const $text = $('#healthScoreValue');
		const $icon = $('#healthIcon');

		try {
			const uiConfig = await this.roomService.getHealthUiConfig(this.roomId);
			$badge.removeClass('badge-secondary badge-success badge-warning badge-danger badge-info').addClass(uiConfig.className);
			$text.text(`${uiConfig.score}%`);
			$icon.attr('class', `fas ${uiConfig.icon} mr-1`);
			$badge.css('opacity', 1);
		} catch (error) {
			console.error('[Dashboard] Load health failed:', error);
			$badge.addClass('badge-secondary').css('opacity', 1);
			$text.text('N/A');
		}
	}

	bindEvents() {
		$('#btn-refresh-chart').on('click', (e) => {
			const $btn = $(e.currentTarget);
			$btn.find('i').addClass('fa-spin');
			this._navigateToCurrentRange();
		});

		$('#btn-refresh-all').on('click', async (e) => {
			const $btn = $(e.currentTarget);
			const $icon = $btn.find('i');

			$icon.addClass('fa-spin');

			await this.loadHealthScore();

			window.notify.success('Health data refreshed');

			this._navigateToCurrentRange();
		});

		$('#lightsList').on('change', '.light-toggle', (e) => this.handleLightToggle(e));
	}

	async handleLightToggle(e) {
		const $input = $(e.currentTarget);
		const lightId = $input.data('light-id');
		const isChecked = $input.is(':checked');
		const lightName = $input.closest('.device-item').find('.device-name').text() || 'Light';

		$input.prop('disabled', true);

		try {
			await this.roomService.toggleLight(lightId);

			window.notify.success(`${lightName} turned ${isChecked ? 'ON' : 'OFF'}`);

			setTimeout(() => this.loadHealthScore(), 500);
		} catch (error) {
			console.error('[Dashboard] Light toggle failed:', error);
			window.notify.error(error.message || `Failed to toggle ${lightName}`);
			$input.prop('checked', !isChecked);
		} finally {
			$input.prop('disabled', false);
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

window.RoomDetailPage = RoomDetailPage;
