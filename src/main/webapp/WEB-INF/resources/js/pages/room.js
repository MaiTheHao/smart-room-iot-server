import { getDevicesByRoom, controlAc, controlFan, controlLight } from '../api/device.api.js';
import { getAverageHistory } from '../api/temperature.api.js';
import { getEnergyMetricHistory } from '../api/metric.api.js';

/**
 * Room Page Module Orchestrator
 */
const RoomPage = {
	state: {
		config: null,
		roomId: null,
		i18n: {},
		devices: [],
		deviceCharts: {}, // { deviceId: { chart, currentType, currentRange } }
		isInteracting: false,
		pollingInterval: null,
	},

	async init() {
		this.state.config = window.__ROOM_CONFIG__;
		if (!this.state.config) return;

		this.state.roomId = this.state.config.roomId;
		this.state.i18n = this.state.config.i18n;

		// Initialize Components
		RoomAnalytics.init();
		DeviceManager.init();
		DeviceController.bindEvents();

		// Initial Sync
		await DeviceManager.syncDevices();
		DeviceManager.startPolling();
	},
};

/**
 * Global Room Analytics (Main Charts)
 */
const RoomAnalytics = {
	charts: { temp: null, power: null },

	init() {
		this.initMainCharts();
		const now = new Date();
		const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);
		this.updateMainCharts(yesterday.toISOString(), now.toISOString());
		this.bindGlobalPicker();
	},

	initMainCharts() {
		const commonOptions = {
			chart: {
				height: 300,
				type: 'area',
				toolbar: { show: false },
				fontFamily: 'inherit',
				animations: { enabled: true },
			},
			dataLabels: { enabled: false },
			stroke: { curve: 'smooth', width: 3 },
			xaxis: {
				type: 'datetime',
				labels: { style: { colors: '#94a3b8', fontSize: '11px' } },
			},
			grid: { borderColor: '#f1f5f9', strokeDashArray: 4 },
			tooltip: { x: { format: 'dd MMM HH:mm' } },
			yaxis: {
				labels: {
					formatter: (val) => (val?.toFixed ? val.toFixed(2) : val),
					style: { colors: '#94a3b8', fontSize: '11px' },
				},
			},
			fill: {
				type: 'gradient',
				gradient: { shadeIntensity: 1, opacityFrom: 0.4, opacityTo: 0.05, stops: [0, 90, 100] },
			},
		};

		const tempEl = document.querySelector('#tempChart');
		const powerEl = document.querySelector('#powerChart');

		if (tempEl && window.ApexCharts) {
			this.charts.temp = new ApexCharts(tempEl, {
				...commonOptions,
				series: [{ name: RoomPage.state.i18n.temp, data: [] }],
				colors: ['#ef4444'],
			});
			this.charts.temp.render();
		}
		if (powerEl && window.ApexCharts) {
			this.charts.power = new ApexCharts(powerEl, {
				...commonOptions,
				series: [{ name: RoomPage.state.i18n.power, data: [] }],
				colors: ['#f59e0b'],
			});
			this.charts.power.render();
		}
	},

	async updateMainCharts(from, to) {
		this.updateTempChart(from, to);
		this.updatePowerChart(from, to);
	},

	async updateTempChart(from, to) {
		const el = document.querySelector('#tempChart');
		if (!el) return;

		const [err, res] = await getAverageHistory(RoomPage.state.roomId, from, to);
		if (err || !res.data) return;

		const data = res.data;
		if (data.length > 0) {
			const avg = (data.reduce((sum, item) => sum + item.avgTempC, 0) / data.length).toFixed(2);
			const badge = document.querySelector('#avgTempBadge');
			if (badge) badge.textContent = `Avg: ${avg}°C`;

			this.charts.temp.updateSeries([{
				name: RoomPage.state.i18n.temp,
				data: data.map((item) => ({ x: new Date(item.timestamp).getTime(), y: item.avgTempC }))
			}]);
		}
	},

	async updatePowerChart(from, to) {
		const el = document.querySelector('#powerChart');
		if (!el) return;

		const [err, res] = await getEnergyMetricHistory({ category: 'ROOM', targetId: RoomPage.state.roomId, from, to });
		if (err || !res.data) return;

		const data = res.data;
		if (data.length > 0) {
			const peak = Math.max(...data.map((item) => item.power || 0)).toFixed(2);
			const badge = document.querySelector('#peakPowerBadge');
			if (badge) badge.textContent = `Peak: ${peak}W`;

			this.charts.power.updateSeries([{
				name: RoomPage.state.i18n.power,
				data: data.map((item) => ({ x: new Date(item.timestamp).getTime(), y: item.power || 0 }))
			}]);
		}
	},

	bindGlobalPicker() {
		if (window.flatpickr) {
			flatpickr('#analyticsRange', {
				mode: 'range',
				enableTime: true,
				time_24hr: true,
				altInput: true,
				altFormat: 'd/m/Y H:i',
				dateFormat: 'Z',
				defaultDate: [new Date(Date.now() - 24 * 60 * 60 * 1000), new Date()],
				onClose: (dates) => {
					if (dates.length === 2) this.updateMainCharts(dates[0].toISOString(), dates[1].toISOString());
				},
			});
		}
	},
};

