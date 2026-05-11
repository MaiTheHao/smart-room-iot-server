import { authService } from '../services/auth.service.js';

document.addEventListener('DOMContentLoaded', () => {
	if (typeof lucide !== 'undefined') {
		lucide.createIcons();
	}

	const loginForm = document.getElementById('loginForm');
	if (loginForm) {
		loginForm.addEventListener('submit', async (e) => {
			e.preventDefault();
			const form = e.target;
			const submitBtn = form.querySelector('button[type="submit"]');
			if (!submitBtn) return;

			const originalBtnText = submitBtn.innerHTML;

			try {
				submitBtn.disabled = true;
				submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Đang xử lý...';

				const credentials = {
					username: form.username.value,
					password: form.password.value,
				};

				const [err, result] = await authService.login(credentials);

				if (!err && result.status === 200) {
					Swal.fire({
						icon: 'success',
						title: 'Đăng nhập thành công',
						text: 'Đang chuyển hướng...',
						timer: 1500,
						showConfirmButton: false,
						timerProgressBar: true,
					}).then(() => {
						window.location.href = '/';
					});
				} else {
					const errorMsg = err?.message || result?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.';
					Swal.fire({
						icon: 'error',
						title: 'Lỗi đăng nhập',
						text: errorMsg,
						confirmButtonColor: '#0d6efd',
					});
				}
			} catch (error) {
				console.error('Login process error:', error);
			} finally {
				submitBtn.disabled = false;
				submitBtn.innerHTML = originalBtnText;
			}
		});
	}
});
