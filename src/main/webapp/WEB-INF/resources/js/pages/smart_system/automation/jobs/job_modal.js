import {
  getAutomations,
  createAutomation,
  updateAutomation,
  deleteAutomation,
  toggleAutomationStatus,
  executeAutomationNow,
} from '../../../../api/automation.api.js';
import { Validator } from '../../../../common/validator.js';
import { CronUtils } from '../../../../common/cron_util.js';
import { UTCUtils } from '../../../../common/utc_util.js';

export const JobModal = (() => {
  const elements = {
    modal: null,
    form: null,
    title: null,
    id: null,
    name: null,
    description: null,
    scheduleType: null,
    weeklyGroup: null,
    monthlyGroup: null,
    monthlySelect: null,
    timeHour: null,
    timeMinute: null,
    timeSecond: null,
    submitBtn: null,
    inputs: [],
    feedbacks: [],
  };

  let bootstrapModal = null;
  const { i18n } = window.__AUTOMATION_CONFIG__;

  const init = () => {
    elements.modal = document.getElementById('automationModal');
    elements.form = document.getElementById('automationForm');
    elements.title = document.getElementById('modalTitle');
    elements.id = document.getElementById('automationId');
    elements.name = document.getElementById('name');
    elements.description = document.getElementById('description');
    elements.scheduleType = document.getElementById('scheduleType');
    elements.weeklyGroup = document.getElementById('weeklyGroup');
    elements.monthlyGroup = document.getElementById('monthlyGroup');
    elements.monthlySelect = document.getElementById('monthlySelect');
    elements.timeHour = document.getElementById('timeHour');
    elements.timeMinute = document.getElementById('timeMinute');
    elements.timeSecond = document.getElementById('timeSecond');
    elements.submitBtn = document.getElementById('btnSubmitAutomation');

    if (!elements.modal) return;
    bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
    elements.inputs = elements.form.querySelectorAll('.form-control');
    elements.feedbacks = elements.form.querySelectorAll('.invalid-feedback');

    const handleInput = (input) => {
      input.value = input.value.replace(/[^0-9]/g, '');
      if (input.value.length > 2) {
        input.value = input.value.slice(0, 2);
      }
    };
    const padAndClamp = (input, min, max) => {
      let val = parseInt(input.value, 10);
      if (isNaN(val)) {
        input.value = '00';
      } else {
        val = Math.min(Math.max(val, min), max);
        input.value = val.toString().padStart(2, '0');
      }
    };

    elements.timeHour.addEventListener('input', () => handleInput(elements.timeHour));
    elements.timeMinute.addEventListener('input', () => handleInput(elements.timeMinute));
    elements.timeSecond.addEventListener('input', () => handleInput(elements.timeSecond));

    elements.timeHour.addEventListener('blur', () => padAndClamp(elements.timeHour, 0, 23));
    elements.timeMinute.addEventListener('blur', () => padAndClamp(elements.timeMinute, 0, 59));
    elements.timeSecond.addEventListener('blur', () => padAndClamp(elements.timeSecond, 0, 59));

    elements.scheduleType.addEventListener('change', (e) => {
      const type = e.target.value;
      if (type === 'DAILY') {
        elements.weeklyGroup.classList.add('d-none');
        elements.monthlyGroup.classList.add('d-none');
      } else if (type === 'WEEKLY') {
        elements.weeklyGroup.classList.remove('d-none');
        elements.monthlyGroup.classList.add('d-none');
      } else if (type === 'MONTHLY') {
        elements.weeklyGroup.classList.add('d-none');
        elements.monthlyGroup.classList.remove('d-none');
      }
    });
  };

  const clearValidation = () => {
    elements.inputs.forEach((el) => el.classList.remove('is-invalid'));
    elements.feedbacks.forEach((el) => (el.textContent = ''));
  };

  const open = (data = null) => {
    elements.form.reset();
    elements.id.value = '';
    clearValidation();

    const isEdit = !!data;
    elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;

    if (isEdit) {
      elements.id.value = data.id || '';
      elements.name.value = data.name || '';
      elements.description.value = data.description || '';

      // Parse cron and set UI
      const parsedCron = CronUtils.fromCron(data.cronExpression);
      elements.scheduleType.value = parsedCron.type;
      elements.scheduleType.dispatchEvent(new Event('change'));

      elements.timeHour.value = parsedCron.hour.toString().padStart(2, '0');
      elements.timeMinute.value = parsedCron.minute.toString().padStart(2, '0');
      elements.timeSecond.value = parsedCron.second.toString().padStart(2, '0');

      if (parsedCron.type === 'WEEKLY' && parsedCron.daysOfWeek) {
        const checkboxes = elements.weeklyGroup.querySelectorAll('.form-check-input');
        checkboxes.forEach((cb) => {
          cb.checked = parsedCron.daysOfWeek.includes(cb.value);
        });
      } else if (parsedCron.type === 'MONTHLY' && parsedCron.dayOfMonth) {
        elements.monthlySelect.value = parsedCron.dayOfMonth;
      }
    } else {
      elements.scheduleType.value = 'DAILY';
      elements.scheduleType.dispatchEvent(new Event('change'));
      elements.timeHour.value = '00';
      elements.timeMinute.value = '00';
      elements.timeSecond.value = '00';
      elements.weeklyGroup
        .querySelectorAll('.form-check-input')
        .forEach((cb) => (cb.checked = false));
      elements.monthlySelect.value = '1';
    }

    bootstrapModal?.show();
    window.renderIcons?.();
  };

  const validateAndGetCron = () => {
    let isValid = true;
    const name = elements.name.value;
    const description = elements.description.value;

    clearValidation();

    const setError = (field, msg) => {
      const input = elements.form.querySelector(`#${field}`);
      const feedback = elements.form.querySelector(`#val-${field}`);
      if (input) input.classList.add('is-invalid');
      if (feedback) feedback.textContent = msg;
      isValid = false;
    };

    if (!Validator.name.isBlank(name)) {
      setError('name', i18n.valRequired.replace('{0}', 'Name'));
    }

    const scheduleType = elements.scheduleType.value;
    const hourVal = elements.timeHour.value.trim();
    const minuteVal = elements.timeMinute.value.trim();
    const secondVal = elements.timeSecond.value.trim();

    const hour = hourVal !== '' ? parseInt(hourVal, 10) : NaN;
    const minute = minuteVal !== '' ? parseInt(minuteVal, 10) : NaN;
    const second = secondVal !== '' ? parseInt(secondVal, 10) : NaN;

    if (
      isNaN(hour) || hour < 0 || hour > 23 ||
      isNaN(minute) || minute < 0 || minute > 59 ||
      isNaN(second) || second < 0 || second > 59
    ) {
      elements.timeHour.classList.add('is-invalid');
      elements.timeMinute.classList.add('is-invalid');
      elements.timeSecond.classList.add('is-invalid');
      const feedback = elements.form.querySelector('#val-time');
      if (feedback) {
        feedback.textContent = 'Please enter a valid time (HH: 0-23, MM: 0-59, SS: 0-59)';
      }
      isValid = false;
    }

    let daysOfWeek = [];
    let dayOfMonth = '1';

    if (scheduleType === 'WEEKLY') {
      elements.weeklyGroup.querySelectorAll('.form-check-input:checked').forEach((cb) => {
        daysOfWeek.push(cb.value);
      });
      if (daysOfWeek.length === 0) {
        // Fallback or validation error. Let's fallback to MON to be safe if they forgot.
        daysOfWeek = ['MON'];
      }
    } else if (scheduleType === 'MONTHLY') {
      dayOfMonth = elements.monthlySelect.value;
    }

    const generatedCron = CronUtils.toCron({
      type: scheduleType,
      hour,
      minute,
      second,
      daysOfWeek,
      dayOfMonth,
    });

    if (!isValid) return null;

    return {
      name,
      description,
      cronExpression: generatedCron,
    };
  };

  const submit = async (e, onRefresh) => {
    e.preventDefault();
    const data = validateAndGetCron();
    if (!data) return;

    const originalHtml = elements.submitBtn.innerHTML;
    elements.submitBtn.disabled = true;
    elements.submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing}`;

    try {
      const isUpdate = !!elements.id.value;

      // For PUT Automation, we must send ALL fields: name, description, cronExpression, isActive
      const payload = {
        name: data.name,
        description: data.description,
        cronExpression: data.cronExpression,
      };

      if (!isUpdate) {
        payload.isActive = true;
      } else {
        // If updating, we need to know the current isActive.
        // Since we are not storing isActive in the form hidden input, we can just assume true
        // or we should fetch the row from Datatable?
        // The backend actually might wipe it if we don't send it.
        // Let's ensure we fetch it or pass it.
        // For now, if we don't have it, let's keep it true.
        // Wait! Datatable calls open(data). We didn't save data.isActive.
        // Let's add a hidden input or just hardcode to true? No, hardcoding changes status!
        // I'll get it from the toggle btn in datatable or add it to hidden.
      }

      // Let's pass the raw id to the API
      const id = elements.id.value;

      // If it's update, let's just make sure we don't break isActive
      // The Backend UpdateAutomationDto requires `isActive`.
      // Since I didn't store `isActive` in the form, I'll fetch the element if needed,
      // or just set it to `true` (it's safe as a fallback, user can turn it off later).
      // Better: let's fetch the checkbox state from Datatable using DOM.
      if (isUpdate) {
        const toggle = document.querySelector(`.btn-toggle-status[data-id="${id}"]`);
        payload.isActive = toggle ? toggle.checked : true;
      }

      const [err, res] = isUpdate
        ? await updateAutomation(id, payload)
        : await createAutomation(payload);

      if (err) {
        Swal.fire(i18n.error, err.message || i18n.error, 'error');
      } else {
        Swal.fire(i18n.success, isUpdate ? i18n.updatedSuccess : i18n.createdSuccess, 'success');
        bootstrapModal?.hide();
        onRefresh();
      }
    } catch (error) {
      console.error('Submit error:', error);
      Swal.fire(i18n.error, i18n.error, 'error');
    } finally {
      elements.submitBtn.disabled = false;
      elements.submitBtn.innerHTML = originalHtml;
    }
  };

  return { init, open, submit };
})();
