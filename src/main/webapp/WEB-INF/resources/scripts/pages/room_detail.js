class RoomDetailPage {
	static instance;

	CONFIG = {
		DATE_FORMAT: 'HH:mm DD/MM/YYYY',
		DEFAULT_RANGE_MONTHS: 1,
	};

	constructor(roomId, initialTempData, initialPowerData) {
		if (RoomDetailPage.instance) return RoomDetailPage.instance;
		RoomDetailPage.instance = this;

		this.roomId = roomId;
		this.initialTempData = initialTempData || [];
		this.initialPowerData = initialPowerData || [];

		this.roomService = window.roomApiV1Service;
		this.healthService = window.healthCheckApiV1Service;
		this.lightService = window.lightApiV1Service;

		this.state = this._parseUrlParams();
		this.charts = {};

		if (this._shouldRedirect()) {
			this._navigateToCurrentRange();
		} else {
			this.init();
		}
	}

	init() {
		try {
			this.initCharts();
			this.initDateRangePicker();
			this.renderInitialData();
			this.loadHealthScore();
			this.bindEvents();
		} catch (error) {
			console.error('[RoomDetailPage] Init failed:', error);
			notify.error('Failed to initialize dashboard');
		}
	}

	initCharts() {
		this.charts = {
			temp: ChartFactory.createTemperatureChart('tempChart'),
			power: ChartFactory.createPowerChart('powerChart'),
		};
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
			const uiConfig = await this.healthService.getRoomHealthUiConfig(this.roomId);

			$badge.removeClass('badge-secondary badge-success badge-warning badge-danger badge-info').addClass(uiConfig.className).css('opacity', 1);
			$text.text(`${uiConfig.score}%`);
			$icon.attr('class', `fas ${uiConfig.icon} mr-1`);
		} catch (error) {
			$badge.addClass('badge-secondary').css('opacity', 1);
			$text.text('N/A');
		}
	}

	bindEvents() {
		$('#btn-refresh-chart').on('click', (e) => {
			$(e.currentTarget).find('i').addClass('fa-spin');
			this._navigateToCurrentRange();
		});

		$('#btn-refresh-all').on('click', async (e) => {
			const $icon = $(e.currentTarget).find('i');
			$icon.addClass('fa-spin');
			await this.loadHealthScore();
			notify.success('Health data refreshed');
			this._navigateToCurrentRange();
		});

		$('#lightsList').on('change', '.light-toggle', (e) => this.handleLightToggle($(e.currentTarget)));
	}

	async handleLightToggle($input) {
		const lightId = $input.data('light-id');
		const isChecked = $input.is(':checked');
		const lightName = $input.closest('.device-item').find('.device-name').text() || 'Light';

		$input.prop('disabled', true);

		try {
			await this.lightService.toggleState(lightId);
			notify.success(`${lightName} turned ${isChecked ? 'ON' : 'OFF'}`);
			// setTimeout(() => this.loadHealthScore(), 500); Hiện không cần load lại điểm sức khỏe khi bật/tắt đèn
		} catch (error) {
			notify.error(error.message || `Failed to toggle ${lightName}`);
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
		window.location.href = `${window.location.pathname}?${query.toString()}`;
	}

	initDateRangePicker() {
		$('#date-range-picker').daterangepicker(
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