/**
 * Device Manager (Sync & Polling)
 */
const DeviceManager = {
	init() {
		const container = document.querySelector('#controls-container');
		if (container) {
			container.innerHTML = `
                <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 mt-5">
                    <h5 class="m-0 fw-bold text-dark d-flex align-items-center">
                        <i data-lucide="settings-2" class="text-primary me-2"></i>
                        <span>${RoomPage.state.i18n.controls}</span>
                    </h5>
                </div>
                <div class="card border-0 rounded-4 overflow-hidden shadow-sm">
                    <div class="card-header bg-white border-0 py-3">
                        <ul class="nav nav-pills gap-2 p-1 bg-light rounded-pill" id="deviceTabs" role="tablist" style="width: fit-content">
                            <li class="nav-item"><button class="nav-link active rounded-pill px-4" data-bs-toggle="tab" data-bs-target="#ac-pane"><i data-lucide="wind" class="me-2"></i>${RoomPage.state.i18n.ac}</button></li>
                            <li class="nav-item"><button class="nav-link rounded-pill px-4" data-bs-toggle="tab" data-bs-target="#fan-pane"><i data-lucide="fan" class="me-2"></i>${RoomPage.state.i18n.fan}</button></li>
                            <li class="nav-item"><button class="nav-link rounded-pill px-4" data-bs-toggle="tab" data-bs-target="#light-pane"><i data-lucide="lightbulb" class="me-2"></i>${RoomPage.state.i18n.light}</button></li>
                        </ul>
                    </div>
                    <div class="card-body p-0 border-top">
                        <div class="tab-content">
                            <div class="tab-pane fade show active p-4" id="ac-pane"></div>
                            <div class="tab-pane fade p-4" id="fan-pane"></div>
                            <div class="tab-pane fade p-4" id="light-pane"></div>
                        </div>
                    </div>
                </div>
            `;
			if (window.lucide) lucide.createIcons();
		}
	},

	async syncDevices() {
		const [err, res] = await getDevicesByRoom(RoomPage.state.roomId);
		if (err || !res.data || RoomPage.state.isInteracting) return;

		const newDevices = res.data;
		this.renderOrUpdateAll(newDevices);
		RoomPage.state.devices = newDevices;
	},

	renderOrUpdateAll(newDevices) {
		const groups = [
			{ id: '#ac-pane', category: 'AIR_CONDITION', empty: RoomPage.state.i18n.noAc },
			{ id: '#fan-pane', category: 'FAN', empty: RoomPage.state.i18n.noFan },
			{ id: '#light-pane', category: 'LIGHT', empty: RoomPage.state.i18n.noLight },
		];

		groups.forEach((g) => {
			const pane = document.querySelector(g.id);
			if (!pane) return;

			const newItems = newDevices.filter((d) => d.category === g.category);
			const currentItems = RoomPage.state.devices.filter((d) => d.category === g.category);

			const newIds = newItems.map((d) => d.id).sort().join(',');
			const currentIds = currentItems.map((d) => d.id).sort().join(',');

			if (newIds !== currentIds || pane.innerHTML.includes('text-center py-4')) {
				this.renderGroupFull(pane, newItems, g.empty);
			} else {
				newItems.forEach((device) => DeviceRenderer.updateCardStatus(device));
			}
		});
	},

	renderGroupFull(pane, items, emptyMsg) {
		const openIds = Array.from(pane.querySelectorAll('.collapse.show')).map((c) => c.id);
		const activeTabIds = Array.from(pane.querySelectorAll('.nav-link.active')).map((t) => t.dataset.bsTarget);

		// Clean up chart state for devices being re-rendered
		pane.querySelectorAll('.device-chart-container').forEach((c) => {
			const nid = c.dataset.naturalId;
			if (RoomPage.state.deviceCharts[nid]) {
				RoomPage.state.deviceCharts[nid].chart?.destroy();
				delete RoomPage.state.deviceCharts[nid];
			}
		});

		pane.innerHTML = items.map((d) => DeviceRenderer.renderCard(d)).join('') || `<p class="text-muted text-center py-4">${emptyMsg}</p>`;

		openIds.forEach((id) => pane.querySelector(`#${id}`)?.classList.add('show'));
		activeTabIds.forEach((tid) => {
			const tabBtn = pane.querySelector(`[data-bs-target="${tid}"]`);
			if (tabBtn) {
				const tabList = tabBtn.closest('ul');
				if (tabList) tabList.querySelectorAll('.nav-link').forEach((nl) => nl.classList.remove('active'));
				tabBtn.classList.add('active');
			}
			const tabPane = pane.querySelector(tid);
			if (tabPane) {
				const tabContent = tabPane.closest('.tab-content');
				if (tabContent) tabContent.querySelectorAll('.tab-pane').forEach((tp) => tp.classList.remove('show', 'active'));
				tabPane.classList.add('show', 'active');
			}
		});

		// Re-initialize charts for restored analytics tabs
		activeTabIds.forEach((tid) => {
			if (tid?.startsWith('#analytics')) {
				const aPane = pane.querySelector(tid);
				if (aPane) {
					const cc = aPane.querySelector('.device-chart-container');
					if (cc) DeviceChart.init(cc.dataset.naturalId);
				}
			}
		});

		if (window.lucide) lucide.createIcons();
	},

	startPolling() {
		if (RoomPage.state.pollingInterval) clearInterval(RoomPage.state.pollingInterval);
		RoomPage.state.pollingInterval = setInterval(() => {
			if (!RoomPage.state.isInteracting) this.syncDevices();
		}, 5000);
	},
};

