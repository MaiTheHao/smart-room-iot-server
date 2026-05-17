import { TabulatorFull as Tabulator } from '../../../../lib/tabulator_esm.min.js';
import { getRules, toggleRuleStatus, executeRuleNow } from '../../../../api/rule.api.js';

export const Datatable = (() => {
	let table = null;
	let isLoadingData = false;
	const { i18n } = window.__RULE_CONFIG__;

	const init = (onEditRow, onSelectionChange) => {
		table = new Tabulator('#rulesTable', {
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
					width: 250,
					formatter: (cell) => {
						const data = cell.getData();
						return `
							<div class="d-flex align-items-center justify-content-center h-100 gap-1">
								<a href="/management/smart-system/rules/${data.id}/conditions" class="btn btn-light btn-sm rounded-pill" title="${i18n.manageConditions}">
									<i data-lucide="filter" class="lucide-sm text-warning"></i>
								</a>
                                <a href="/management/smart-system/rules/${data.id}/actions" class="btn btn-light btn-sm rounded-pill" title="${i18n.manageActions}">
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
            const [err] = await toggleRuleStatus(id, isActive);
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
            const [err] = await executeRuleNow(id);
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

			const [err, res] = await getRules(0, 1000);
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
