import { getAll as getAllClients, deleteClient, create as createClient, update as updateClient, patchUpdate, deleteAllHardwareConfigs } from '../../api/user.api.js';
import { setup as setupGateway } from '../../api/system.api.js';
import { getGroupsWithClientStatus } from '../../api/group.api.js';
import { assignGroupsToClient, unassignGroupsFromClient } from '../../api/role.api.js';
import { TabulatorFull as Tabulator } from '../../lib/tabulator_esm.min.js';
import { Validator } from '../../common/validator.js';

document.addEventListener('DOMContentLoaded', () => {
	const tableContainer = document.querySelector('#clientsTable');
	if (!tableContainer) return;

	const config = window.__CLIENTS_CONFIG__ || {
		constants: {
			CLIENT_TYPE: {
				USER: 'USER',
				HARDWARE_GATEWAY: 'HARDWARE_GATEWAY',
			},
		},
		i18n: {},
	};

	const { constants, i18n } = config;

	const MainForm = (() => {
		const elements = {
			modal: document.getElementById('clientModal'),
			form: document.getElementById('clientForm'),
			title: document.getElementById('modalTitle'),
			clientId: document.getElementById('clientId'),
			username: document.getElementById('username'),
			password: document.getElementById('password'),
			passwordContainer: document.getElementById('passwordFieldContainer'),
			clientType: document.getElementById('clientType'),
			gatewayField: document.getElementById('gatewayPasswordField'),
			submitBtn: document.getElementById('btnSubmitClient'),
		};

		const bootstrapModal = elements.modal && typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		const inputs = elements.form.querySelectorAll('.form-control, .form-select');
		const feedbacks = elements.form.querySelectorAll('.invalid-feedback');

		const toggleGatewayFields = (visible) => {
			if (elements.gatewayField) {
				elements.gatewayField.style.display = visible ? 'block' : 'none';
			}
		};

		const clearValidation = () => {
			inputs.forEach((el) => el.classList.remove('is-invalid'));
			feedbacks.forEach((el) => (el.textContent = ''));
		};

		const reset = () => {
			elements.form.reset();
			elements.clientId.value = '';
			clearValidation();
		};

		const open = (data = null) => {
			reset();
			const isEdit = !!data;

			elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;
			elements.username.readOnly = isEdit;
			elements.username.parentElement?.classList.toggle('opacity-75', isEdit);

			if (elements.passwordContainer) {
				elements.passwordContainer.style.display = isEdit ? 'none' : 'block';
			}

			if (isEdit) {
				Object.entries(data).forEach(([key, value]) => {
					const input = elements.form.elements[key];
					if (input) input.value = value ?? '';
				});
				elements.clientId.value = data.id || '';
				toggleGatewayFields(false);
			} else {
				toggleGatewayFields(elements.clientType.value === constants.CLIENT_TYPE.HARDWARE_GATEWAY);
			}

			bootstrapModal?.show();
			window.renderIcons?.();
		};

		const validate = () => {
			const formData = new FormData(elements.form);
			const data = Object.fromEntries(formData.entries());
			let isValid = true;

			clearValidation();

			const setError = (field, msg) => {
				const input = elements.form.querySelector(`#${field}`);
				const feedback = elements.form.querySelector(`#val-${field}`);
				if (input) input.classList.add('is-invalid');
				if (feedback) feedback.textContent = msg;
				isValid = false;
			};

			const isUpdate = !!data.id;

			if (!isUpdate) {
				if (!Validator.username.isBlank(data.username)) {
					setError('username', i18n.valRequired.replace('{0}', i18n.colUsername));
				} else if (!Validator.username.isLowerMin(data.username) || !Validator.username.isHigherMax(data.username)) {
					setError('username', i18n.valUsernameLen);
				}

				if (!Validator.password.isBlank(data.password)) {
					setError('password', i18n.valRequired.replace('{0}', 'Password'));
				} else if (!Validator.password.isLowerMin(data.password) || !Validator.password.isHigherMax(data.password)) {
					setError('password', i18n.valPasswordLen);
				}
			}

			if (!Validator.clientType.isBlank(data.clientType)) {
				setError('clientType', i18n.valRequired.replace('{0}', i18n.colType));
			}

			if (data.clientType === constants.CLIENT_TYPE.HARDWARE_GATEWAY) {
				if (!Validator.ip.isBlank(data.ipAddress)) {
					setError('ipAddress', i18n.valRequired.replace('{0}', i18n.colIp));
				}
			}

			if (data.ipAddress && !Validator.ip.isValidFormat(data.ipAddress)) {
				setError('ipAddress', i18n.valIpInvalid);
			}

			if (data.macAddress && !Validator.mac.isValidFormat(data.macAddress)) {
				setError('macAddress', i18n.valMacInvalid);
			}

			if (data.avatarUrl && !Validator.url.isValidFormat(data.avatarUrl)) {
				setError('avatarUrl', i18n.valUrlInvalid);
			}

			if (!isUpdate && data.clientType === constants.CLIENT_TYPE.HARDWARE_GATEWAY) {
				if (!Validator.generic.isBlank(data.gatewayPassword)) {
					data.gatewayPassword = data.password;
				}
			}

			if (isValid) {
				return Object.fromEntries(Object.entries(data).filter(([_, v]) => Validator.generic.isBlank(v)));
			}
			return null;
		};

		return {
			open,
			close: () => bootstrapModal?.hide(),
			validate,
			elements,
		};
	})();

	const PasswordForm = (() => {
		const elements = {
			modal: document.getElementById('passwordModal'),
			form: document.getElementById('passwordForm'),
			title: document.getElementById('pwdModalTitle'),
			clientId: document.getElementById('pwdClientId'),
			gatewayContainer: document.getElementById('pwdGatewayContainer'),
			submitBtn: document.getElementById('btnSubmitPassword'),
		};

		const bootstrapModal = elements.modal && typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		const inputs = elements.form.querySelectorAll('.form-control');
		const feedbacks = elements.form.querySelectorAll('.invalid-feedback');

		const clearValidation = () => {
			inputs.forEach((el) => el.classList.remove('is-invalid'));
			feedbacks.forEach((el) => (el.textContent = ''));
		};

		const open = (clientData) => {
			elements.form.reset();
			clearValidation();
			elements.clientId.value = clientData.id;
			elements.title.textContent = i18n.pwdTitle.replace('{0}', clientData.username);

			const isGateway = clientData.clientType === constants.CLIENT_TYPE.HARDWARE_GATEWAY;
			elements.gatewayContainer.style.display = isGateway ? 'block' : 'none';

			bootstrapModal?.show();
			window.renderIcons?.();
		};

		const validate = () => {
			const formData = new FormData(elements.form);
			const data = Object.fromEntries(formData.entries());
			let isValid = true;

			clearValidation();

			const setError = (id, msg) => {
				const input = document.getElementById(id);
				const feedback = document.getElementById(`val-${id}`);
				if (input) input.classList.add('is-invalid');
				if (feedback) feedback.textContent = msg;
				isValid = false;
			};

			const validatePwd = (val, id) => {
				if (val && (!Validator.password.isLowerMin(val) || !Validator.password.isHigherMax(val))) {
					setError(id, i18n.valPasswordLen);
					return false;
				}
				return true;
			};

			const clientPwd = data.password;
			const confirmClientPwd = document.getElementById('confirmPassword').value;

			if (clientPwd) {
				if (validatePwd(clientPwd, 'newPassword')) {
					if (clientPwd !== confirmClientPwd) {
						setError('confirmPassword', i18n.pwdMatchError);
					}
				}
			}

			if (elements.gatewayContainer.style.display !== 'none') {
				const gatewayPwd = data.gatewayPassword;
				const confirmGatewayPwd = document.getElementById('confirmGatewayPassword').value;
				if (gatewayPwd) {
					if (validatePwd(gatewayPwd, 'newGatewayPassword')) {
						if (gatewayPwd !== confirmGatewayPwd) {
							setError('confirmGatewayPassword', i18n.pwdMatchError);
						}
					}
				}
			}

			// At least one must be provided
			if (!data.password && !data.gatewayPassword) {
				isValid = false;
				Swal.fire(i18n.info, i18n.pwdAtLeastOne, 'info');
			}

			if (isValid) {
				return Object.fromEntries(Object.entries(data).filter(([_, v]) => Validator.generic.isBlank(v)));
			}
			return null;
		};

		return {
			open,
			close: () => bootstrapModal?.hide(),
			validate,
			elements,
		};
	})();

	const Datatable = (() => {
		let table = null;
		let isLoadingData = false;

		const init = (onEditRow, onSelectionChange, onPasswordRow) => {
			table = new Tabulator('#clientsTable', {
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
						title: i18n.colUsername,
						field: 'username',
						headerFilter: 'input',
						headerFilterPlaceholder: i18n.placeholderSearch,
						formatter: (cell) => {
							const client = cell.getData();
							const avatarUrl = client.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(client.username)}&background=random`;
							return `
								<div class="d-flex align-items-center h-100 py-1">
									<img src="${avatarUrl}" class="rounded-circle border shadow-sm me-3" style="width: 32px; height: 32px; object-fit: cover;" alt="${client.username}">
									<div class="fw-bold text-dark">${client.username}</div>
								</div>`;
						},
					},
					{
						title: i18n.colType,
						field: 'clientType',
						headerFilter: 'list',
						headerFilterParams: {
							values: {
								'': i18n.allTypes,
								USER: i18n.typeUser,
								HARDWARE_GATEWAY: i18n.typeGateway,
							},
						},
						formatter: (cell) => {
							const value = cell.getValue();
							const isUser = value === constants.CLIENT_TYPE.USER;
							const icon = isUser ? 'user' : 'cpu';
							const cls = isUser ? 'bg-primary text-primary' : 'bg-purple text-purple';
							const customStyle = !isUser ? 'style="--bs-purple: #6610f2; color: #6610f2 !important; background-color: rgba(102, 16, 242, 0.1) !important; border-color: rgba(102, 16, 242, 0.25) !important;"' : '';

							return `
								<div class="d-flex align-items-center h-100">
									<span class="badge rounded-pill bg-opacity-10 border border-opacity-25 px-3 py-2 ${cls}" ${customStyle}>
										<i data-lucide="${icon}" class="lucide-sm me-1"></i>${isUser ? i18n.typeUser : i18n.typeGateway}
									</span>
								</div>`;
						},
					},
					{
						title: i18n.colIp,
						field: 'ipAddress',
						headerFilter: 'input',
						formatter: (cell) => `<div class="d-flex align-items-center h-100"><span class="font-monospace small text-muted">${cell.getValue() || '--'}</span></div>`,
					},
					{
						title: i18n.colMac,
						field: 'macAddress',
						headerFilter: 'input',
						formatter: (cell) => `<div class="d-flex align-items-center h-100"><span class="font-monospace small text-muted">${cell.getValue() || '--'}</span></div>`,
					},
					{
						title: i18n.colLastLogin,
						field: 'lastLoginAt',
						formatter: (cell) => {
							const val = cell.getValue();
							return `<div class="d-flex align-items-center h-100">${val ? new Date(val).toLocaleString() : `<span class="text-muted small">${i18n.never}</span>`}</div>`;
						},
					},
					{
						title: i18n.colActions,
						hozAlign: 'center',
						headerSort: false,
						width: 200,
						formatter: (cell) => {
							const data = cell.getData();
							const isGateway = data.clientType === constants.CLIENT_TYPE.HARDWARE_GATEWAY;
							const setupBtn = isGateway
								? `
								<button class="btn btn-light btn-sm rounded-pill btn-setup me-1" data-id="${data.id}" data-username="${data.username}" title="${i18n.setupTitle}">
									<i data-lucide="file-cog" class="lucide-sm text-success"></i>
								</button>
								<button class="btn btn-light btn-sm rounded-pill btn-clear-config me-1" data-id="${data.id}" data-username="${data.username}" title="${i18n.clearConfigTitle}">
									<i data-lucide="file-x" class="lucide-sm text-danger"></i>
								</button>`
								: '';
							return `
								<div class="d-flex align-items-center justify-content-center h-100">
									${setupBtn}
									<button class="btn btn-light btn-sm rounded-pill btn-groups me-1" data-id="${data.id}" title="${i18n.mappingTitle || 'Manage Groups'}">
										<i data-lucide="shield" class="lucide-sm text-info"></i>
									</button>
									<button class="btn btn-light btn-sm rounded-pill btn-password me-1" data-id="${data.id}" title="${i18n.pwdChangeTitle || 'Change Password'}">
										<i data-lucide="key" class="lucide-sm text-warning"></i>
									</button>
									<button class="btn btn-light btn-sm rounded-pill btn-edit" data-id="${data.id}" title="${i18n.commonEdit || 'Edit'}">
										<i data-lucide="edit-3" class="lucide-sm text-primary"></i>
									</button>
								</div>`;
						},
					},
				],
			});

			// Events
			table.on('tableBuilt', refresh);
			table.on('rowSelectionChanged', (data) => onSelectionChange?.(data.length));
			table.on('renderComplete', () => window.renderIcons?.());

			// Action button delegation
			document.addEventListener('click', (e) => {
				const btnEdit = e.target.closest('.btn-edit');
				if (btnEdit) {
					const row = table.getRow(btnEdit.dataset.id);
					if (row) onEditRow?.(row.getData());
					return;
				}

				const btnPassword = e.target.closest('.btn-password');
				if (btnPassword) {
					const row = table.getRow(btnPassword.dataset.id);
					if (row) onPasswordRow?.(row.getData());
					return;
				}

				const btnSetup = e.target.closest('.btn-setup');
				if (btnSetup) {
					Controller.handleSetup(btnSetup.dataset.id, btnSetup.dataset.username);
					return;
				}

				const btnClear = e.target.closest('.btn-clear-config');
				if (btnClear) {
					Controller.handleClearHardwareConfig(btnClear.dataset.id, btnClear.dataset.username);
					return;
				}

				const btnGroups = e.target.closest('.btn-groups');
				if (btnGroups) {
					const row = table.getRow(btnGroups.dataset.id);
					if (row) MappingModule.open(row.getData());
				}
			});
		};

		const refresh = async () => {
			if (isLoadingData) return;

			const btnReload = document.getElementById('btnReload');
			const icon = btnReload?.querySelector('[data-lucide="refresh-cw"]');

			try {
				isLoadingData = true;
				if (btnReload) btnReload.disabled = true;
				if (icon) icon.classList.add('lucide-spin');

				const [err, res] = await getAllClients({ page: 0, size: 1000 });

				if (err) {
					table.setPlaceholder(`
						<div class="text-center py-5 text-danger">
							<i data-lucide="alert-circle" class="mb-2" style="width: 48px; height: 48px"></i>
							<p class="fw-bold">${err.message || i18n.error}</p>
						</div>`);
					table.setData([]);
					return;
				}

				table.setData(res.data.content || []);
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

	const MappingModule = (() => {
		const elements = {
			modal: document.getElementById('manageGroupsModal'),
			title: document.getElementById('mappingModalTitle'),
			list: document.getElementById('groupsList'),
			loader: document.getElementById('groupsListLoader'),
			search: document.getElementById('groupSearch'),
			saveBtn: document.getElementById('btnSaveGroups'),
		};

		const bootstrapModal = elements.modal ? new bootstrap.Modal(elements.modal) : null;
		let currentClientId = null;
		let initialStates = {}; // Map of groupId -> boolean

		const open = async (client) => {
			currentClientId = client.id;
			elements.title.textContent = i18n.mappingTitle.replace('{0}', client.username);
			elements.list.style.display = 'none';
			elements.loader.classList.remove('d-none');
			elements.search.value = '';
			initialStates = {};

			bootstrapModal.show();
			window.renderIcons?.();

			try {
				const [err, res] = await getGroupsWithClientStatus(currentClientId);
				if (err) throw err;

				const groupsWithStatus = res.data || [];
				renderList(groupsWithStatus);
				groupsWithStatus.forEach((g) => {
					initialStates[g.id] = g.isAssignedToClient;
				});
			} catch (err) {
				console.error('Failed to load groups:', err);
				elements.list.innerHTML = `<div class="alert alert-danger">${i18n.error}</div>`;
			} finally {
				elements.loader.classList.add('d-none');
				elements.list.style.display = 'block';
				window.renderIcons?.();
			}
		};

		const renderList = (groups) => {
			if (groups.length === 0) {
				elements.list.innerHTML = `<div class="text-center py-4 text-muted">${i18n.noData}</div>`;
				return;
			}

			elements.list.innerHTML = groups
				.map(
					(group) => `
				<div class="selection-list-item d-flex align-items-start group-item" 
                    data-search="${group.name.toLowerCase()} ${group.groupCode.toLowerCase()}">
					<div class="form-check pt-1">
						<input class="form-check-input scale-checkbox group-chk" type="checkbox" 
							id="group_${group.id}" 
							data-id="${group.id}"
							${group.isAssignedToClient ? 'checked' : ''}>
						<label class="form-check-label" for="group_${group.id}"></label>
					</div>
					<div class="ms-2 w-100 cursor-pointer" onclick="document.getElementById('group_${group.id}').click()">
						<div class="d-flex justify-content-between align-items-center">
							<div class="fw-bold text-dark">${group.name}</div>
							<span class="badge bg-light text-muted border small badge-code">${group.groupCode}</span>
						</div>
						<div class="small text-muted mt-1">${group.description || ''}</div>
					</div>
				</div>
			`,
				)
				.join('');
			window.renderIcons?.(elements.list);
		};

		const handleSearch = (query) => {
			const q = query.toLowerCase();
			const items = elements.list.querySelectorAll('.group-item');
			items.forEach((item) => {
				const text = item.dataset.search;
				item.style.display = text.includes(q) ? 'flex' : 'none';
			});
		};

		const getChanges = () => {
			const added = [];
			const removed = [];
			const checks = elements.list.querySelectorAll('.group-chk');

			checks.forEach((chk) => {
				const id = parseInt(chk.dataset.id);
				const isChecked = chk.checked;
				if (isChecked !== initialStates[id]) {
					if (isChecked) added.push(id);
					else removed.push(id);
				}
			});

			return added.length > 0 || removed.length > 0 ? { added, removed } : null;
		};

		const save = async () => {
			const changes = getChanges();
			if (!changes) {
				Swal.fire(i18n.info, i18n.mappingNoChanges, 'info');
				bootstrapModal.hide();
				return;
			}

			const btn = elements.saveBtn;
			const originalHtml = btn.innerHTML;
			btn.disabled = true;
			btn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing}`;

			try {
				if (changes.added.length > 0) {
					const [err] = await assignGroupsToClient({ clientId: currentClientId, groupIds: changes.added });
					if (err) throw err;
				}

				if (changes.removed.length > 0) {
					const [err] = await unassignGroupsFromClient({ clientId: currentClientId, groupIds: changes.removed });
					if (err) throw err;
				}

				Swal.fire(i18n.success, i18n.mappingSuccess.replace('{0}', changes.added.length).replace('{1}', changes.removed.length), 'success');
				bootstrapModal.hide();
			} catch (err) {
				console.error('Save groups error:', err);
				Swal.fire(i18n.error, err.message || i18n.mappingError, 'error');
			} finally {
				btn.disabled = false;
				btn.innerHTML = originalHtml;
			}
		};

		elements.search?.addEventListener('input', (e) => handleSearch(e.target.value));
		elements.saveBtn?.addEventListener('click', () => save());

		return { open };
	})();

	const Controller = {
		init() {
			Datatable.init(
				(data) => MainForm.open(data),
				(count) => {
					const btnDelete = document.getElementById('btnDeleteSelected');
					if (btnDelete) btnDelete.disabled = count === 0;
				},
				(data) => PasswordForm.open(data),
			);

			this.bindEvents();
		},

		bindEvents() {
			document.getElementById('btnReload')?.addEventListener('click', () => Datatable.refresh());
			document.getElementById('btnAdd')?.addEventListener('click', () => MainForm.open());
			document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());

			MainForm.elements.clientType?.addEventListener('change', (e) => {
				const isGateway = e.target.value === constants.CLIENT_TYPE.HARDWARE_GATEWAY;
				const isEdit = !!MainForm.elements.clientId.value;
				MainForm.elements.gatewayField.style.display = isGateway && !isEdit ? 'block' : 'none';
			});

			MainForm.elements.form?.addEventListener('submit', (e) => this.handleFormSubmit(e));
			PasswordForm.elements.form?.addEventListener('submit', (e) => this.handlePasswordSubmit(e));
		},

		async handleFormSubmit(e) {
			e.preventDefault();

			const data = MainForm.validate();
			if (!data) return;

			const { submitBtn } = MainForm.elements;
			const originalHtml = submitBtn.innerHTML;

			submitBtn.disabled = true;
			submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing}`;

			try {
				const isUpdate = !!data.id;
				const [err, res] = isUpdate ? await patchUpdate(data.id, data) : await createClient(data);

				if (err) {
					Swal.fire(i18n.error, err.message || i18n.error, 'error');
				} else {
					Swal.fire(i18n.success, isUpdate ? i18n.updatedSuccess : i18n.createdSuccess, 'success');
					MainForm.close();
					Datatable.refresh();
				}
			} catch (error) {
				console.error('Submit error:', error);
				Swal.fire(i18n.error, i18n.error, 'error');
			} finally {
				submitBtn.disabled = false;
				submitBtn.innerHTML = originalHtml;
			}
		},

		async handlePasswordSubmit(e) {
			e.preventDefault();
			const data = PasswordForm.validate();
			if (!data) return;

			const { submitBtn } = PasswordForm.elements;
			const originalHtml = submitBtn.innerHTML;

			submitBtn.disabled = true;
			submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing}`;

			try {
				const [err, res] = await patchUpdate(data.id, data);

				if (err) {
					Swal.fire(i18n.error, err.message || i18n.pwdError, 'error');
				} else {
					Swal.fire(i18n.success, i18n.pwdSuccess, 'success');
					PasswordForm.close();
				}
			} catch (error) {
				console.error('Password submit error:', error);
				Swal.fire(i18n.error, i18n.error, 'error');
			} finally {
				submitBtn.disabled = false;
				submitBtn.innerHTML = originalHtml;
			}
		},

		async handleBatchDelete() {
			const selected = Datatable.getSelectedData();
			if (selected.length === 0) return;

			const result = await Swal.fire({
				title: i18n.confirmDelete,
				text: selected.length > 1 ? i18n.confirmDeleteTextBatch.replace('{0}', selected.length) : i18n.confirmDeleteTextSingle.replace('{0}', selected.length),
				icon: 'warning',
				showCancelButton: true,
				confirmButtonColor: '#d33',
				cancelButtonColor: '#3085d6',
				confirmButtonText: i18n.yesDelete,
				cancelButtonText: i18n.cancel,
			});

			if (result.isConfirmed) {
				try {
					const deletePromises = selected.map((row) => deleteClient(row.id));
					const results = await Promise.all(deletePromises);

					const errors = results.filter(([err]) => err !== null);
					if (errors.length > 0) {
						throw new Error(`Failed to delete ${errors.length} items`);
					}

					await Swal.fire(i18n.success, i18n.success, 'success');
					Datatable.refresh();
				} catch (err) {
					Swal.fire(i18n.error, err.message, 'error');
				}
			}
		},

		async handleSetup(id, username) {
			const result = await Swal.fire({
				title: i18n.setupTitle,
				text: i18n.setupText.replace('{0}', username),
				icon: 'question',
				showCancelButton: true,
				confirmButtonText: i18n.confirm || 'Confirm',
				cancelButtonText: i18n.cancel || 'Cancel',
			});

			if (result.isConfirmed) {
				Swal.fire({
					title: i18n.setupProcessing,
					allowOutsideClick: false,
					didOpen: () => {
						Swal.showLoading();
					},
				});

				try {
					const [err, res] = await setupGateway(id);

					if (err) {
						Swal.fire(i18n.setupErrorTitle, err.message || i18n.error, 'error');
					} else {
						Swal.fire(i18n.setupSuccessTitle, i18n.setupSuccessText, 'success');
					}
				} catch (error) {
					console.error('Setup error:', error);
					Swal.fire(i18n.setupErrorTitle, i18n.error, 'error');
				}
			}
		},

		async handleClearHardwareConfig(id, username) {
			const result = await Swal.fire({
				title: i18n.clearConfigTitle,
				text: i18n.clearConfigText.replace('{0}', username),
				icon: 'warning',
				showCancelButton: true,
				confirmButtonColor: '#d33',
				confirmButtonText: i18n.confirm || 'Confirm',
				cancelButtonText: i18n.cancel || 'Cancel',
			});

			if (result.isConfirmed) {
				Swal.fire({
					title: i18n.clearConfigProcessing,
					allowOutsideClick: false,
					didOpen: () => {
						Swal.showLoading();
					},
				});

				try {
					const [err, res] = await deleteAllHardwareConfigs(id);

					if (err) {
						Swal.fire(i18n.error, err.message || i18n.error, 'error');
					} else {
						Swal.fire(i18n.success, i18n.clearConfigSuccess, 'success');
						Datatable.refresh();
					}
				} catch (error) {
					console.error('Clear hardware config error:', error);
					Swal.fire(i18n.error, i18n.error, 'error');
				}
			}
		},
	};

	Controller.init();
});
