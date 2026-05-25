import { Validator } from '../../../common/validator.js';
import { getFunctionsWithGroupStatus } from '../../../api/function.api.js';
import { toggleGroupFunctions } from '../../../api/role.api.js';
import { StateManager } from './state_manager.js';
import { Toast, Alert } from '../../../common/notification_util.js';

export const MainForm = (() => {
	const elements = {
		modal: null,
		form: null,
		title: null,
		roleId: null,
		name: null,
		groupCode: null,
		description: null,
		submitBtn: null,
	};

	let bootstrapModal = null;

	const init = () => {
		elements.modal = document.getElementById('roleModal');
		elements.form = document.getElementById('roleForm');
		elements.title = document.getElementById('modalTitle');
		elements.roleId = document.getElementById('roleId');
		elements.name = document.getElementById('name');
		elements.groupCode = document.getElementById('groupCode');
		elements.description = document.getElementById('description');
		elements.submitBtn = document.getElementById('btnSubmitRole');

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

	const reset = () => {
		if (elements.form) elements.form.reset();
		if (elements.roleId) elements.roleId.value = '';
		clearValidation();
	};

	const open = (data = null) => {
		reset();
		const isEdit = !!data;
		const i18n = StateManager.getI18n();

		if (elements.title) elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;
		if (elements.groupCode) {
			elements.groupCode.readOnly = isEdit;
			elements.groupCode.parentElement?.classList.toggle('opacity-75', isEdit);
		}

		if (isEdit && data) {
			Object.entries(data).forEach(([key, value]) => {
				const input = elements.form.elements[key];
				if (input) input.value = value ?? '';
			});
			if (elements.roleId) elements.roleId.value = data.id || '';
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

		if (!Validator.name.isBlank(data.name)) {
			await Alert.warning((i18n.valRequired || '').replace('{0}', i18n.colName || ''), i18n.error || 'Error');
			elements.name?.focus();
			return null;
		}
		if (!Validator.name.isLowerMin(data.name) || !Validator.name.isHigherMax(data.name)) {
			await Alert.warning(i18n.valNameLen || '', i18n.error || 'Error');
			elements.name?.focus();
			return null;
		}

		if (!data.id) {
			if (!Validator.groupCode.isBlank(data.groupCode)) {
				await Alert.warning((i18n.valRequired || '').replace('{0}', i18n.colCode || ''), i18n.error || 'Error');
				elements.groupCode?.focus();
				return null;
			}
			if (!Validator.groupCode.isHigherMax(data.groupCode)) {
				await Alert.warning(i18n.valCodeLen || '', i18n.error || 'Error');
				elements.groupCode?.focus();
				return null;
			}
			if (!Validator.groupCode.isValidFormat(data.groupCode)) {
				await Alert.warning(i18n.valGroupCodeFormat || '', i18n.error || 'Error');
				elements.groupCode?.focus();
				return null;
			}
			if (data.groupCode) data.groupCode = data.groupCode.toUpperCase();
		}

		if (data.description && !Validator.description.isHigherMax(data.description)) {
			await Alert.warning(i18n.valDescriptionLen || '', i18n.error || 'Error');
			elements.description?.focus();
			return null;
		}

		return data;
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
	let currentGroupId = null;
	let initialStates = {};

	const init = () => {
		elements.modal = document.getElementById('manageFunctionsModal');
		elements.title = document.getElementById('mappingModalTitle');
		elements.list = document.getElementById('functionsList');
		elements.loader = document.getElementById('functionsListLoader');
		elements.search = document.getElementById('funcSearch');
		elements.saveBtn = document.getElementById('btnSaveFunctions');

		if (elements.modal) {
			bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		}

		elements.search?.addEventListener('input', (e) => handleSearch(e.target.value));
		elements.saveBtn?.addEventListener('click', () => save());
	};

	const open = async (role) => {
		const i18n = StateManager.getI18n();
		currentGroupId = role.id;
		if (elements.title) elements.title.textContent = (i18n.mappingTitle || '').replace('{0}', role.name);
		if (elements.list) elements.list.style.display = 'none';
		if (elements.loader) elements.loader.classList.remove('d-none');
		if (elements.search) elements.search.value = '';
		initialStates = {};

		bootstrapModal?.show();
		window.renderIcons?.();

		try {
			const [err, res] = await getFunctionsWithGroupStatus(currentGroupId);
			if (err) throw err;

			const functions = res.data || [];
			renderList(functions);
			functions.forEach((f) => {
				initialStates[f.functionCode] = f.isAssignedToGroup;
			});
		} catch (err) {
			console.error('Failed to load permissions:', err);
			if (elements.list) elements.list.innerHTML = `<div class="alert alert-danger">${i18n.error || ''}</div>`;
		} finally {
			if (elements.loader) elements.loader.classList.add('d-none');
			if (elements.list) elements.list.style.display = 'block';
			window.renderIcons?.();
		}
	};

	const renderList = (functions) => {
		const i18n = StateManager.getI18n();
		if (!elements.list) return;

		if (functions.length === 0) {
			elements.list.innerHTML = `<div class="text-center py-4 text-muted">${i18n.noData || ''}</div>`;
			return;
		}

		elements.list.innerHTML = functions
			.map(
				(func) => `
			<div class="selection-list-item d-flex align-items-start func-item" data-search="${func.name.toLowerCase()} ${func.functionCode.toLowerCase()}">
				<div class="form-check pt-1">
					<input class="form-check-input scale-checkbox func-chk" type="checkbox" 
						id="func_${func.id}" 
						data-code="${func.functionCode}"
						${func.isAssignedToGroup ? 'checked' : ''}>
					<label class="form-check-label" for="func_${func.id}"></label>
				</div>
				<div class="ms-2 w-100 cursor-pointer" onclick="document.getElementById('func_${func.id}').click()">
					<div class="d-flex justify-content-between align-items-center">
						<div class="fw-bold text-dark">${func.name}</div>
						<span class="badge bg-light text-muted border badge-code small">${func.functionCode}</span>
					</div>
					<div class="small text-muted mt-1">${func.description || '--'}</div>
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
		const items = elements.list.querySelectorAll('.func-item');
		items.forEach((item) => {
			const text = item.dataset.search;
			item.style.display = text.includes(q) ? 'flex' : 'none';
		});
	};

	const getChanges = () => {
		const toggles = {};
		if (!elements.list) return null;
		const checks = elements.list.querySelectorAll('.func-chk');
		let hasChanges = false;

		checks.forEach((chk) => {
			const code = chk.dataset.code;
			const isChecked = chk.checked;
			if (isChecked !== initialStates[code]) {
				toggles[code] = isChecked;
				hasChanges = true;
			}
		});

		return hasChanges ? toggles : null;
	};

	const save = async () => {
		const i18n = StateManager.getI18n();
		const toggles = getChanges();
		if (!toggles) {
			Toast.info(i18n.mappingNoChanges || 'Không có thay đổi');
			bootstrapModal?.hide();
			return;
		}

		const originalHtml = elements.saveBtn.innerHTML;
		elements.saveBtn.disabled = true;
		elements.saveBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing || ''}`;

		try {
			const [err, res] = await toggleGroupFunctions({
				groupId: currentGroupId,
				functionToggles: toggles,
			});

			if (err) throw err;

			const added = Object.values(toggles).filter((v) => v).length;
			const removed = Object.values(toggles).filter((v) => !v).length;

			Toast.success((i18n.mappingSuccess || '').replace('{0}', added).replace('{1}', removed));
			bootstrapModal?.hide();
		} catch (err) {
			console.error('Save mapping error:', err);
			Toast.error(i18n.mappingError || 'An error occurred');
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
