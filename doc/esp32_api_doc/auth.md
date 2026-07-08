# Auth Module - SmartRoom Client

## Auth API Documentation

---

### **POST** `/auth/login` - Đăng nhập vào thiết bị SmartRoom

> Đăng nhập vào thiết bị SmartRoom để lấy JWT token. Token này được sử dụng để xác thực cho các request điều khiển thiết bị sau này.

### Request Headers

| Tên header | Giá trị | Bắt buộc | Mô tả |
| :--------- | :------ | :------- | :---- |
| Content-Type | application/json | Có | Định dạng dữ liệu gửi lên |
| Origin | string | Không | Nguồn gốc request (hỗ trợ CORS) |

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--------- | :--- | :------- | :---- |
| username | string | Có | Tên đăng nhập |
| password | string | Có | Mật khẩu đăng nhập |

### Request Example

```json
{
	"username": "vuesp",
	"password": "123456789"
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Đăng nhập thành công",
	"data": {
		"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InZ1ZXNwIiwicm9sZSI6InN1cGVyX3VzZXIiLCJleHAiOjE3NDg3NjU0MzJ9.signature"
	},
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Response (401 Unauthorized)

```json
{
	"status": 401,
	"message": "username hoặc password không đúng",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Response (500 Internal Server Error)

> Xảy ra khi thiết bị chưa đồng bộ thời gian thành công từ NTP server.

```json
{
	"status": 500,
	"message": "Server chưa đồng bộ xong thời gian, hãy thử lại sau vài giây!",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Thiếu body request hoặc body không đúng định dạng",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Thông tin JWT Token

Sau khi đăng nhập thành công, server trả về JWT token với các thông tin sau:

| Trường | Loại | Mô tả |
| :----- | :--- | :---- |
| username | string | Tên đăng nhập của client |
| role | string | Vai trò (mặc định: `super_user`) |
| exp | long | Thời gian hết hạn (epoch time), token có hiệu lực trong 1 giờ |

### Lưu ý

- Token được sử dụng trong header `Authorization: Bearer <token>` cho các API điều khiển thiết bị
- Token có thời gian sống là 1 giờ kể từ lúc phát hành
- Username/password mặc định là `vuesp` / `123456789` (có thể thay đổi trong code)
