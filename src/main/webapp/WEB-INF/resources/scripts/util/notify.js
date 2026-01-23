const Toast = Swal.mixin({
	toast: true,
	position: 'top-end',
	showConfirmButton: false,
	timer: 3000,
	timerProgressBar: true,
	didOpen: (toast) => {
		toast.addEventListener('mouseenter', Swal.stopTimer);
		toast.addEventListener('mouseleave', Swal.resumeTimer);
	},
});

window.notify = {
	success: function (message, title = 'Success!') {
		Toast.fire({
			icon: 'success',
			title: title,
			text: message,
		});
	},

	error: function (message, title = 'Error!') {
		Toast.fire({
			icon: 'error',
			title: title,
			text: message,
		});
	},

	warning: function (message, title = 'Warning!') {
		Toast.fire({
			icon: 'warning',
			title: title,
			text: message,
		});
	},

	info: function (message, title = 'Info') {
		Toast.fire({
			icon: 'info',
			title: title,
			text: message,
		});
	},

	confirm: async function (title = 'Are you sure?', message = '', icon = 'warning') {
		const result = await Swal.fire({
			title: title,
			text: message,
			icon: icon,
			showCancelButton: true,
			confirmButtonColor: '#3085d6',
			cancelButtonColor: '#d33',
			confirmButtonText: 'Yes, do it!',
			cancelButtonText: 'Cancel',
		});
		return result.isConfirmed;
	},

	confirmDelete: async function (itemName) {
		const result = await Swal.fire({
			title: 'Delete Confirmation',
			html: `Are you sure you want to delete <strong>${itemName}</strong>?<br><small class="text-danger">This action cannot be undone.</small>`,
			icon: 'warning',
			showCancelButton: true,
			confirmButtonColor: '#d33',
			cancelButtonColor: '#6c757d',
			confirmButtonText: '<i class="fas fa-trash mr-1"></i> Yes, delete it!',
			cancelButtonText: 'Cancel',
		});
		return result.isConfirmed;
	},

	prompt: async function (title, label, inputType = 'text', placeholder = '') {
		const result = await Swal.fire({
			title: title,
			html: `<label class="text-left d-block mb-2">${label}</label>`,
			input: inputType,
			inputPlaceholder: placeholder,
			showCancelButton: true,
			confirmButtonText: 'Submit',
			cancelButtonText: 'Cancel',
			inputValidator: (value) => {
				if (!value) {
					return 'This field is required!';
				}
			},
		});
		return result.isConfirmed ? result.value : null;
	},

	loading: function (message = 'Loading...') {
		Swal.fire({
			title: message,
			allowOutsideClick: false,
			didOpen: () => {
				Swal.showLoading();
			},
		});
	},

	closeLoading: function () {
		Swal.close();
	},
};

window.Notify = window.notify;
