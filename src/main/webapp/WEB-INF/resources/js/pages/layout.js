import { authService } from '../services/auth.service.js';
import { storage } from '../localstorage.js';

document.addEventListener('DOMContentLoaded', () => {
	if (window.renderIcons) {
		window.renderIcons();
	}

	const fetchUserInfo = async () => {
		const [err, result] = await authService.getMe();

		if (!err && result.status === 200) {
			renderUserUI(result.data);
		} else {
			if (err) console.error('Failed to fetch user info:', err);
			const savedUser = storage.getJson('user');
			if (savedUser) {
				renderUserUI(savedUser);
			}
		}
	};

	const renderUserUI = (user) => {
		const userNameEls = document.querySelectorAll('#userName, #userFullName');
		const avatarEls = document.querySelectorAll('#userAvatar, #userAvatarLarge');
		const rolesEl = document.getElementById('userRoles');

		userNameEls.forEach((el) => (el.textContent = user.username));

		if (user.avatarUrl) {
			avatarEls.forEach((el) => (el.src = user.avatarUrl));
		} else {
			const avatarUrl = `https://ui-avatars.com/api/?name=${user.username}&background=random`;
			avatarEls.forEach((el) => (el.src = avatarUrl));
		}

		if (rolesEl) {
			const roles = storage.getJson('roles') || [];
			rolesEl.textContent = roles.length > 0 ? `Roles: ${roles.join(', ')}` : '';
		}
	};

	fetchUserInfo();

	const logoutBtn = document.getElementById('logoutBtn');
	if (logoutBtn) {
		logoutBtn.addEventListener('click', async (e) => {
			e.preventDefault();

			const confirmResult = await Swal.fire({
				title: 'Đăng xuất?',
				text: 'Bạn có chắc chắn muốn thoát khỏi hệ thống?',
				icon: 'warning',
				showCancelButton: true,
				confirmButtonColor: '#0d6efd',
				cancelButtonColor: '#6c757d',
				confirmButtonText: 'Đăng xuất',
				cancelButtonText: 'Hủy',
			});

			if (confirmResult.isConfirmed) {
				storage.removeMany(['username', 'roles', 'user']);
				window.location.href = '/logout';
			}
		});
	}
});
