# Temperature Module - SmartRoom Client

## Temperature API Documentation

---

### **POST** `/temperature` - Lấy dữ liệu nhiệt độ từ cảm biến

> API này dùng để lấy dữ liệu nhiệt độ hiện tại từ cảm biến được chỉ định.
> 
> Hiện tại chỉ hỗ trợ cảm biến DS18B20 sử dụng giao tiếp 1-Wire.
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
| naturalId | string | Có | Định danh cảm biến nhiệt độ |

### Request Example

```json
{
	"naturalId": "TEMP_ESP32_01"
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Lấy nhiệt độ thành công",
	"data": {
		"tempC": "25.500"
	},
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Response Fields

| Tên trường | Loại | Mô tả |
| :--------- | :--- | :---- |
| tempC | string | Giá trị nhiệt độ theo độ Celsius (3 chữ số thập phân) |

### Lỗi chung

#### Response (400 Bad Request)

> Xảy ra khi body trống hoặc thiếu trường `naturalId`.

```json
{
	"status": 400,
	"message": "Body bắt buộc phải có trường: naturalId",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

#### Response (401 Unauthorized)

> Xảy ra khi thiếu header `Authorization`, token không đúng định dạng, hoặc token không hợp lệ/hết hạn.

```json
{
	"status": 401,
	"message": "Token hết hạn hoặc không đúng",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

#### Response (404 Not Found)

> Xảy ra khi không tìm thấy cảm biến có `naturalId` tương ứng trong config.

```json
{
	"status": 404,
	"message": "Không tìm thấy cảm biến nhiệt độ có naturalId tương ứng",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

#### Response (500 Internal Server Error)

> Xảy ra khi:
> - Cảm biến thiếu thông tin cấu hình (module, GPIO pin)
> - Không thể đọc dữ liệu từ cảm biến (cảm biến không phản hồi)
> - Lỗi khi parse JSON cấu hình

```json
{
	"status": 500,
	"message": "Lỗi: Không thể đọc dữ liệu từ cảm biến",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

#### Response (501 Not Implemented)

> Xảy ra khi module cảm biến chưa được hỗ trợ (không phải DS18B20).

```json
{
	"status": 501,
	"message": "Module cảm biến chưa được hỗ trợ",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

---
