import { Datatable } from './datatable.js';
import { RuleModal } from './rule_modal.js';
import { deleteRule } from '../../../../api/rule.api.js';

document.addEventListener('DOMContentLoaded', () => {
	const tableContainer = document.querySelector('#rulesTable');
	if (!tableContainer) return;

	const { i18n } = window.__RULE_CONFIG__;

	const Controller = {
		init() {
            RuleModal.init();
			Datatable.init(
				(data) => RuleModal.open(data),
				(count) => {
					const btnDelete = document.getElementById('btnDeleteSelected');
					if (btnDelete) btnDelete.disabled = count === 0;
				},
			);
			this.bindEvents();
		},

		bindEvents() {
			document.getElementById('btnReload')?.addEventListener('click', () => Datatable.refresh());
			document.getElementById('btnAdd')?.addEventListener('click', () => RuleModal.open());
			document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());
			document.getElementById('ruleForm')?.addEventListener('submit', (e) => RuleModal.submit(e, () => Datatable.refresh()));
		},

		async handleBatchDelete() {
			const selected = Datatable.getSelectedData();
			if (selected.length === 0) return;

			const result = await Swal.fire({
				title: i18n.confirmDelete,
				text: selected.length > 1 ? i18n.confirmDeleteTextBatch.replace('{0}', selected.length) : i18n.confirmDeleteTextSingle.replace('{0}', '1'),
				icon: 'warning',
				showCancelButton: true,
				confirmButtonColor: '#d33',
				cancelButtonColor: '#3085d6',
				confirmButtonText: i18n.yesDelete,
				cancelButtonText: i18n.cancel,
			});

			if (result.isConfirmed) {
				try {
					let errorCount = 0;
					let lastErrorMessage = '';

					for (const row of selected) {
						const [err] = await deleteRule(row.id);
						if (err) {
							errorCount++;
							lastErrorMessage = err.message;
						}
					}

					if (errorCount > 0) {
						Swal.fire(i18n.error, `Failed to delete ${errorCount} items. ${lastErrorMessage}`, 'error');
					} else {
						await Swal.fire(i18n.success, i18n.success, 'success');
					}
					Datatable.refresh();
				} catch (err) {
					Swal.fire(i18n.error, err.message, 'error');
				}
			}
		},
	};

	Controller.init();
});
