import { Validator } from '../../../common/validator.js';
import { getGroupsWithClientStatus } from '../../../api/group.api.js';
import { assignGroupsToClient, unassignGroupsFromClient } from '../../../api/role.api.js';
import { StateManager } from './state_manager.js';
import { Toast, Alert } from '../../../common/notification_util.js';

export const MainForm = (() => {
	const elements = {
		modal: null,
		form: null,
		title: null,
		clientId: null,
		username: null,
		password: null,
		passwordContainer: null,
		clientType: null,
		gatewayField: null,
		submitBtn: null,
	};

	let bootstrapModal = null;

	const init = () => {
		elements.modal = document.getElementById('clientModal');
		elements.form = document.getElementById('clientForm');
		elements.title = document.getElementById('modalTitle');
		elements.clientId = document.getElementById('clientId');
		elements.username = document.getElementById('username');
		elements.password = document.getElementById('password');
		elements.passwordContainer = document.getElementById('passwordFieldContainer');
		elements.clientType = document.getElementById('clientType');
		elements.gatewayField = document.getElementById('gatewayPasswordField');
		elements.submitBtn = document.getElementById('btnSubmitClient');

		if (elements.modal) {
			bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		}

		elements.clientType?.addEventListener('change', (e) => {
			const isGateway = StateManager.isGateway(e.target.value);
			const isEdit = !!elements.clientId.value;
			toggleGatewayFields(isGateway && !isEdit);
		});
	};

	const toggleGatewayFields = (visible) => {
		if (elements.gatewayField) {
			elements.gatewayField.style.display = visible ? 'block' : 'none';
		}
	};

	const getInputsAndFeedbacks = () => {
		if (!elements.form) return { inputs: [], feedbacks: [] };
		return {
			inputs: elements.form.querySelectorAll('.form-control, .form-select'),
			feedbacks: elements.form.querySelectorAll('.invalid-feedback'),
		};
	};

	const clearValidation = () => {
		const { inputs, feedbacks } = getInputsAndFeedbacks();
		inputs.forEach((el) => el.classList.remove('is-invalid'));
		feedbacks.forEach((el) => (el.textContent = ''));
	};

	const reset = () => {
		if (elements.form) elements.form.reset();
		if (elements.clientId) elements.clientId.value = '';
		clearValidation();
	};

	const open = (data = null) => {
		reset();
		const isEdit = !!data;
		const i18n = StateManager.getI18n();
		const constants = StateManager.getConstants();

		if (elements.title) elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;
		if (elements.username) {
			elements.username.readOnly = isEdit;
			elements.username.parentElement?.classList.toggle('opacity-75', isEdit);
		}

		if (elements.passwordContainer) {
			elements.passwordContainer.style.display = isEdit ? 'none' : 'block';
		}

		if (isEdit && data) {
			Object.entries(data).forEach(([key, value]) => {
				const input = elements.form.elements[key];
				if (input) input.value = value ?? '';
			});
			if (elements.clientId) elements.clientId.value = data.id || '';
			toggleGatewayFields(false);
		} else {
			if (elements.clientType) {
				toggleGatewayFields(StateManager.isGateway(elements.clientType.value));
			}
		}

		bootstrapModal?.show();
		window.renderIcons?.();
	};

	const close = () => bootstrapModal?.hide();

	const validate = async () => {
		const i18n = StateManager.getI18n();
		const constants = StateManager.getConstants();
		const formData = new FormData(elements.form);
		const data = Object.fromEntries(formData.entries());

		clearValidation();

		const isUpdate = !!data.id;

		if (!isUpdate) {
			if (!Validator.username.isBlank(data.username)) {
				await Alert.warning((i18n.valRequired || '').replace('{0}', i18n.colUsername || ''), i18n.error || 'Error');
				elements.username?.focus();
				return null;
			}
			if (!Validator.username.isLowerMin(data.username) || !Validator.username.isHigherMax(data.username)) {
				await Alert.warning(i18n.valUsernameLen || '', i18n.error || 'Error');
				elements.username?.focus();
				return null;
			}

			if (!Validator.password.isBlank(data.password)) {
				await Alert.warning((i18n.valRequired || '').replace('{0}', 'Password'), i18n.error || 'Error');
				elements.password?.focus();
				return null;
			}
			if (!Validator.password.isLowerMin(data.password) || !Validator.password.isHigherMax(data.password)) {
				await Alert.warning(i18n.valPasswordLen || '', i18n.error || 'Error');
				elements.password?.focus();
				return null;
			}
		}

		if (!Validator.clientType.isBlank(data.clientType)) {
			await Alert.warning((i18n.valRequired || '').replace('{0}', i18n.colType || ''), i18n.error || 'Error');
			elements.clientType?.focus();
			return null;
		}

		if (StateManager.isGateway(data.clientType)) {
			if (!Validator.ip.isBlank(data.ipAddress)) {
				await Alert.warning((i18n.valRequired || '').replace('{0}', i18n.colIp || ''), i18n.error || 'Error');
				const ipEl = elements.form.querySelector('#ipAddress');
				ipEl?.focus();
				return null;
			}
		}

		if (data.ipAddress && !Validator.ip.isValidFormat(data.ipAddress)) {
			await Alert.warning(i18n.valIpInvalid || '', i18n.error || 'Error');
			const ipEl = elements.form.querySelector('#ipAddress');
			ipEl?.focus();
			return null;
		}

		if (data.macAddress && !Validator.mac.isValidFormat(data.macAddress)) {
			await Alert.warning(i18n.valMacInvalid || '', i18n.error || 'Error');
			const macEl = elements.form.querySelector('#macAddress');
			macEl?.focus();
			return null;
		}

		if (data.avatarUrl && !Validator.url.isValidFormat(data.avatarUrl)) {
			await Alert.warning(i18n.valUrlInvalid || '', i18n.error || 'Error');
			const avatarEl = elements.form.querySelector('#avatarUrl');
			avatarEl?.focus();
			return null;
		}

		if (!isUpdate && StateManager.isGateway(data.clientType)) {
			if (!Validator.generic.isBlank(data.gatewayPassword)) {
				data.gatewayPassword = data.password;
			}
		}

		return Object.fromEntries(Object.entries(data).filter(([_, v]) => Validator.generic.isBlank(v)));
	};

	return {
		init,
		open,
		close,
		validate,
		getElements: () => elements,
	};
})();

