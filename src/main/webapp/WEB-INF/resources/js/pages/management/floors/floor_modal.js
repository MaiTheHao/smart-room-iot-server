import { Validator } from '../../../common/validator.js';
import { StateManager } from './state_manager.js';
import { Alert } from '../../../common/notification_util.js';

export const FloorModal = (() => {
	const elements = {
		modal: null,
		form: null,
		title: null,
		floorId: null,
		name: null,
		code: null,
		level: null,
		description: null,
		submitBtn: null,
	};

	let bootstrapModal = null;

	const init = () => {
		elements.modal = document.getElementById('floorModal');
		elements.form = document.getElementById('floorForm');
		elements.title = document.getElementById('modalTitle');
		elements.floorId = document.getElementById('floorId');
		elements.name = document.getElementById('name');
		elements.code = document.getElementById('code');
		elements.level = document.getElementById('level');
		elements.description = document.getElementById('description');
		elements.submitBtn = document.getElementById('btnSubmitFloor');

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
		if (elements.floorId) elements.floorId.value = '';
		clearValidation();
	};

	const open = (data = null) => {
		reset();
		const isEdit = !!data;
		const i18n = StateManager.getI18n();

		if (elements.title) elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;
		if (elements.code) {
			elements.code.readOnly = isEdit;
			elements.code.parentElement?.classList.toggle('opacity-75', isEdit);
		}

		if (isEdit && data) {
			Object.entries(data).forEach(([key, value]) => {
				const input = elements.form.elements[key];
				if (input) input.value = value ?? '';
			});
			if (elements.floorId) elements.floorId.value = data.id || '';
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

		if (!Validator.code.isBlank(data.code)) {
			await Alert.warning((i18n.valRequired || '').replace('{0}', i18n.colCode || ''), i18n.error || 'Error');
			elements.code?.focus();
			return null;
		}
		if (!Validator.code.isHigherMax(data.code)) {
			await Alert.warning(i18n.valCodeLen || '', i18n.error || 'Error');
			elements.code?.focus();
			return null;
		}

		if (!Validator.level.isValidFormat(data.level)) {
			await Alert.warning(i18n.valLevelInvalid || '', i18n.error || 'Error');
			elements.level?.focus();
			return null;
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
