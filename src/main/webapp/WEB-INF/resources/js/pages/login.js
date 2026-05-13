(function () {
  'use strict';

  const i18n = {
    loadingText: /*[[#{login.loading}]]*/ 'Processing...',
    errorTitle: /*[[#{login.error.title}]]*/ 'Login Failed',
  };

  function Login() {
    function bindEvents() {
      const loginForm = document.getElementById('loginForm');
      if (loginForm) {
        loginForm.addEventListener('submit', function () {
          const submitBtn = this.querySelector('button[type="submit"]');
          if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = `
              <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              ${i18n.loadingText}
            `;
          }
        });
      }
    }

    function checkErrors() {
      const error = window.INITIAL_ERROR;
      if (error) {
        Swal.fire({
          icon: 'error',
          title: i18n.errorTitle,
          text: error,
          confirmButtonColor: '#0d6efd',
        });
      }
    }

    function init() {
      if (window.lucide) {
        lucide.createIcons();
      }
      checkErrors();
      bindEvents();
    }

    return {
      init,
    };
  }

  document.addEventListener('DOMContentLoaded', () => {
    const loginPage = Login();
    loginPage.init();
  });
})();