export const PasswordForm = (() => {
	const elements = {
		modal: null,
		form: null,
		title: null,
		clientId: null,
		gatewayContainer: null,
		submitBtn: null,
	};

	let bootstrapModal = null;

	const init = () => {
		elements.modal = document.getElementById('passwordModal');
		elements.form = document.getElementById('passwordForm');
		elements.title = document.getElementById('pwdModalTitle');
		elements.clientId = document.getElementById('pwdClientId');
		elements.gatewayContainer = document.getElementById('pwdGatewayContainer');
		elements.submitBtn = document.getElementById('btnSubmitPassword');

		if (elements.modal) {
			bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		}
	};

	const getInputsAndFeedbacks = () => {
		if (!elements.form) return { inputs: [], feedbacks: [] };
		return {
			inputs: elements.form.querySelectorAll('.form-control'),
			feedbacks: elements.form.querySelectorAll('.invalid-feedback'),
		};
	};

	const clearValidation = () => {
		const { inputs, feedbacks } = getInputsAndFeedbacks();
		inputs.forEach((el) => el.classList.remove('is-invalid'));
		feedbacks.forEach((el) => (el.textContent = ''));
	};

	const open = (clientData) => {
		const i18n = StateManager.getI18n();
		const constants = StateManager.getConstants();

		if (elements.form) elements.form.reset();
		clearValidation();
		if (elements.clientId) elements.clientId.value = clientData.id;
		if (elements.title) elements.title.textContent = (i18n.pwdTitle || '').replace('{0}', clientData.username);

		const isGateway = StateManager.isGateway(clientData.clientType);
		if (elements.gatewayContainer) {
			elements.gatewayContainer.style.display = isGateway ? 'block' : 'none';
		}

		bootstrapModal?.show();
		window.renderIcons?.();
	};

	const close = () => bootstrapModal?.hide();

	const validate = async () => {
		const i18n = StateManager.getI18n();
		const formData = new FormData(elements.form);
		const data = Object.fromEntries(formData.entries());

		clearValidation();

		const validatePwd = async (val, id) => {
			if (val && (!Validator.password.isLowerMin(val) || !Validator.password.isHigherMax(val))) {
				await Alert.warning(i18n.valPasswordLen || '', i18n.error || 'Error');
				const inputEl = document.getElementById(id);
				inputEl?.focus();
				return false;
			}
			return true;
		};

		const clientPwd = data.password;
		const confirmClientPwd = document.getElementById('confirmPassword')?.value;

		if (clientPwd) {
			if (await validatePwd(clientPwd, 'newPassword')) {
				if (clientPwd !== confirmClientPwd) {
					await Alert.warning(i18n.pwdMatchError || '', i18n.error || 'Error');
					const inputEl = document.getElementById('confirmPassword');
					inputEl?.focus();
					return null;
				}
			} else {
				return null;
			}
		}

		if (elements.gatewayContainer && elements.gatewayContainer.style.display !== 'none') {
			const gatewayPwd = data.gatewayPassword;
			const confirmGatewayPwd = document.getElementById('confirmGatewayPassword')?.value;
			if (gatewayPwd) {
				if (await validatePwd(gatewayPwd, 'newGatewayPassword')) {
					if (gatewayPwd !== confirmGatewayPwd) {
						await Alert.warning(i18n.pwdMatchError || '', i18n.error || 'Error');
						const inputEl = document.getElementById('confirmGatewayPassword');
						inputEl?.focus();
						return null;
					}
				} else {
					return null;
				}
			}
		}

		if (!data.password && !data.gatewayPassword) {
			Toast.info(i18n.pwdAtLeastOne || 'Cần đổi ít nhất 1 mật khẩu');
			return null;
		}

		return Object.fromEntries(Object.entries(data).filter(([_, v]) => Validator.generic.isBlank(v)));
	};

	return {
		init,
		open,
		close,
		validate,
		getElements: () => elements,
	};
})();

