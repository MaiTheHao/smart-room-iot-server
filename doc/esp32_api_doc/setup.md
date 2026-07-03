# Setup Module - SmartRoom Client

## Setup API Documentation

---

### **GET** `/setup` - Lấy JSON cấu hình thiết bị


> API này dùng để lấy toàn bộ JSON cấu hình thiết bị hiện đang được firmware nạp trong bộ nhớ.
> 
> Request bắt buộc phải có JWT token hợp lệ trong header `Authorization`.

### Request Headers

| Tên header | Giá trị | Bắt buộc | Mô tả |
| :--------- | :------ | :------- | :---- |
| Authorization | Bearer <token> | Có | JWT token lấy từ API đăng nhập |
| Origin | string | Không | Nguồn gốc request (hỗ trợ CORS) |

### Request Body

> Không có request body.

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Lấy JSON cấu hình thiết bị thành công",
	"data": {
		"roomCode": "A101",
		"devices": [
			{
				"naturalId": "LIGHT_01",
				"category": "LIGHTING",
				"translations": {
					"vi": {
						"name": "Đèn A101 1",
						"description": "Đèn số 01 của phòng lab A101"
					},
					"en": {
						"name": "Light A101 1",
						"description": "Light 01 of Lab A101"
					}
				},
				"peripheralType": "RELAY",
				"controlType": "GPIO",
				"specificType": "GPIO",
				"gpioPin": [13]
			}
		]
	},
	"timestamp": "2026-07-03T12:31:09Z"
}
```

### Response (401 Unauthorized)

> Xảy ra khi thiếu header Authorization, token không đúng định dạng, hoặc token không hợp lệ/hết hạn.

```json
{
	"status": 401,
	"message": "Token hết hạn hoặc không đúng",
	"timestamp": "2026-07-03T12:31:09Z"
}
```

### Response (500 Internal Server Error)

> Endpoint này không chủ động trả 500 trong firmware hiện tại, nhưng dữ liệu cấu hình phụ thuộc vào JSON đang được nạp trong thiết bị.

### Lưu ý

- Dữ liệu trả về là toàn bộ mảng `configDevice` đang được firmware sử dụng
- `naturalId` và `category` trong dữ liệu này phải khớp với cấu hình điều khiển thực tế
- JWT token phải được gửi qua header `Authorization: Bearer <token>`
- Nếu không có header Authorization hoặc token sai, server sẽ trả lỗi `401`
