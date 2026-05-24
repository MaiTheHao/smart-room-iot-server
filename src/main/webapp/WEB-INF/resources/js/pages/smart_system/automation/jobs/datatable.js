import { TabulatorFull as Tabulator } from '../../../../lib/tabulator_esm.min.js';
import { getAutomations, toggleAutomationStatus, executeAutomationNow } from '../../../../api/automation.api.js';
import { CronUtils } from '../../../../common/cron_util.js';

export const Datatable = (() => {
	let table = null;
	let isLoadingData = false;
	const { i18n } = window.__AUTOMATION_CONFIG__;

	const init = (onEditRow, onSelectionChange) => {
		table = new Tabulator('#automationsTable', {
			height: 'auto',
			layout: 'fitColumns',
			responsiveLayout: 'collapse',
			selectableRows: true,
			rowHeader: {
				formatter: 'rowSelection',
				titleFormatter: 'rowSelection',
				headerSort: false,
				resizable: false,
				frozen: true,
				headerHozAlign: 'center',
				hozAlign: 'center',
				width: 40,
			},
			placeholder: `
				<div class="text-center py-5 text-muted">
					<i data-lucide="inbox" class="mb-2" style="width: 48px; height: 48px"></i>
					<p>${i18n.noData}</p>
				</div>`,
			pagination: true,
			paginationMode: 'local',
			paginationSize: 10,
			paginationSizeSelector: [10, 25, 50, 100],
			paginationCounter: 'rows',
			columns: [
				{
					title: i18n.colId,
					field: 'id',
					width: 80,
					hozAlign: 'center',
					formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100"><span class="fw-medium text-muted">#${cell.getValue()}</span></div>`,
				},
				{
					title: i18n.colName,
					field: 'name',
					headerFilter: 'input',
					formatter: (cell) => {
                        const data = cell.getData();
                        return `<div class="d-flex flex-column justify-content-center h-100 py-1">
                                    <div class="fw-bold text-dark">${cell.getValue()}</div>
                                    <div class="small text-muted">${data.description || ''}</div>
                                </div>`;
                    },
				},
				{
					title: i18n.colCron,
					field: 'cronExpression',
					width: 220,
					formatter: (cell) => {
                        const cron = cell.getValue();
                        const parsed = CronUtils.fromCron(cron);
                        const time = `${parsed.hour.toString().padStart(2, '0')}:` +
                                     `${parsed.minute.toString().padStart(2, '0')}:` +
                                     `${parsed.second.toString().padStart(2, '0')}`;
                        if (parsed.type === 'DAILY') {
                            return `<div class="d-flex align-items-center h-100">
                                <span class="badge bg-light text-dark border d-inline-flex align-items-center px-2 py-1">
                                    <i data-lucide="rotate-cw" class="text-muted me-2" style="width:14px;height:14px"></i>
                                    <i data-lucide="clock" class="text-muted me-1" style="width:14px;height:14px"></i>
                                    <span class="fw-medium">${time}</span>
                                </span>
                            </div>`;
                        } else if (parsed.type === 'WEEKLY') {
                            return `<div class="d-flex align-items-center h-100">
                                <span class="badge bg-light text-dark border d-inline-flex align-items-center px-2 py-1">
                                    <i data-lucide="calendar-days" class="text-muted me-2" style="width:14px;height:14px"></i>
                                    <span class="me-2 fw-bold text-primary">${parsed.daysOfWeek.join(',')}</span>
                                    <i data-lucide="clock" class="text-muted me-1" style="width:14px;height:14px"></i>
                                    <span class="fw-medium">${time}</span>
                                </span>
                            </div>`;
                        } else if (parsed.type === 'MONTHLY') {
                            return `<div class="d-flex align-items-center h-100">
                                <span class="badge bg-light text-dark border d-inline-flex align-items-center px-2 py-1">
                                    <i data-lucide="calendar" class="text-muted me-2" style="width:14px;height:14px"></i>
                                    <span class="me-2 fw-bold text-info">#${parsed.dayOfMonth}</span>
                                    <i data-lucide="clock" class="text-muted me-1" style="width:14px;height:14px"></i>
                                    <span class="fw-medium">${time}</span>
                                </span>
                            </div>`;
                        }
                        return `<div class="d-flex align-items-center h-100"><span class="badge bg-light text-dark border badge-code">${cron}</span></div>`;
                    },
				},
                {
					title: i18n.colStatus,
					field: 'isActive',
					width: 120,
					hozAlign: 'center',
					formatter: (cell) => {
						const isActive = cell.getValue();
						return `<div class="d-flex align-items-center justify-content-center h-100">
                                    <div class="form-check form-switch m-0">
                                        <input class="form-check-input btn-toggle-status" type="checkbox" data-id="${cell.getData().id}" ${isActive ? 'checked' : ''} title="Toggle Active">
                                    </div>
                                </div>`;
					},
				},
				{
					title: i18n.colActions,
					hozAlign: 'center',
					headerSort: false,
					width: 200,
					formatter: (cell) => {
						const data = cell.getData();
						return `
							<div class="d-flex align-items-center justify-content-center h-100 gap-1">
								<a href="/management/smart-system/automations/${data.id}/actions" class="btn btn-light btn-sm rounded-pill" title="${i18n.manageActions}">
									<i data-lucide="settings-2" class="lucide-sm text-info"></i>
								</a>
                                <button class="btn btn-light btn-sm rounded-pill btn-execute" data-id="${data.id}" title="Run Now">
									<i data-lucide="play" class="lucide-sm text-success"></i>
								</button>
								<button class="btn btn-light btn-sm rounded-pill btn-edit" data-id="${data.id}" title="${i18n.editTitle}">
									<i data-lucide="edit-3" class="lucide-sm text-primary"></i>
								</button>
							</div>`;
					},
				},
			],
		});

		table.on('tableBuilt', refresh);
		table.on('rowSelectionChanged', (data) => onSelectionChange?.(data.length));
		table.on('renderComplete', () => window.renderIcons?.());

		document.addEventListener('click', (e) => {
			const btnEdit = e.target.closest('.btn-edit');
            const btnExecute = e.target.closest('.btn-execute');

			if (btnEdit) {
				const row = table.getRow(btnEdit.dataset.id);
				if (row) onEditRow?.(row.getData());
			} else if (btnExecute) {
                const id = btnExecute.dataset.id;
                handleExecute(id, btnExecute);
            }
		});

        document.addEventListener('change', (e) => {
            const toggle = e.target.closest('.btn-toggle-status');
            if (toggle) {
                handleToggleStatus(toggle.dataset.id, toggle.checked, toggle);
            }
        });
	};

    const handleToggleStatus = async (id, isActive, el) => {
        el.disabled = true;
        try {
            const [err] = await toggleAutomationStatus(id, isActive);
            if(err) throw err;
            const row = table.getRow(id);
            if(row) row.update({ isActive });
        } catch(e) {
            el.checked = !isActive;
            Swal.fire(i18n.error, e.message || i18n.error, 'error');
        } finally {
            el.disabled = false;
        }
    };

    const handleExecute = async (id, btn) => {
        const icon = btn.querySelector('svg, i');
        if (!icon) return;

        const tempI = document.createElement('i');
        tempI.setAttribute('data-lucide', 'loader-2');
        tempI.className = 'lucide-sm lucide-spin';
        icon.replaceWith(tempI);
        window.renderIcons?.();

        try {
            const [err] = await executeAutomationNow(id);
            if(err) throw err;
            Swal.fire({
                title: 'Executed',
                icon: 'success',
                timer: 1500,
                showConfirmButton: false
            });
        } catch(e) {
            Swal.fire(i18n.error, e.message || i18n.error, 'error');
        } finally {
            const newIcon = btn.querySelector('svg, i');
            if (newIcon) {
                const resetI = document.createElement('i');
                resetI.setAttribute('data-lucide', 'play');
                resetI.className = 'lucide-sm text-success';
                newIcon.replaceWith(resetI);
                window.renderIcons?.();
            }
        }
    };

	const refresh = async () => {
		if (isLoadingData) return;
		const btnReload = document.getElementById('btnReload');
		const icon = btnReload?.querySelector('[data-lucide="refresh-cw"]');

		try {
			isLoadingData = true;
			if (btnReload) btnReload.disabled = true;
			if (icon) icon.classList.add('lucide-spin');

			const [err, res] = await getAutomations(0, 1000);
			if (err) throw err;

			const dataList = res.data.content || [];
			table.setData(dataList);
		} catch (err) {
			console.error('Refresh error:', err);
		} finally {
			isLoadingData = false;
			if (btnReload) btnReload.disabled = false;
			if (icon) icon.classList.remove('lucide-spin');
			window.renderIcons?.();
		}
	};

	return {
		init,
		refresh,
		getSelectedData: () => table?.getSelectedData() || [],
	};
})();
