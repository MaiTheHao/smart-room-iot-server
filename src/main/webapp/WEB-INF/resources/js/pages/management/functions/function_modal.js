import { CreateSysFunctionDto, UpdateSysFunctionDto } from '../../../types/system.domain.js';
import { StateManager } from './state_manager.js';
import { Alert } from '../../../common/notification_util.js';

export const FunctionModal = (() => {
  const elements = {
    modal: null,
    form: null,
    title: null,
    functionId: null,
    name: null,
    functionCode: null,
    description: null,
    submitBtn: null,
  };

  let bootstrapModal = null;

  const init = () => {
    elements.modal = document.getElementById('functionModal');
    elements.form = document.getElementById('functionForm');
    elements.title = document.getElementById('modalTitle');
    elements.functionId = document.getElementById('functionId');
    elements.name = document.getElementById('name');
    elements.functionCode = document.getElementById('functionCode');
    elements.description = document.getElementById('description');
    elements.submitBtn = document.getElementById('btnSubmitFunction');

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
    if (elements.functionId) elements.functionId.value = '';
    clearValidation();
  };

  const open = (data = null) => {
    reset();
    const isEdit = !!data;
    const i18n = StateManager.getI18n();

    if (elements.title) elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;
    if (elements.functionCode) {
      elements.functionCode.readOnly = isEdit;
      elements.functionCode.parentElement?.classList.toggle('opacity-75', isEdit);
    }

    if (isEdit && data) {
      Object.entries(data).forEach(([key, value]) => {
        const input = elements.form.elements[key];
        if (input) input.value = value ?? '';
      });
      if (elements.functionId) elements.functionId.value = data.id || '';
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

    const isUpdate = !!data.id;
    const builder = isUpdate
      ? new UpdateSysFunctionDto.Builder()
          .setName(data.name)
          .setDescription(data.description)
      : new CreateSysFunctionDto.Builder()
          .setFunctionCode(data.functionCode)
          .setName(data.name)
          .setDescription(data.description);

    const result = builder.validate();
    if (!result.isValid) {
      const firstField = Object.keys(result.errors)[0];
      const msgKey = result.errors[firstField];
      await Alert.warning(i18n[msgKey] || i18n.valRequired, i18n.error || 'Error');
      elements[firstField]?.focus();
      return null;
    }

    const payload = builder.build();
    return isUpdate ? { ...payload, id: data.id } : { ...payload };
  };

  return {
    init,
    open,
    close,
    validate,
    getElements: () => elements,
  };
})();
