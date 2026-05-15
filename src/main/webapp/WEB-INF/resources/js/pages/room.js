import { getDevicesByRoom, controlAc, controlFan, controlLight } from '../api/device.api.js';
import { getAverageHistory } from '../api/temperature.api.js';
import { getEnergyMetricHistory } from '../api/metric.api.js';

document.addEventListener('DOMContentLoaded', () => {
	const config = window.__ROOM_CONFIG__;
	if (!config) return;

	const i18n = {
		ac: config.i18n.ac,
		fan: config.i18n.fan,
		light: config.i18n.light,
		controls: config.i18n.controls,
		loading: config.i18n.loading,
		on: config.i18n.on,
		off: config.i18n.off,
		modes: config.i18n.modes,
		fanSpeed: config.i18n.fanSpeed,
		swing: config.i18n.swing,
		brightness: config.i18n.brightness,
		level: config.i18n.level,
		dark: config.i18n.dark,
		bright: config.i18n.bright,
		temp: config.i18n.temp,
		power: config.i18n.power,
		errorTitle: config.i18n.errorTitle,
		errorFetch: config.i18n.errorFetch,
		errorControl: config.i18n.errorControl,
		successControl: config.i18n.successControl,
		noAc: config.i18n.noAc,
		noFan: config.i18n.noFan,
		noLight: config.i18n.noLight,
	};

	const { roomId } = config;
	let charts = {
		temp: null,
		power: null,
	};

	const getAcModeIcon = (mode) => {
		switch (mode) {
			case 'COOL':
				return 'snowflake';
			case 'HEAT':
				return 'flame';
			case 'DRY':
				return 'droplets';
			case 'FAN':
				return 'fan';
			case 'AUTO':
				return 'sparkles';
			default:
				return 'circle';
		}
	};

	const getFanModeIcon = (mode) => {
		switch (mode) {
			case 'NORMAL':
				return 'wind';
			case 'SLEEP':
				return 'moon';
			case 'NATURAL':
				return 'leaf';
			default:
				return 'circle';
		}
	};

	const updateCharts = async (from, to) => {
		const results = await Promise.all([
			getAverageHistory(roomId, from, to),
			getEnergyMetricHistory({
				category: 'ROOM',
				targetId: roomId,
				from,
				to,
			}),
		]);

		const [errTemp, tempRes] = results[0];
		const [errPower, powerRes] = results[1];

		if (!errTemp && tempRes.data) {
			const data = tempRes.data;
			const avg = data.length > 0 ? (data.reduce((sum, item) => sum + item.avgTempC, 0) / data.length).toFixed(2) : '--';
			document.querySelector('#avgTempBadge').textContent = `Avg: ${avg}°C`;

			charts.temp.updateSeries([
				{
					name: i18n.temp,
					data: data.map((item) => ({
						x: new Date(item.timestamp).getTime(),
						y: parseFloat(item.avgTempC.toFixed(2)),
					})),
				},
			]);
		}

		if (!errPower && powerRes.data) {
			const data = powerRes.data;
			const peak = data.length > 0 ? Math.max(...data.map((item) => item.power || 0)).toFixed(2) : '--';
			document.querySelector('#peakPowerBadge').textContent = `Peak: ${peak}W`;

			charts.power.updateSeries([
				{
					name: i18n.power,
					data: data.map((item) => ({
						x: new Date(item.timestamp).getTime(),
						y: parseFloat((item.power || 0).toFixed(2)),
					})),
				},
			]);
		}
	};

	const initCharts = () => {
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
			tooltip: {
				x: { format: 'dd MMM HH:mm' },
				y: {
					formatter: (val) => (val !== undefined ? val.toFixed(2) : val),
				},
			},
			yaxis: {
				labels: {
					formatter: (val) => (val !== undefined ? val.toFixed(2) : val),
					style: { colors: '#94a3b8', fontSize: '11px' },
				},
			},
			fill: {
				type: 'gradient',
				gradient: {
					shadeIntensity: 1,
					opacityFrom: 0.4,
					opacityTo: 0.05,
					stops: [0, 90, 100],
				},
			},
		};

		const tempOptions = {
			...commonOptions,
			series: [{ name: i18n.temp, data: [] }],
			colors: ['#ef4444'],
		};

		const powerOptions = {
			...commonOptions,
			series: [{ name: i18n.power, data: [] }],
			colors: ['#f59e0b'],
		};

		const tempEl = document.querySelector('#tempChart');
		const powerEl = document.querySelector('#powerChart');

		if (tempEl && window.ApexCharts) {
			charts.temp = new ApexCharts(tempEl, tempOptions);
			charts.temp.render();
		}
		if (powerEl && window.ApexCharts) {
			charts.power = new ApexCharts(powerEl, powerOptions);
			charts.power.render();
		}
	};

	const renderAc = (ac) => {
		const isActive = ac.power === 'ON';
		return `
			<div class="mb-4 ac-item" data-id="${ac.id}" data-natural-id="${ac.naturalId}">
				<div class="d-flex justify-content-between align-items-center">
					<div class="d-flex align-items-center flex-grow-1 overflow-hidden">
						<div class="d-flex align-items-center justify-content-center rounded-3 me-3 flex-shrink-0 ${isActive ? 'bg-primary bg-opacity-10 text-primary' : 'bg-light text-muted'}" style="width: 48px; height: 48px">
							<i data-lucide="wind"></i>
						</div>
						<div class="overflow-hidden">
							<h6 class="mb-0 fw-bold text-dark small text-truncate">${ac.name}</h6>
							<small class="text-muted tiny text-truncate text-nowrap d-block">${ac.description || i18n.ac}</small>
						</div>
					</div>
					<div class="d-flex align-items-center gap-3 ms-3">
						<div class="position-relative">
							<input type="checkbox" class="btn-check ac-master-switch" id="acSwitch${ac.id}" ${isActive ? 'checked' : ''} autocomplete="off">
							<label class="btn ${isActive ? 'btn-success border-success' : 'btn-outline-secondary'} rounded-pill px-3 py-1 d-flex align-items-center gap-2 shadow-sm" for="acSwitch${ac.id}" style="font-size: 0.75rem; border-width: 2px">
								<i data-lucide="power" style="width: 14px; height: 14px"></i>
								<span class="fw-bold">${isActive ? i18n.on : i18n.off}</span>
							</label>
						</div>
						<button class="btn btn-link text-muted p-0 border-0" data-bs-toggle="collapse" data-bs-target="#acControls${ac.id}">
							<i data-lucide="chevron-down" style="width: 18px"></i>
						</button>
					</div>
				</div>
				<div class="collapse mt-3" id="acControls${ac.id}">
					<div class="bg-light rounded-4 p-4 border border-white position-relative ${!isActive ? 'opacity-50' : ''}">
						<div class="text-center mb-4 pt-1">
							<div class="d-flex justify-content-center align-items-center">
								<button class="btn btn-outline-secondary rounded-circle d-flex align-items-center justify-content-center shadow-none ac-temp-minus" style="width: 50px; height: 50px" ${!isActive ? 'disabled' : ''}>
									<i data-lucide="minus"></i>
								</button>
								<div class="mx-4">
									<span class="fw-bold h2 m-0 ac-temp-value">${ac.temperature || 25}</span>
									<span class="ms-1 text-muted">°C</span>
								</div>
								<button class="btn btn-outline-secondary rounded-circle d-flex align-items-center justify-content-center shadow-none ac-temp-plus" style="width: 50px; height: 50px" ${!isActive ? 'disabled' : ''}>
									<i data-lucide="plus"></i>
								</button>
							</div>
						</div>
						<div class="mb-4 pt-4 border-top border-white">
							<small class="text-muted d-block mb-3 fw-bold text-uppercase tiny text-center">${i18n.modes}</small>
							<div class="d-flex justify-content-center flex-wrap gap-3">
								${['COOL', 'HEAT', 'DRY', 'FAN', 'AUTO']
									.map(
										(m) => `
									<div class="d-flex flex-column align-items-center">
										<button class="btn btn-outline-secondary rounded-circle d-flex align-items-center justify-content-center p-0 ac-mode-btn ${ac.mode === m ? 'bg-primary bg-opacity-10 border-primary text-primary' : ''}" data-mode="${m}" style="width: 56px; height: 56px" ${!isActive ? 'disabled' : ''}>
											<i data-lucide="${getAcModeIcon(m)}" style="width: 24px"></i>
										</button>
									</div>
								`,
									)
									.join('')}
							</div>
						</div>
						<div class="bg-white rounded-4 p-3 border border-light">
							<div class="mb-3">
								<div class="d-flex justify-content-between align-items-center mb-2">
									<small class="fw-bold text-uppercase text-muted tiny">${i18n.fanSpeed}</small>
									<span class="badge bg-primary rounded-pill tiny">${i18n.level} ${ac.fanSpeed || 0}</span>
								</div>
								<input type="range" class="form-range w-100 ac-fanspeed-range" min="0" max="5" value="${ac.fanSpeed || 0}" ${!isActive ? 'disabled' : ''}>
							</div>
							<div class="d-flex justify-content-between align-items-center pt-2 border-top">
								<label class="fw-bold text-uppercase text-muted tiny">${i18n.swing}</label>
								<div class="form-check form-switch">
									<input class="form-check-input ac-swing-switch" type="checkbox" ${ac.swing === 'ON' ? 'checked' : ''} ${!isActive ? 'disabled' : ''}>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		`;
	};

	const renderFan = (fan) => {
		const isActive = fan.power === 'ON';
		const isIR = fan.type === 'IR';
		return `
			<div class="mb-4 fan-item" data-id="${fan.id}" data-natural-id="${fan.naturalId}">
				<div class="d-flex justify-content-between align-items-center">
					<div class="d-flex align-items-center flex-grow-1 overflow-hidden">
						<div class="d-flex align-items-center justify-content-center rounded-3 me-3 flex-shrink-0 ${isActive ? 'bg-warning bg-opacity-10 text-warning' : 'bg-light text-muted'}" style="width: 48px; height: 48px">
							<i data-lucide="fan"></i>
						</div>
						<div class="overflow-hidden">
							<h6 class="mb-0 fw-bold text-dark small text-truncate">${fan.name}</h6>
							<small class="text-muted tiny text-truncate text-nowrap d-block">${fan.description || i18n.fan}</small>
						</div>
					</div>
					<div class="d-flex align-items-center gap-3 ms-3">
						<div class="position-relative">
							<input type="checkbox" class="btn-check fan-master-switch" id="fanSwitch${fan.id}" ${isActive ? 'checked' : ''} autocomplete="off">
							<label class="btn ${isActive ? 'btn-success border-success' : 'btn-outline-secondary'} rounded-pill px-3 py-1 d-flex align-items-center gap-2 shadow-sm" for="fanSwitch${fan.id}" style="font-size: 0.75rem; border-width: 2px">
								<i data-lucide="power" style="width: 14px; height: 14px"></i>
								<span class="fw-bold">${isActive ? i18n.on : i18n.off}</span>
							</label>
						</div>
						<button class="btn btn-link text-muted p-0 border-0" data-bs-toggle="collapse" data-bs-target="#fanControls${fan.id}">
							<i data-lucide="chevron-down" style="width: 18px"></i>
						</button>
					</div>
				</div>
				<div class="collapse mt-3" id="fanControls${fan.id}">
					<div class="bg-light rounded-4 p-4 border border-white position-relative ${!isActive ? 'opacity-50' : ''}">
						${
							isIR
								? `
							<div class="mb-4 pt-1">
								<small class="text-muted d-block mb-3 fw-bold text-uppercase tiny text-center">${i18n.modes}</small>
								<div class="d-flex justify-content-center gap-3">
									${['NORMAL', 'SLEEP', 'NATURAL']
										.map(
											(m) => `
										<div class="d-flex flex-column align-items-center">
											<button class="btn btn-outline-secondary rounded-circle d-flex align-items-center justify-content-center p-0 fan-mode-btn ${fan.mode === m ? 'bg-primary bg-opacity-10 border-primary text-primary' : ''}" data-mode="${m}" style="width: 56px; height: 56px" ${!isActive ? 'disabled' : ''}>
												<i data-lucide="${getFanModeIcon(m)}" style="width: 24px"></i>
											</button>
										</div>
									`,
										)
										.join('')}
								</div>
							</div>
						`
								: ''
						}
						<div class="bg-white rounded-4 p-3 border border-light">
							<div class="mb-3">
								<div class="d-flex justify-content-between align-items-center mb-2">
									<small class="fw-bold text-uppercase text-muted tiny">${i18n.fanSpeed}</small>
									<span class="badge bg-warning text-dark rounded-pill tiny">${i18n.level} ${fan.speed || 0}</span>
								</div>
								<input type="range" class="form-range w-100 fan-speed-range" min="0" max="3" value="${fan.speed || 0}" ${!isActive ? 'disabled' : ''}>
							</div>
							${
								isIR
									? `
								<div class="d-flex justify-content-between align-items-center mb-3">
									<label class="fw-bold text-uppercase text-muted tiny">${i18n.swing}</label>
									<div class="form-check form-switch">
										<input class="form-check-input fan-swing-switch" type="checkbox" ${fan.swing === 'ON' ? 'checked' : ''} ${!isActive ? 'disabled' : ''}>
									</div>
								</div>
								<div class="d-flex justify-content-between align-items-center">
									<label class="fw-bold text-uppercase text-muted tiny">${i18n.light}</label>
									<div class="form-check form-switch">
										<input class="form-check-input fan-light-switch" type="checkbox" ${fan.light === 'ON' ? 'checked' : ''} ${!isActive ? 'disabled' : ''}>
									</div>
								</div>
							`
									: ''
							}
						</div>
					</div>
				</div>
			</div>
		`;
	};

	const renderLight = (light) => {
		const isActive = light.power === 'ON';
		return `
			<div class="mb-4 light-item" data-id="${light.id}" data-natural-id="${light.naturalId}">
				<div class="d-flex justify-content-between align-items-center">
					<div class="d-flex align-items-center flex-grow-1 overflow-hidden">
						<div class="d-flex align-items-center justify-content-center rounded-3 me-3 flex-shrink-0 ${isActive ? 'bg-success bg-opacity-10 text-success' : 'bg-light text-muted'}" style="width: 48px; height: 48px">
							<i data-lucide="lightbulb"></i>
						</div>
						<div class="overflow-hidden">
							<h6 class="mb-0 fw-bold text-dark small text-truncate">${light.name}</h6>
							<small class="text-muted tiny text-truncate text-nowrap d-block">${light.description || i18n.light}</small>
						</div>
					</div>
					<div class="d-flex align-items-center gap-3 ms-3">
						<div class="position-relative">
							<input type="checkbox" class="btn-check light-master-switch" id="lightSwitch${light.id}" ${isActive ? 'checked' : ''} autocomplete="off">
							<label class="btn ${isActive ? 'btn-success border-success' : 'btn-outline-secondary'} rounded-pill px-3 py-1 d-flex align-items-center gap-2 shadow-sm" for="lightSwitch${light.id}" style="font-size: 0.75rem; border-width: 2px">
								<i data-lucide="power" style="width: 14px; height: 14px"></i>
								<span class="fw-bold">${isActive ? i18n.on : i18n.off}</span>
							</label>
						</div>
						<button class="btn btn-link text-muted p-0 border-0" data-bs-toggle="collapse" data-bs-target="#lightControls${light.id}">
							<i data-lucide="chevron-down" style="width: 18px"></i>
						</button>
					</div>
				</div>
				<div class="collapse mt-3" id="lightControls${light.id}">
					<div class="bg-light rounded-4 p-4 border border-white position-relative ${!isActive ? 'opacity-50' : ''}">
						<div class="bg-white rounded-4 p-3 border border-light">
							<div class="d-flex justify-content-between align-items-center mb-2">
								<small class="fw-bold text-uppercase text-muted tiny">${i18n.brightness}</small>
								<span class="badge bg-success rounded-pill tiny">${i18n.level} ${light.level || 0}%</span>
							</div>
							<input type="range" class="form-range w-100 light-level-range" min="0" max="100" value="${light.level || 0}" ${!isActive ? 'disabled' : ''}>
							<div class="d-flex justify-content-between mt-1">
								<small class="text-muted tiny">${i18n.dark}</small>
								<small class="text-muted tiny">${i18n.bright}</small>
							</div>
						</div>
					</div>
				</div>
			</div>
		`;
	};

	const loadDevices = async () => {
		const [err, res] = await getDevicesByRoom(roomId);
		if (err || !res.data) {
			if (window.Swal) Swal.fire(i18n.errorTitle, i18n.errorFetch, 'error');
			return;
		}

		const devices = res.data;
		const acs = devices.filter((d) => d.category === 'AIR_CONDITION');
		const fans = devices.filter((d) => d.category === 'FAN');
		const lights = devices.filter((d) => d.category === 'LIGHT');

		const container = document.querySelector('#controls-container');
		if (!container) return;

		container.innerHTML = `
			<div class="d-flex flex-wrap justify-content-between align-items-center mb-4 mt-5">
				<h5 class="m-0 fw-bold text-dark d-flex align-items-center">
					<i data-lucide="settings-2" class="text-primary me-2"></i>
					<span>${i18n.controls}</span>
				</h5>
			</div>
			<div class="card border-0 rounded-4 overflow-hidden shadow-sm">
				<div class="card-header bg-white border-0 py-3">
					<ul class="nav nav-pills gap-2 p-1 bg-light rounded-pill" id="deviceTabs" role="tablist" style="width: fit-content">
						<li class="nav-item"><button class="nav-link active rounded-pill px-4" data-bs-toggle="tab" data-bs-target="#ac-pane"><i data-lucide="wind" class="me-2"></i>${i18n.ac}</button></li>
						<li class="nav-item"><button class="nav-link rounded-pill px-4" data-bs-toggle="tab" data-bs-target="#fan-pane"><i data-lucide="fan" class="me-2"></i>${i18n.fan}</button></li>
						<li class="nav-item"><button class="nav-link rounded-pill px-4" data-bs-toggle="tab" data-bs-target="#light-pane"><i data-lucide="lightbulb" class="me-2"></i>${i18n.light}</button></li>
					</ul>
				</div>
				<div class="card-body p-0 border-top">
					<div class="tab-content">
						<div class="tab-pane fade show active p-4" id="ac-pane">${acs.map(renderAc).join('') || `<p class="text-muted text-center py-4">${i18n.noAc}</p>`}</div>
						<div class="tab-pane fade p-4" id="fan-pane">${fans.map(renderFan).join('') || `<p class="text-muted text-center py-4">${i18n.noFan}</p>`}</div>
						<div class="tab-pane fade p-4" id="light-pane">${lights.map(renderLight).join('') || `<p class="text-muted text-center py-4">${i18n.noLight}</p>`}</div>
					</div>
				</div>
			</div>
		`;
		if (window.lucide) lucide.createIcons();
	};

	const handleControl = async (type, naturalId, payload) => {
		let err, res;
		switch (type) {
			case 'AC':
				[err, res] = await controlAc(naturalId, payload);
				break;
			case 'FAN':
				[err, res] = await controlFan(naturalId, payload);
				break;
			case 'LIGHT':
				[err, res] = await controlLight(naturalId, payload);
				break;
		}

		if (err) {
			if (window.Swal) {
				Swal.fire({
					title: i18n.errorTitle,
					text: i18n.errorControl,
					icon: 'error',
					toast: true,
					position: 'top-end',
					showConfirmButton: false,
					timer: 3000,
				});
			}
			return false;
		}

		if (window.Swal) {
			Swal.fire({
				text: i18n.successControl,
				icon: 'success',
				toast: true,
				position: 'top-end',
				showConfirmButton: false,
				timer: 2000,
			});
		}
		return true;
	};

	const bindEvents = () => {
		// Flatpickr
		if (window.flatpickr) {
			flatpickr('#analyticsRange', {
				mode: 'range',
				enableTime: true,
				time_24hr: true,
				altInput: true,
				altFormat: 'd/m/Y H:i',
				dateFormat: 'Z',
				defaultDate: [new Date(Date.now() - 3 * 60 * 60 * 1000), new Date()],
				onClose: (selectedDates) => {
					if (selectedDates.length === 2) {
						updateCharts(selectedDates[0].toISOString(), selectedDates[1].toISOString());
					}
				},
			});
		}

		// Device Controls Delegation
		const container = document.querySelector('#controls-container');
		if (container) {
			container.addEventListener('change', async (e) => {
				const target = e.target;
				const item = target.closest('.ac-item, .fan-item, .light-item');
				if (!item) return;

				const naturalId = item.dataset.naturalId;
				let type = '';
				let payload = {};

				if (item.classList.contains('ac-item')) {
					type = 'AC';
					if (target.classList.contains('ac-master-switch')) payload.power = target.checked ? 'ON' : 'OFF';
					else if (target.classList.contains('ac-fanspeed-range')) payload.fanSpeed = parseInt(target.value);
					else if (target.classList.contains('ac-swing-switch')) payload.swing = target.checked ? 'ON' : 'OFF';
				} else if (item.classList.contains('fan-item')) {
					type = 'FAN';
					if (target.classList.contains('fan-master-switch')) payload.power = target.checked ? 'ON' : 'OFF';
					else if (target.classList.contains('fan-speed-range')) payload.speed = parseInt(target.value);
					else if (target.classList.contains('fan-swing-switch')) payload.swing = target.checked ? 'ON' : 'OFF';
					else if (target.classList.contains('fan-light-switch')) payload.light = target.checked ? 'ON' : 'OFF';
				} else if (item.classList.contains('light-item')) {
					type = 'LIGHT';
					if (target.classList.contains('light-master-switch')) payload.power = target.checked ? 'ON' : 'OFF';
					else if (target.classList.contains('light-level-range')) payload.level = parseInt(target.value);
				}

				if (Object.keys(payload).length > 0) {
					const success = await handleControl(type, naturalId, payload);
					if (success) {
						if (payload.power) loadDevices();
					}
				}
			});

			// Temperature +/- buttons
			container.addEventListener('click', async (e) => {
				const btn = e.target.closest('.ac-temp-minus, .ac-temp-plus, .ac-mode-btn, .fan-mode-btn');
				if (!btn) return;

				const item = btn.closest('.ac-item, .fan-item');
				const naturalId = item.dataset.naturalId;

				if (btn.classList.contains('ac-temp-minus') || btn.classList.contains('ac-temp-plus')) {
					const valEl = item.querySelector('.ac-temp-value');
					let val = parseInt(valEl.textContent);
					val = btn.classList.contains('ac-temp-plus') ? val + 1 : val - 1;
					if (val >= 16 && val <= 32) {
						const success = await handleControl('AC', naturalId, { temperature: val });
						if (success) valEl.textContent = val;
					}
				} else if (btn.classList.contains('ac-mode-btn')) {
					const mode = btn.dataset.mode;
					const success = await handleControl('AC', naturalId, { mode });
					if (success) loadDevices();
				} else if (btn.classList.contains('fan-mode-btn')) {
					const mode = btn.dataset.mode;
					const success = await handleControl('FAN', naturalId, { mode });
					if (success) loadDevices();
				}
			});
		}
	};

	// Initialize
	initCharts();
	const now = new Date();
	const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);
	updateCharts(yesterday.toISOString(), now.toISOString());
	loadDevices();
	bindEvents();
});
