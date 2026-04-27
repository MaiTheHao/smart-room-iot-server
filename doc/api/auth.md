# Auth Module

## Auth API Documentation

---

<details>
<summary><b>POST</b> <code>/api/v1/auth/signin</code> - Đăng nhập hệ thống</summary>

> Đăng nhập hệ thống, trả về JWT token và thông tin quyền.

### Request Body

| Tên trường | Loại   | Bắt buộc | Mô tả                        |
| :--------- | :----- | :------- | :--------------------------- |
| username   | string | Có       | Tên đăng nhập của client     |
| password   | string | Có       | Mật khẩu (tối thiểu 6 ký tự) |

### Request Example

```json
{
	"username": "client01",
	"password": "secret123"
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
		"type": "Bearer",
		"username": "client01",
		"groups": ["G_ADMIN", "G_USER"]
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

### Response (401 Unauthorized)

```json
{
	"status": 401,
	"message": "Invalid username or password",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

### Response (429 Too Many Requests)

> Hệ thống giới hạn số lượng request để ngăn chặn tấn công Brute-force/DDoS.

```json
{
    "status": 429,
    "message": "Too many requests. Please try again after 60 seconds.",
    "data": null,
    "timestamp": "2026-04-27T10:00:00Z"
}
```

</details>

<details>
<summary><b>POST</b> <code>/api/v1/auth/signup</code> - Đăng ký client mới</summary>

> Đăng ký client mới vào hệ thống.

### Request Body

| Tên trường | Loại         | Bắt buộc | Mô tả                                       |
| :--------- | :----------- | :------- | :------------------------------------------ |
| username   | string       | Có       | Tên đăng nhập client (3-100 ký tự)          |
| password   | string       | Có       | Mật khẩu (6-100 ký tự)                      |
| clientType | ClientTypeV1 | Có       | Loại client                                 |
| ipAddress  | string       | Không    | Địa chỉ IP (IPv4/IPv6, tối đa 45 ký tự)     |
| macAddress | string       | Không    | Địa chỉ MAC (tối đa 100 ký tự)              |
| avatarUrl  | string       | Không    | URL ảnh đại diện (tối đa 255 ký tự, hợp lệ) |

### Request Example

```json
{
	"username": "client01",
	"password": "secret123",
	"clientType": "DEVICE",
	"ipAddress": "192.168.1.10",
	"macAddress": "AA:BB:CC:DD:EE:FF",
	"avatarUrl": "https://example.com/avatar.png"
}
```

### Response (201 Created)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 1,
		"username": "client01",
		"clientType": "DEVICE",
		"ipAddress": "192.168.1.10",
		"macAddress": "AA:BB:CC:DD:EE:FF",
		"avatarUrl": "https://example.com/avatar.png",
		"lastLoginAt": null
	},
	"timestamp": "2024-06-07T09:01:00Z"
}
```

### Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Username already exists",
	"data": null,
	"timestamp": "2024-06-07T09:01:00Z"
}
```

</details>

<details>
<summary><b>POST</b> <code>/api/v1/auth/logout</code> - Đăng xuất client</summary>

> Đăng xuất client khỏi hệ thống, hủy JWT token phía backend (nếu có) và xóa session phía client.

### Request

-   Header:
    -   `Authorization: Bearer <JWT token>`

### Request Example

```http
POST /api/v1/auth/logout HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Logout successful",
	"data": null,
	"timestamp": "2024-06-07T09:05:00Z"
}
```

### Gợi ý xử lý phía client (JavaScript)

```js
async function logoutApi() {
	await fetch('/api/v1/auth/logout', {
		method: 'POST',
		headers: {
			Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
		},
	});
	localStorage.removeItem('accessToken');
	sessionStorage.clear();
	window.location.href = '/login';
}
```

</details>
