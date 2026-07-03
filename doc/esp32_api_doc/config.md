# Config Module - SmartRoom Client

## Config API Documentation

---

### **PATCH** `/config` - Cập nhật JSON cấu hình thiết bị


> API này dùng để cập nhật toàn bộ JSON cấu hình thiết bị đang được firmware sử dụng trong bộ nhớ.
> 
> Dữ liệu mới sẽ được ghi đè lên cấu hình hiện tại và áp dụng lại cho các relay sau khi lưu thành công.
> 
> Request bắt buộc phải có JWT token hợp lệ trong header `Authorization`.

### Request Headers

| Tên header | Giá trị | Bắt buộc | Mô tả |
| :--------- | :------ | :------- | :---- |
| Content-Type | application/json | Có | Định dạng dữ liệu gửi lên |
| Authorization | Bearer <token> | Có | JWT token lấy từ API đăng nhập |
| Origin | string | Không | Nguồn gốc request (hỗ trợ CORS) |

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--------- | :--- | :------- | :---- |
| configDevice | array | Có | Mảng JSON cấu hình thiết bị mới cần lưu vào firmware |

### Request Example

```json
{
	"configDevice": [
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
			"specificType": "GPIO",
			"gpioPin": [13]
		},
		{
			"naturalId": "Fan_01",
			"category": "FAN",
			"translations": {
				"vi": {
					"name": "Quạt A101 1",
					"description": "Quạt số 01 của phòng lab A101"
				},
				"en": {
					"name": "Fan A101 1",
					"description": "Fan 01 of Lab A101"
				}
			},
			"peripheralType": "RELAY",
			"specificType": "GPIO",
			"gpioPin": [14, 27, 26]
		}
	]
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Cập nhật JSON cấu hình thiết bị thành công",
	"timestamp": "2026-07-03T12:31:09Z"
}
```

### Response (400 Bad Request)

> Xảy ra khi body trống, JSON không hợp lệ, hoặc thiếu trường `configDevice`.

```json
{
	"status": 400,
	"message": "Thiếu trường configDevice hoặc không hợp lệ",
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

### Lưu ý

- `configDevice` phải là một mảng JSON hợp lệ
- Sau khi cập nhật thành công, firmware sẽ lưu cấu hình mới vào bộ nhớ và khởi tạo lại các chân relay
- JWT token phải được gửi qua header `Authorization: Bearer <token>`
- Nếu body trống hoặc không có `configDevice`, server sẽ trả lỗi `400`
