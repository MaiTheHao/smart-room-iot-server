# Client Module

## API Documentation

---

## ## Get All Clients

### GET /api/v1/clients

> Lấy danh sách tất cả client (người dùng và gateway) có phân trang.

#### Tham số Truy vấn (Query Parameters)

| Tên  | Loại | Mô tả                  | Mặc định |
| :--- | :--- | :--------------------- | :------- |
| page | int  | Trang hiện tại         | 0        |
| size | int  | Số lượng phần tử/trang | 10       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"username": "client1",
				"clientType": "USER",
				"ipAddress": "192.168.1.10",
				"macAddress": "AA:BB:CC:DD:EE:FF",
				"avatarUrl": "https://example.com/avatar1.png",
				"lastLoginAt": "2024-06-07T09:00:00Z"
			}
		],
		"page": 0,
		"size": 10,
		"totalElements": 1,
		"totalPages": 1
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## ## Get Client By ID

### GET /api/v1/clients/{id}

> Lấy thông tin chi tiết của một client theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả         | Bắt buộc |
| :-- | :--- | :------------ | :------- |
| id  | Long | ID của client | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"username": "client1",
		"clientType": "USER",
		"ipAddress": "192.168.1.10",
		"macAddress": "AA:BB:CC:DD:EE:FF",
		"avatarUrl": "https://example.com/avatar1.png",
		"lastLoginAt": "2024-06-07T09:00:00Z"
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## ## Get Clients By Room ID

### GET /api/v1/clients/room/{roomId}

> Lấy danh sách client thuộc một phòng cụ thể (phân trang).

#### Tham số Đường dẫn (Path Parameters)

| Tên    | Loại | Mô tả        | Bắt buộc |
| :----- | :--- | :----------- | :------- |
| roomId | Long | ID của phòng | Có       |

#### Tham số Truy vấn (Query Parameters)

| Tên  | Loại | Mô tả                  | Mặc định |
| :--- | :--- | :--------------------- | :------- |
| page | int  | Trang hiện tại         | 0        |
| size | int  | Số lượng phần tử/trang | 10       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 2,
				"username": "client2",
				"clientType": "HARDWARE_GATEWAY",
				"ipAddress": "192.168.1.11",
				"macAddress": "11:22:33:44:55:66",
				"avatarUrl": "https://example.com/avatar2.png",
				"lastLoginAt": "2024-06-07T09:00:00Z"
			}
		],
		"page": 0,
		"size": 10,
		"totalElements": 1,
		"totalPages": 1
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## ## Create Client

### POST /api/v1/clients

> Tạo mới một client (người dùng hoặc gateway).

#### Request Body Fields

| Tên trường | Loại         | Bắt buộc | Mô tả                                    |
| :--------- | :----------- | :------- | :--------------------------------------- |
| username   | string       | Có       | Tên đăng nhập (3-100 ký tự, duy nhất)    |
| password   | string       | Có       | Mật khẩu (6-100 ký tự)                   |
| clientType | ClientTypeV1 | Có       | Loại client (`USER`, `HARDWARE_GATEWAY`) |
| ipAddress  | string       | Không    | Địa chỉ IP (IPv4/IPv6, tối đa 45 ký tự)  |
| macAddress | string       | Không    | Địa chỉ MAC (tối đa 100 ký tự)           |
| avatarUrl  | string (URL) | Không    | Đường dẫn avatar (tối đa 255 ký tự)      |

#### Ví dụ Request Body

```json
{
	"username": "newclient",
	"password": "secret123",
	"clientType": "USER",
	"ipAddress": "192.168.1.12",
	"macAddress": "22:33:44:55:66:77",
	"avatarUrl": "https://example.com/avatar3.png"
}
```

#### Ví dụ Response (201 Created)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 3,
		"username": "newclient",
		"clientType": "USER",
		"ipAddress": "192.168.1.12",
		"macAddress": "22:33:44:55:66:77",
		"avatarUrl": "https://example.com/avatar3.png",
		"lastLoginAt": null
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## ## Update Client

### PUT /api/v1/clients/{id}

> Cập nhật thông tin một client theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả         | Bắt buộc |
| :-- | :--- | :------------ | :------- |
| id  | Long | ID của client | Có       |

#### Request Body Fields

| Tên trường | Loại         | Bắt buộc | Mô tả                                    |
| :--------- | :----------- | :------- | :--------------------------------------- |
| username   | string       | Không    | Tên đăng nhập (3-100 ký tự, duy nhất)    |
| password   | string       | Không    | Mật khẩu (6-100 ký tự)                   |
| clientType | ClientTypeV1 | Không    | Loại client (`USER`, `HARDWARE_GATEWAY`) |
| ipAddress  | string       | Không    | Địa chỉ IP (IPv4/IPv6, tối đa 45 ký tự)  |
| macAddress | string       | Không    | Địa chỉ MAC (tối đa 100 ký tự)           |
| avatarUrl  | string (URL) | Không    | Đường dẫn avatar (tối đa 255 ký tự)      |

#### Ví dụ Request Body

```json
{
	"username": "updatedclient",
	"clientType": "HARDWARE_GATEWAY",
	"avatarUrl": "https://example.com/avatar4.png"
}
```

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 3,
		"username": "updatedclient",
		"clientType": "HARDWARE_GATEWAY",
		"ipAddress": "192.168.1.12",
		"macAddress": "22:33:44:55:66:77",
		"avatarUrl": "https://example.com/avatar4.png",
		"lastLoginAt": "2024-06-07T09:00:00Z"
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## ## Delete Client

### DELETE /api/v1/clients/{id}

> Xóa một client theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả         | Bắt buộc |
| :-- | :--- | :------------ | :------- |
| id  | Long | ID của client | Có       |

#### Ví dụ Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## ## Kiểu dữ liệu ClientTypeV1

| Giá trị          | Mô tả                   |
| :--------------- | :---------------------- |
| USER             | Người dùng thông thường |
| HARDWARE_GATEWAY | Gateway phần cứng       |

---
