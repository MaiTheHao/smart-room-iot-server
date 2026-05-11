document.addEventListener('DOMContentLoaded', () => {
	initDashboard();
});

function initDashboard() {
	if (window.renderIcons) window.renderIcons();
	initSceneButtons();
}

function initSceneButtons() {
	const buttons = document.querySelectorAll('.scene-btn');

	buttons.forEach((btn) => {
		btn.addEventListener('click', async (e) => {
			const scene = btn.getAttribute('data-scene');
			const sceneName = btn.querySelector('span').textContent;

			btn.classList.add('active');
			setTimeout(() => btn.classList.remove('active'), 200);

			const result = await Swal.fire({
				title: `Activate ${sceneName}?`,
				text: `Are you sure you want to trigger the "${sceneName}" scene?`,
				icon: 'question',
				showCancelButton: true,
				confirmButtonColor: '#0d6efd',
				cancelButtonColor: '#6c757d',
				confirmButtonText: 'Yes, activate!',
				showLoaderOnConfirm: true,
				preConfirm: () => {
					return new Promise((resolve) => {
						setTimeout(() => {
							resolve(true);
						}, 1000);
					});
				},
				allowOutsideClick: () => !Swal.isLoading(),
			});

			if (result.isConfirmed) {
				Swal.fire({
					title: 'Activated!',
					text: `Scene "${sceneName}" has been successfully triggered.`,
					icon: 'success',
					timer: 2000,
					showConfirmButton: false,
				});
			}
		});
	});
}
