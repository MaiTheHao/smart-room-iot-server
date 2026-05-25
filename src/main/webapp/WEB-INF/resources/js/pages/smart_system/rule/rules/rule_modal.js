import { createRule, updateRule } from '../../../../api/rule.api.js';
import { Validator } from '../../../../common/validator.js';
import { Toast, Alert } from '../../../../common/notification_util.js';

export const RuleModal = (() => {
	const elements = {
		modal: null,
		form: null,
		title: null,
		id: null,
		name: null,
		priority: null,
		intervalSeconds: null,
		submitBtn: null,
		inputs: [],
		feedbacks: []
	};

	let bootstrapModal = null;
	let currentData = null;
	const { i18n } = window.__RULE_CONFIG__;

	const init = () => {
		elements.modal = document.getElementById('ruleModal');
		elements.form = document.getElementById('ruleForm');
		elements.title = document.getElementById('modalTitle');
		elements.id = document.getElementById('ruleId');
		elements.name = document.getElementById('name');
		elements.priority = document.getElementById('priority');
		elements.intervalSeconds = document.getElementById('intervalSeconds');
		elements.submitBtn = document.getElementById('btnSubmitRule');

		if (!elements.modal) return;
		bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		elements.inputs = elements.form.querySelectorAll('.form-control');
		elements.feedbacks = elements.form.querySelectorAll('.invalid-feedback');
	};

	const clearValidation = () => {
		elements.inputs.forEach((el) => el.classList.remove('is-invalid'));
		elements.feedbacks.forEach((el) => (el.textContent = ''));
	};

	const open = (data = null) => {
		currentData = data;
		elements.form.reset();
		elements.id.value = '';
		clearValidation();

		const isEdit = !!data;
		elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;

		if (isEdit) {
			elements.id.value = data.id || '';
			elements.name.value = data.name || '';
			elements.priority.value = data.priority !== undefined ? data.priority : '1';
			elements.intervalSeconds.value = data.intervalSeconds !== undefined ? data.intervalSeconds : '60';
		} else {
			elements.priority.value = '1';
			elements.intervalSeconds.value = '60';
		}

		bootstrapModal?.show();
		window.renderIcons?.();
	};

	const validate = async () => {
		const formData = new FormData(elements.form);
		const data = Object.fromEntries(formData.entries());

		clearValidation();

		if (!Validator.name.isBlank(data.name)) {
			await Alert.warning(i18n.valRequired.replace('{0}', 'Name'), i18n.error || 'Error');
			elements.name?.focus();
			return null;
		}

		if (!data.priority || isNaN(data.priority)) {
			await Alert.warning(i18n.valPriorityRequired, i18n.error || 'Error');
			elements.priority?.focus();
			return null;
		} else if (parseInt(data.priority, 10) < 0) {
			await Alert.warning(i18n.valPriorityMin, i18n.error || 'Error');
			elements.priority?.focus();
			return null;
		}

		if (!data.intervalSeconds || isNaN(data.intervalSeconds)) {
			await Alert.warning(i18n.valIntervalRequired, i18n.error || 'Error');
			elements.intervalSeconds?.focus();
			return null;
		} else if (parseInt(data.intervalSeconds, 10) < 60) {
			await Alert.warning(i18n.valIntervalMin, i18n.error || 'Error');
			elements.intervalSeconds?.focus();
			return null;
		}

		return data;
	};

	const submit = async (e, onRefresh) => {
		e.preventDefault();
		const data = await validate();
		if (!data) return;

		const originalHtml = elements.submitBtn.innerHTML;
		elements.submitBtn.disabled = true;
		elements.submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing}`;

		try {
			const isUpdate = !!data.id;
			const payload = {
				name: data.name,
				priority: parseInt(data.priority, 10),
				intervalSeconds: parseInt(data.intervalSeconds, 10),
				conditions: isUpdate ? (currentData?.conditions || []) : [],
				actions: isUpdate ? (currentData?.actions || []) : []
			};

			if (!isUpdate) {
				payload.isActive = true;
			}

			const [err, res] = isUpdate ? await updateRule(data.id, payload) : await createRule(payload);

			if (err) {
				Toast.error(err.message || i18n.error);
			} else {
				Toast.success(isUpdate ? i18n.updatedSuccess : i18n.createdSuccess);
				bootstrapModal?.hide();
				onRefresh();
			}
		} catch (error) {
			console.error('Submit error:', error);
			Toast.error(i18n.error);
		} finally {
			elements.submitBtn.disabled = false;
			elements.submitBtn.innerHTML = originalHtml;
		}
	};

	return { init, open, submit };
})();
