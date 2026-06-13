document.addEventListener('DOMContentLoaded', () => {
  const config = window.__DASHBOARD_CONFIG__;
  if (!config) return;

  const { data, i18n } = config;
  const { floors, floorRoomMap, roomInfoMap, totalFloors, totalRooms, totalHardwares } = data;

  const renderSummary = () => {
    const container = document.querySelector('#summary-container');
    if (!container) return;

    const stats = [
      { label: i18n.summaryFloors, value: totalFloors, icon: 'layers', color: 'primary' },
      { label: i18n.summaryRooms, value: totalRooms, icon: 'home', color: 'success' },
      { label: i18n.summaryHardwares, value: totalHardwares, icon: 'cpu', color: 'info' },
    ];

    container.innerHTML = stats
      .map(
        (s) => `
				<div class="col-12 col-md-4 mb-3 mb-md-0">
					<div class="card border-1 shadow-sm rounded-2 h-100 transition-all">
						<div class="card-body p-4 d-flex align-items-center">
							<div class="rounded-2 bg-${s.color} bg-opacity-10 p-3 me-4 text-${s.color} d-flex align-items-center justify-content-center" style="width: 64px; height: 64px">
								<i data-lucide="${s.icon}" style="width: 32px; height: 32px"></i>
							</div>
							<div>
								<p class="text-muted mb-1 fw-medium">${s.label}</p>
								<h2 class="mb-0 fw-bold">${s.value || 0}</h2>
							</div>
						</div>
					</div>
				</div>
			`,
      )
      .join('');
  };

  const renderScenes = () => {
    const container = document.querySelector('#scenes-container');
    if (!container) return;

    const scenes = [
      { id: 'in_home', name: i18n.scenesInHome, icon: 'home', color: 'success' },
      { id: 'out_home', name: i18n.scenesOutHome, icon: 'log-out', color: 'danger' },
    ];

    container.innerHTML = scenes
      .map(
        (s) => `
				<button class="btn btn-outline-${s.color} border-2 p-3 d-flex flex-column align-items-center rounded-2 shadow-sm scene-btn" data-scene-id="${s.id}" style="min-width: 140px">
					<i data-lucide="${s.icon}" class="mb-2" style="width: 24px; height: 24px"></i>
					<span class="fw-bold small">${s.name}</span>
				</button>
			`,
      )
      .join('');
  };

  const renderRoomCard = (room, info) => {
    const isOnline = info && info.deviceCount > 0;
    const imageUrl = `/imgs/fallback_room_background600x400.png`;

    return `
			<div class="card h-100 room-card shadow-sm transition-all" style="cursor: pointer;" data-room-id="${room.id}">
				<div class="room-image-container">
					<img src="${imageUrl}" class="room-image" alt="${room.name}" />
					<div class="status-badge" style="color: ${isOnline ? '#198754' : '#dc3545'}">
						<span class="dot-ping" style="background-color: ${isOnline ? '#198754' : '#dc3545'}"></span>
						<span>${isOnline ? 'Online' : 'Offline'}</span>
					</div>
				</div>

				<div class="card-body">
					<h5 class="card-title fw-bold">${room.name}</h5>
					<br />
					<p class="text-muted small mb-0 text-truncate">
						${room.description || ''}
					</p>
				</div>

				<div class="card-footer px-2 py-3 bg-white border-top">
					<div class="row g-0 text-center">
						<div class="col-4 border-end">
							<div class="d-flex flex-column align-items-center">
								<div class="stat-icon-wrapper bg-soft-info">
									<i data-lucide="cpu" style="width: 20px; height: 20px"></i>
								</div>
								<span class="stat-value">${info ? info.deviceCount : 0}</span>
								<span class="stat-label">${i18n.roomDevices}</span>
							</div>
						</div>
						<div class="col-4 border-end">
							<div class="d-flex flex-column align-items-center">
								<div class="stat-icon-wrapper bg-soft-warning">
									<i data-lucide="thermometer" style="width: 20px; height: 20px"></i>
								</div>
								<span class="stat-value">${info && info.latestAvgTemperature ? info.latestAvgTemperature.toFixed(2) + '°C' : '--'}</span>
								<span class="stat-label">${i18n.roomTemp}</span>
							</div>
						</div>
						<div class="col-4">
							<div class="d-flex flex-column align-items-center">
								<div class="stat-icon-wrapper bg-soft-success">
									<i data-lucide="zap" style="width: 20px; height: 20px"></i>
								</div>
								<span class="stat-value">${info && info.latestSumWatt ? info.latestSumWatt.toFixed(2) + 'W' : '--'}</span>
								<span class="stat-label">${i18n.roomPower}</span>
							</div>
						</div>
					</div>
				</div>
			</div>
		`;
  };

  const renderFloorsAndRooms = () => {
    const container = document.querySelector('#floors-container');
    if (!container) return;

    let html = '';
    floors.forEach((floor) => {
      const rooms = floorRoomMap[floor.id] || [];

      html += `
				<div class="floor-divider d-flex align-items-center justify-content-between mt-5">
					<h4 class="m-0 fw-bold text-dark">
						<i data-lucide="layout" class="text-muted me-2" style="width: 20px"></i>
						<span>${floor.name}</span>
					</h4>
					<span class="badge bg-secondary rounded-pill px-3">${i18n.floorLevel} ${floor.level}</span>
				</div>

				<div class="room-grid mb-4">
					${rooms.length === 0 ? `<div class="w-100"><div class="alert alert-light text-center border shadow-sm rounded-4">${i18n.roomNoRooms}</div></div>` : rooms.map((r) => renderRoomCard(r, roomInfoMap[r.id])).join('')}
				</div>
			`;
    });
    container.innerHTML = html;
  };

  const triggerScene = (id, name) => {
    if (typeof Swal === 'undefined') return;

    Swal.fire({
      title: i18n.sceneTriggerTitle.replace('{0}', name),
      text: i18n.sceneTriggerText.replace('{0}', name),
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#0d6efd',
      cancelButtonColor: '#6c757d',
      confirmButtonText: i18n.sceneTriggerConfirm,
      cancelButtonText: i18n.sceneTriggerCancel,
      showLoaderOnConfirm: true,
      preConfirm: () => {
        return new Promise((resolve) => {
          setTimeout(() => {
            resolve(true);
          }, 1000);
        });
      },
      allowOutsideClick: () => !Swal.isLoading(),
    }).then((result) => {
      if (result.isConfirmed) {
        Swal.fire({
          title: i18n.sceneTriggerSuccessTitle,
          text: i18n.sceneTriggerSuccessText.replace('{0}', name),
          icon: 'success',
          timer: 2000,
          showConfirmButton: false,
        });
      }
    });
  };

  const bindEvents = () => {
    document.addEventListener('click', (e) => {
      const sceneBtn = e.target.closest('.scene-btn');
      if (sceneBtn) {
        const sceneId = sceneBtn.dataset.sceneId;
        const sceneName = sceneBtn.querySelector('span').textContent;
        triggerScene(sceneId, sceneName);
        return;
      }

      const roomCard = e.target.closest('.room-card');
      if (roomCard) {
        const roomId = roomCard.dataset.roomId;
        if (roomId) {
          window.location.href = `/rooms/${roomId}`;
        }
        return;
      }
    });
  };

  renderSummary();
  renderScenes();
  renderFloorsAndRooms();

  if (window.renderIcons) {
    window.renderIcons();
  }
  bindEvents();
});