/**
 * Device Renderer (HTML Templates)
 */
const DeviceRenderer = {
	renderCard(device) {
		const isActive = device.power === 'ON';
		const icon = this.getCategoryIcon(device.category);
		const colorClass = this.getCategoryColor(device.category, isActive);

		return `
            <div class="mb-4 device-item" data-id="${device.id}" data-natural-id="${device.naturalId}" data-category="${device.category}">
                <div class="d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center flex-grow-1 overflow-hidden">
                        <div class="d-flex align-items-center justify-content-center rounded-3 me-3 flex-shrink-0 ${colorClass}" style="width: 48px; height: 48px">
                            <i data-lucide="${icon}"></i>
                        </div>
                        <div class="overflow-hidden">
                            <h6 class="mb-0 fw-bold text-dark small text-truncate">${device.name}</h6>
                            <small class="text-muted tiny text-truncate d-block">${device.description || ''}</small>
                        </div>
                    </div>
                    <div class="d-flex align-items-center gap-3 ms-3">
                        <div class="position-relative">
                            <input type="checkbox" class="btn-check master-switch" id="switch${device.naturalId}" ${isActive ? 'checked' : ''} autocomplete="off">
                            <label class="btn ${isActive ? 'btn-success border-success' : 'btn-outline-secondary'} rounded-pill px-3 py-1 d-flex align-items-center gap-2 shadow-sm" for="switch${device.naturalId}" style="font-size: 0.75rem; border-width: 2px">
                                <i data-lucide="power" style="width: 14px; height: 14px"></i>
                                <span class="fw-bold">${isActive ? RoomPage.state.i18n.on : RoomPage.state.i18n.off}</span>
                            </label>
                        </div>
                        <button class="btn btn-link text-muted p-0 border-0" data-bs-toggle="collapse" data-bs-target="#deviceContent${device.naturalId}">
                            <i data-lucide="chevron-down" style="width: 18px"></i>
                        </button>
                    </div>
                </div>
                <div class="collapse mt-3" id="deviceContent${device.naturalId}">
                    <div class="card border-0 bg-light rounded-4 overflow-hidden">
                        <div class="card-header bg-transparent border-0 p-2">
                            <ul class="nav nav-pills nav-fill gap-2 p-1 bg-white bg-opacity-50 rounded-4" role="tablist">
                                <li class="nav-item">
                                    <button class="nav-link active py-1 tiny rounded-4 shadow-none" data-bs-toggle="tab" data-bs-target="#control${device.naturalId}">
                                        <i data-lucide="sliders" class="me-1" style="width:12px"></i>Control
                                    </button>
                                </li>
                                <li class="nav-item">
                                    <button class="nav-link py-1 tiny rounded-4 device-analytics-tab shadow-none" data-bs-toggle="tab" data-bs-target="#analytics${device.naturalId}">
                                        <i data-lucide="bar-chart-2" class="me-1" style="width:12px"></i>Analytics
                                    </button>
                                </li>
                            </ul>
                        </div>
                        <div class="tab-content">
                            <div class="tab-pane fade show active p-3" id="control${device.naturalId}">
                                ${this.renderControlPane(device, isActive)}
                            </div>
                            <div class="tab-pane fade p-3" id="analytics${device.naturalId}">
                                ${this.renderAnalyticsPane(device)}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
	},

	updateCardStatus(device) {
		const card = document.querySelector(`.device-item[data-natural-id="${device.naturalId}"]`);
		if (!card) return;

		const isActive = device.power === 'ON';

		const masterSwitch = card.querySelector('.master-switch');
		if (masterSwitch && masterSwitch.checked !== isActive) {
			masterSwitch.checked = isActive;
		}

		const label = card.querySelector(`label[for="switch${device.naturalId}"]`);
		const iconContainer = card.querySelector('.rounded-3');
		const statusText = label?.querySelector('span');

		if (label) {
			label.className = `btn ${isActive ? 'btn-success border-success' : 'btn-outline-secondary'} rounded-pill px-3 py-1 d-flex align-items-center gap-2 shadow-sm`;
			if (statusText) statusText.textContent = isActive ? RoomPage.state.i18n.on : RoomPage.state.i18n.off;
		}

		if (iconContainer) {
			iconContainer.className = `d-flex align-items-center justify-content-center rounded-3 me-3 flex-shrink-0 ${this.getCategoryColor(device.category, isActive)}`;
		}

		if (device.category === 'AIR_CONDITION') {
			const tempVal = card.querySelector('.ac-temp-value');
			if (tempVal) tempVal.textContent = device.temperature || 25;

			const modeBtns = card.querySelectorAll('.ac-mode-btn');
			modeBtns.forEach((btn) => {
				const isCurrentMode = btn.dataset.mode === device.mode;
				btn.className = `btn btn-sm rounded-pill ac-mode-btn shadow-none ${isCurrentMode ? 'btn-primary' : 'btn-outline-secondary'}`;
				btn.disabled = !isActive;
			});

			const fanBadge = card.querySelector('.ac-fanspeed-range')?.previousElementSibling?.querySelector('.badge');
			if (fanBadge) fanBadge.textContent = device.fanSpeed || 0;

			const swingSwitch = card.querySelector('.ac-swing-switch');
			if (swingSwitch) swingSwitch.checked = device.swing === 'ON';
		} else if (device.category === 'FAN') {
			const speedBadge = card.querySelector('.fan-speed-range')?.previousElementSibling?.querySelector('.badge');
			if (speedBadge) speedBadge.textContent = device.speed || 0;

			const swingSwitch = card.querySelector('.fan-swing-switch');
			if (swingSwitch) swingSwitch.checked = device.swing === 'ON';

			const lightSwitch = card.querySelector('.fan-light-switch');
			if (lightSwitch) lightSwitch.checked = device.light === 'ON';
		} else if (device.category === 'LIGHT') {
			const levelBadge = card.querySelector('.light-level-range')?.previousElementSibling?.querySelector('.badge');
			if (levelBadge) levelBadge.textContent = `${device.level || 0}%`;
		}

		const inputs = card.querySelectorAll('input:not(.master-switch):not(.device-chart-range), button:not([data-bs-toggle]):not(.chart-type-group button)');
		inputs.forEach((input) => (input.disabled = !isActive));
	},

	renderControlPane(device, isActive) {
		if (device.category === 'AIR_CONDITION') return this.renderAcControl(device, isActive);
		if (device.category === 'FAN') return this.renderFanControl(device, isActive);
		if (device.category === 'LIGHT') return this.renderLightControl(device, isActive);
		return '';
	},

	renderAnalyticsPane(device) {
		const i18n = RoomPage.state.i18n;
		return `
            <div class="device-chart-container" data-id="${device.id}" data-natural-id="${device.naturalId}" data-category="${device.category}">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <div class="btn-group btn-group-sm chart-type-group shadow-none" role="group">
                        <button type="button" class="btn btn-outline-primary active tiny" data-type="power" title="${i18n.metricPower}">W</button>
                        <button type="button" class="btn btn-outline-primary tiny" data-type="voltage" title="${i18n.metricVoltage}">V</button>
                        <button type="button" class="btn btn-outline-primary tiny" data-type="current" title="${i18n.metricCurrent}">A</button>
                        <button type="button" class="btn btn-outline-primary tiny" data-type="energy" title="${i18n.metricEnergy}">kWh</button>
                    </div>
                    <div class="input-group input-group-sm shadow-none" style="width: 140px">
                        <input type="text" class="form-control border-0 bg-white device-chart-range tiny fw-bold" placeholder="Range" readonly>
                    </div>
                </div>
                <div class="device-chart-el" style="min-height: 200px"></div>
            </div>
        `;
	},

	renderAcControl(ac, isActive) {
		const modes = ['COOL', 'HEAT', 'DRY', 'FAN', 'AUTO'];
		return `
            <div class="text-center mb-4">
                <div class="d-flex justify-content-center align-items-center">
                    <button class="btn btn-outline-secondary rounded-circle ac-temp-btn shadow-none" data-delta="-1" ${!isActive ? 'disabled' : ''}><i data-lucide="minus"></i></button>
                    <div class="mx-4"><span class="fw-bold h2 m-0 ac-temp-value">${ac.temperature || 25}</span><span class="ms-1 text-muted">°C</span></div>
                    <button class="btn btn-outline-secondary rounded-circle ac-temp-btn shadow-none" data-delta="1" ${!isActive ? 'disabled' : ''}><i data-lucide="plus"></i></button>
                </div>
            </div>
            <div class="mb-4 pt-3 border-top border-white text-center">
                <div class="d-flex justify-content-center flex-wrap gap-2">
                    ${modes
						.map(
							(m) => `
                        <button class="btn btn-sm rounded-pill ac-mode-btn shadow-none ${ac.mode === m ? 'btn-primary' : 'btn-outline-secondary'}" data-mode="${m}" ${!isActive ? 'disabled' : ''}>
                            <i data-lucide="${this.getAcIcon(m)}" class="me-1" style="width:14px"></i>${m}
                        </button>
                    `,
						)
						.join('')}
                </div>
            </div>
            <div class="bg-white rounded-3 p-3 border border-light">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <small class="fw-bold text-muted tiny">${RoomPage.state.i18n.fanSpeed}</small>
                    <span class="badge bg-primary rounded-pill tiny">${ac.fanSpeed || 0}</span>
                </div>
                <input type="range" class="form-range ac-fanspeed-range" min="0" max="5" value="${ac.fanSpeed || 0}" ${!isActive ? 'disabled' : ''}>
                <div class="d-flex justify-content-between align-items-center pt-2 mt-2 border-top">
                    <small class="fw-bold text-muted tiny">${RoomPage.state.i18n.swing}</small>
                    <div class="form-check form-switch"><input class="form-check-input ac-swing-switch" type="checkbox" ${ac.swing === 'ON' ? 'checked' : ''} ${!isActive ? 'disabled' : ''}></div>
                </div>
            </div>
        `;
	},

	renderFanControl(fan, isActive) {
		const isIR = fan.type === 'IR';
		return `
            <div class="bg-white rounded-3 p-3 border border-light">
                <div class="mb-3">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <small class="fw-bold text-muted tiny">${RoomPage.state.i18n.fanSpeed}</small>
                        <span class="badge bg-warning text-dark rounded-pill tiny">${fan.speed || 0}</span>
                    </div>
                    <input type="range" class="form-range fan-speed-range" min="0" max="3" value="${fan.speed || 0}" ${!isActive ? 'disabled' : ''}>
                </div>
                ${
					isIR
						? `
                    <div class="d-flex justify-content-between align-items-center mb-2 pt-2 border-top">
                        <small class="fw-bold text-muted tiny">${RoomPage.state.i18n.swing}</small>
                        <div class="form-check form-switch"><input class="form-check-input fan-swing-switch" type="checkbox" ${fan.swing === 'ON' ? 'checked' : ''} ${!isActive ? 'disabled' : ''}></div>
                    </div>
                    <div class="d-flex justify-content-between align-items-center">
                        <small class="fw-bold text-muted tiny">${RoomPage.state.i18n.light}</small>
                        <div class="form-check form-switch"><input class="form-check-input fan-light-switch" type="checkbox" ${fan.light === 'ON' ? 'checked' : ''} ${!isActive ? 'disabled' : ''}></div>
                    </div>
                `
						: ''
				}
            </div>
        `;
	},

	renderLightControl(light, isActive) {
		return `
            <div class="bg-white rounded-3 p-3 border border-light">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <small class="fw-bold text-muted tiny">${RoomPage.state.i18n.brightness}</small>
                    <span class="badge bg-success rounded-pill tiny">${light.level || 0}%</span>
                </div>
                <input type="range" class="form-range light-level-range" min="0" max="100" value="${light.level || 0}" ${!isActive ? 'disabled' : ''}>
            </div>
        `;
	},

	getCategoryIcon(cat) {
		if (cat === 'AIR_CONDITION') return 'wind';
		if (cat === 'FAN') return 'fan';
		return 'lightbulb';
	},

	getCategoryColor(cat, active) {
		if (!active) return 'bg-light text-muted';
		if (cat === 'AIR_CONDITION') return 'bg-primary bg-opacity-10 text-primary';
		if (cat === 'FAN') return 'bg-warning bg-opacity-10 text-warning';
		return 'bg-success bg-opacity-10 text-success';
	},

	getAcIcon(mode) {
		const map = { COOL: 'snowflake', HEAT: 'flame', DRY: 'droplets', FAN: 'fan', AUTO: 'sparkles' };
		return map[mode] || 'circle';
	},
};

/**
 * Device Chart Manager (Analytics Logic)
 */
const DeviceChart = {
	async init(naturalId) {
		const container = document.querySelector(`.device-chart-container[data-natural-id="${naturalId}"]`);
		if (!container || RoomPage.state.deviceCharts[naturalId]) return;

		const chartEl = container.querySelector('.device-chart-el');
		const rangeInput = container.querySelector('.device-chart-range');
		const category = container.dataset.category;
		const targetId = parseInt(container.dataset.id);

		// Initialize State
		RoomPage.state.deviceCharts[naturalId] = {
			chart: null,
			currentType: 'power',
			category: category,
			targetId: targetId,
			data: [],
			range: { from: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(), to: new Date().toISOString() },
		};

		// Init Flatpickr
		if (window.flatpickr) {
			flatpickr(rangeInput, {
				enableTime: true,
				time_24hr: true,
				dateFormat: 'Z',
				altInput: true,
				altFormat: 'H:i',
				defaultDate: [new Date(RoomPage.state.deviceCharts[naturalId].range.from), new Date(RoomPage.state.deviceCharts[naturalId].range.to)],
				mode: 'range',
				onClose: (dates) => {
					if (dates.length === 2) {
						RoomPage.state.deviceCharts[naturalId].range = { from: dates[0].toISOString(), to: dates[1].toISOString() };
						this.refreshData(naturalId);
					}
				},
			});
		}

		// Init Chart
		const options = {
			chart: { height: 200, type: 'line', toolbar: { show: false }, animations: { enabled: true } },
			stroke: { curve: 'smooth', width: 2 },
			series: [{ name: 'Value', data: [] }],
			xaxis: { type: 'datetime', labels: { style: { fontSize: '10px' } } },
			yaxis: { labels: { style: { fontSize: '10px' } } },
			colors: ['#3b82f6'],
		};

		RoomPage.state.deviceCharts[naturalId].chart = new ApexCharts(chartEl, options);
		await RoomPage.state.deviceCharts[naturalId].chart.render();

		// Fetch & Render
		await this.refreshData(naturalId);
	},

	async refreshData(naturalId) {
		const state = RoomPage.state.deviceCharts[naturalId];
		const [err, res] = await getEnergyMetricHistory({
			category: state.category,
			targetId: state.targetId,
			from: state.range.from,
			to: state.range.to,
		});

		if (err || !res.data) return;

		state.data = res.data;
		this.updateChart(naturalId);
	},

	updateChart(naturalId) {
		const state = RoomPage.state.deviceCharts[naturalId];
		if (!state || !state.chart) return;
		
		const type = state.currentType;
		const seriesData = state.data.map((item) => ({
			x: new Date(item.timestamp).getTime(),
			y: item[type] || 0,
		}));

		state.chart.updateSeries([{ name: RoomPage.state.i18n[`metric${type.charAt(0).toUpperCase() + type.slice(1)}`], data: seriesData }]);
	},

	switchType(naturalId, newType) {
		if (!RoomPage.state.deviceCharts[naturalId]) return;
		RoomPage.state.deviceCharts[naturalId].currentType = newType;
		this.updateChart(naturalId);
	},
};

/**
 * Device Controller (Event Handling)
 */
const DeviceController = {
	bindEvents() {
		const container = document.querySelector('#controls-container');
		if (!container) return;

		// 1. Control Events
		container.addEventListener('change', (e) => this.handleChange(e));
		container.addEventListener('click', (e) => this.handleClick(e));

		// 2. Polling Guard for range inputs
		container.addEventListener('mousedown', (e) => e.target.type === 'range' && (RoomPage.state.isInteracting = true));
		container.addEventListener('mouseup', (e) => e.target.type === 'range' && (RoomPage.state.isInteracting = false));
		container.addEventListener('touchstart', (e) => e.target.type === 'range' && (RoomPage.state.isInteracting = true));
		container.addEventListener('touchend', (e) => e.target.type === 'range' && (RoomPage.state.isInteracting = false));
	},

	async handleChange(e) {
		const target = e.target;
		const item = target.closest('.device-item');
		if (!item) return;

		const { id, naturalId, category } = item.dataset;
		let payload = {};

		if (target.classList.contains('master-switch')) payload.power = target.checked ? 'ON' : 'OFF';
		else if (target.classList.contains('ac-fanspeed-range')) payload.fanSpeed = parseInt(target.value);
		else if (target.classList.contains('ac-swing-switch')) payload.swing = target.checked ? 'ON' : 'OFF';
		else if (target.classList.contains('fan-speed-range')) payload.speed = parseInt(target.value);
		else if (target.classList.contains('fan-swing-switch')) payload.swing = target.checked ? 'ON' : 'OFF';
		else if (target.classList.contains('fan-light-switch')) payload.light = target.checked ? 'ON' : 'OFF';
		else if (target.classList.contains('light-level-range')) payload.level = parseInt(target.value);

		if (Object.keys(payload).length > 0) await this.handleApiCall(category, naturalId, payload);
	},

	async handleClick(e) {
		const target = e.target;

		// AC Temp/Mode
		const acBtn = target.closest('.ac-temp-btn, .ac-mode-btn');
		if (acBtn) {
			const item = acBtn.closest('.device-item');
			const { naturalId } = item.dataset;
			if (acBtn.classList.contains('ac-temp-btn')) {
				const valEl = item.querySelector('.ac-temp-value');
				const newVal = parseInt(valEl.textContent) + parseInt(acBtn.dataset.delta);
				if (newVal >= 16 && newVal <= 32) await this.handleApiCall('AC', naturalId, { temperature: newVal });
			} else {
				await this.handleApiCall('AC', naturalId, { mode: acBtn.dataset.mode });
			}
			return;
		}

		// Analytics Tab Click
		const tabBtn = target.closest('.device-analytics-tab');
		if (tabBtn) {
			const item = tabBtn.closest('.device-item');
			DeviceChart.init(item.dataset.naturalId);
			return;
		}

		// Chart Type Switch
		const typeBtn = target.closest('.chart-type-group button');
		if (typeBtn) {
			const item = typeBtn.closest('.device-item');
			typeBtn.parentElement.querySelectorAll('button').forEach((b) => b.classList.remove('active'));
			typeBtn.classList.add('active');
			DeviceChart.switchType(item.dataset.naturalId, typeBtn.dataset.type);
		}
	},

	async handleApiCall(category, naturalId, payload) {
		RoomPage.state.isInteracting = true;
		let err, res;
		const type = category === 'AIR_CONDITION' ? 'AC' : category;

		if (type === 'AC') [err, res] = await controlAc(naturalId, payload);
		else if (type === 'FAN') [err, res] = await controlFan(naturalId, payload);
		else if (type === 'LIGHT') [err, res] = await controlLight(naturalId, payload);

		RoomPage.state.isInteracting = false;

		if (err) {
			window.Swal?.fire({ title: RoomPage.state.i18n.errorTitle, text: RoomPage.state.i18n.errorControl, icon: 'error', toast: true, position: 'top-end', showConfirmButton: false, timer: 3000 });
		} else if (res?.data) {
			const deviceName = document.querySelector(`.device-item[data-natural-id="${naturalId}"] h6`)?.textContent || 'Device';
			const msg = RoomPage.state.i18n.controlStatus.replace('{0}', deviceName).replace('{1}', res.data.successCount).replace('{2}', res.data.totalCount);
			window.Swal?.fire({ text: msg, icon: 'info', toast: true, position: 'top-end', showConfirmButton: false, timer: 3000 });
		}
		DeviceManager.syncDevices();
	},
};

// Initialize App
document.addEventListener('DOMContentLoaded', () => RoomPage.init());
