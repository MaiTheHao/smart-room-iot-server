# Auth Module

## Auth API Documentation

---

### POST /api/v1/auth/signin

> Đăng nhập hệ thống, trả về JWT token và thông tin quyền.

#### Request Body Fields

| Tên trường | Loại   | Bắt buộc | Mô tả                        |
| :--------- | :----- | :------- | :--------------------------- |
| username   | string | Có       | Tên đăng nhập của client     |
| password   | string | Có       | Mật khẩu (tối thiểu 6 ký tự) |

#### Ví dụ Request Body

```json
{
	"username": "client01",
	"password": "secret123"
}
```

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
		"type": "Bearer",
		"username": "client01",
		"roles": ["ROLE_CLIENT"]
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

#### Ví dụ Response (401 Unauthorized)

```json
{
	"status": 401,
	"message": "Invalid username or password",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/auth/signup

> Đăng ký client mới vào hệ thống.

#### Request Body Fields

| Tên trường | Loại         | Bắt buộc | Mô tả                                       |
| :--------- | :----------- | :------- | :------------------------------------------ |
| username   | string       | Có       | Tên đăng nhập client (3-100 ký tự)          |
| password   | string       | Có       | Mật khẩu (6-100 ký tự)                      |
| clientType | ClientTypeV1 | Có       | Loại client                                 |
| ipAddress  | string       | Không    | Địa chỉ IP (IPv4/IPv6, tối đa 45 ký tự)     |
| macAddress | string       | Không    | Địa chỉ MAC (tối đa 100 ký tự)              |
| avatarUrl  | string       | Không    | URL ảnh đại diện (tối đa 255 ký tự, hợp lệ) |

#### Ví dụ Request Body

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

#### Ví dụ Response (201 Created)

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

#### Ví dụ Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Username already exists",
	"data": null,
	"timestamp": "2024-06-07T09:01:00Z"
}
```

---