export const MappingModule = (() => {
	const elements = {
		modal: null,
		title: null,
		list: null,
		loader: null,
		search: null,
		saveBtn: null,
	};

	let bootstrapModal = null;
	let currentClientId = null;
	let initialStates = {};

	const init = () => {
		elements.modal = document.getElementById('manageGroupsModal');
		elements.title = document.getElementById('mappingModalTitle');
		elements.list = document.getElementById('groupsList');
		elements.loader = document.getElementById('groupsListLoader');
		elements.search = document.getElementById('groupSearch');
		elements.saveBtn = document.getElementById('btnSaveGroups');

		if (elements.modal) {
			bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		}

		elements.search?.addEventListener('input', (e) => handleSearch(e.target.value));
		elements.saveBtn?.addEventListener('click', () => save());
	};

	const open = async (client) => {
		const i18n = StateManager.getI18n();
		currentClientId = client.id;
		if (elements.title) elements.title.textContent = (i18n.mappingTitle || '').replace('{0}', client.username);
		if (elements.list) elements.list.style.display = 'none';
		if (elements.loader) elements.loader.classList.remove('d-none');
		if (elements.search) elements.search.value = '';
		initialStates = {};

		bootstrapModal?.show();
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
			if (elements.list) elements.list.innerHTML = `<div class="alert alert-danger">${i18n.error || ''}</div>`;
		} finally {
			if (elements.loader) elements.loader.classList.add('d-none');
			if (elements.list) elements.list.style.display = 'block';
			window.renderIcons?.();
		}
	};

	const renderList = (groups) => {
		const i18n = StateManager.getI18n();
		if (!elements.list) return;

		if (groups.length === 0) {
			elements.list.innerHTML = `<div class="text-center py-4 text-muted">${i18n.noData || ''}</div>`;
			return;
		}

		elements.list.innerHTML = groups
			.map(
				(group) => `
			<div class="selection-list-item d-flex align-items-start group-item" 
				data-search="${(group.name || '').toLowerCase()} ${(group.groupCode || '').toLowerCase()}">
				<div class="form-check pt-1">
					<input class="form-check-input scale-checkbox group-chk" type="checkbox" 
						id="group_${group.id}" 
						data-id="${group.id}"
						${group.isAssignedToClient ? 'checked' : ''}>
					<label class="form-check-label" for="group_${group.id}"></label>
				</div>
				<div class="ms-2 w-100 cursor-pointer" onclick="document.getElementById('group_${group.id}').click()">
					<div class="d-flex justify-content-between align-items-center">
						<div class="fw-bold text-dark">${group.name || ''}</div>
						<span class="badge bg-light text-muted border small badge-code">${group.groupCode || ''}</span>
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
		if (!elements.list) return;
		const items = elements.list.querySelectorAll('.group-item');
		items.forEach((item) => {
			const text = item.dataset.search;
			item.style.display = text.includes(q) ? 'flex' : 'none';
		});
	};

	const getChanges = () => {
		const added = [];
		const removed = [];
		if (!elements.list) return null;
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
		const i18n = StateManager.getI18n();
		const changes = getChanges();
		if (!changes) {
			Toast.info(i18n.mappingNoChanges || 'Không có thay đổi');
			bootstrapModal?.hide();
			return;
		}

		const originalHtml = elements.saveBtn.innerHTML;
		elements.saveBtn.disabled = true;
		elements.saveBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing || ''}`;

		try {
			if (changes.added.length > 0) {
				const [err] = await assignGroupsToClient({ clientId: currentClientId, groupIds: changes.added });
				if (err) throw err;
			}

			if (changes.removed.length > 0) {
				const [err] = await unassignGroupsFromClient({ clientId: currentClientId, groupIds: changes.removed });
				if (err) throw err;
			}

			Toast.success((i18n.mappingSuccess || '').replace('{0}', changes.added.length).replace('{1}', changes.removed.length));
			bootstrapModal?.hide();
		} catch (err) {
			console.error('Save groups error:', err);
			Toast.error(err.message || i18n.mappingError || 'An error occurred');
		} finally {
			elements.saveBtn.disabled = false;
			elements.saveBtn.innerHTML = originalHtml;
		}
	};

	return {
		init,
		open,
	};
})();
