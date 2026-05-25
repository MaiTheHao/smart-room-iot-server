const SwalToastMixin = typeof Swal !== 'undefined' ? Swal.mixin({
  toast: true,
  position: 'top-end',
  showConfirmButton: false,
  timer: 3000,
  timerProgressBar: true,
  didOpen: (toast) => {
    toast.addEventListener('mouseenter', Swal.stopTimer);
    toast.addEventListener('mouseleave', Swal.resumeTimer);
  }
}) : null;

export const Toast = {
  success(message) {
    SwalToastMixin?.fire({ icon: 'success', title: message });
  },
  error(message) {
    SwalToastMixin?.fire({ icon: 'error', title: message });
  },
  warning(message) {
    SwalToastMixin?.fire({ icon: 'warning', title: message });
  },
  info(message) {
    SwalToastMixin?.fire({ icon: 'info', title: message });
  }
};

export const Alert = {
  success(message, title = 'Success') {
    return Swal?.fire({ icon: 'success', title, text: message });
  },
  error(message, title = 'Error') {
    return Swal?.fire({ icon: 'error', title, text: message });
  },
  warning(message, title = 'Warning') {
    return Swal?.fire({ icon: 'warning', title, text: message });
  },
  info(message, title = 'Information') {
    return Swal?.fire({ icon: 'info', title, text: message });
  },
  async confirm(options = {}) {
    if (typeof Swal === 'undefined') {
      return { isConfirmed: confirm(options.text || '') };
    }
    return Swal.fire({
      title: options.title || 'Are you sure?',
      text: options.text || '',
      icon: options.icon || 'warning',
      showCancelButton: true,
      confirmButtonColor: options.confirmColor || '#d33',
      cancelButtonColor: options.cancelColor || '#3085d6',
      confirmButtonText: options.confirmText || 'Confirm',
      cancelButtonText: options.cancelText || 'Cancel'
    });
  }
};
