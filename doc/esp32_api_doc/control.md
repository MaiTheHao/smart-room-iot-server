# Control Module - SmartRoom Client

## Control API Documentation

---

### **POST** `/control` - Điều khiển thiết bị theo `naturalId` và `category`

> API này dùng để điều khiển các thiết bị đang được hỗ trợ trong firmware hiện tại.
> 
> Hiện tại hỗ trợ 3 loại: `LIGHT`, `FAN`, và `AIR_CONDITION`.
> 
> Request bắt buộc phải có JWT token hợp lệ trong header `Authorization`.

### Request Headers

| Tên header | Giá trị | Bắt buộc | Mô tả |
| :--------- | :------ | :------- | :---- |
| Content-Type | application/json | Có | Định dạng dữ liệu gửi lên |
| Authorization | Bearer <token> | Có | JWT token lấy từ API đăng nhập |
| Origin | string | Không | Nguồn gốc request (hỗ trợ CORS) |

### Request Body - Chung

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--------- | :--- | :------- | :---- |
| naturalId | string | Có | Định danh thiết bị cần điều khiển |
| category | string | Có | Loại thiết bị: `LIGHT`, `FAN`, `AIR_CONDITION` |

---

## LIGHT - Điều khiển đèn

### Request Example

```json
{
	"naturalId": "LIGHT_01",
	"category": "LIGHT",
	"power": "ON"
}
```

### Tham số LIGHT

| Tên trường | Loại | Bắt buộc | Giá trị | Mô tả |
| :--------- | :--- | :------- | :------ | :---- |
| power | string | Không | `ON`, `OFF` | Bật/tắt đèn |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Điều khiển thành công",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

---

## FAN - Điều khiển quạt

### Request Example - Bằng tốc độ

```json
{
	"naturalId": "Fan_01",
	"category": "FAN",
	"speed": 2
}
```

### Request Example - Bằng trạng thái

```json
{
	"naturalId": "Fan_01",
	"category": "FAN",
	"power": "ON"
}
```

### Tham số FAN

| Tên trường | Loại | Bắt buộc | Giá trị | Mô tả |
| :--------- | :--- | :------- | :------ | :---- |
| power | string | Không | `ON`, `OFF` | Bật/tắt quạt (bật tốc độ 1 nếu ON) |
| speed | number | Không | `1`, `2`, `3` | Chọn mức quạt |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Điều khiển thành công",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

---

## AIR_CONDITION - Điều khiển điều hòa

> Điều khiển AC thông qua gửi mã IR dựa trên cấu hình `internal.codeConfigs` trong device config.
> 
> Hỗ trợ các hãng: `LG`, `SAMSUNG`, `PANASONIC`. Có thể mở rộng bằng cách thêm hãng mới vào `BrandIrSender` registry.

### Request Example - Bật AC

```json
{
	"naturalId": "LABAC1",
	"category": "AIR_CONDITION",
	"power": "ON"
}
```

### Request Example - Tắt AC

```json
{
	"naturalId": "LABAC1",
	"category": "AIR_CONDITION",
	"power": "OFF"
}
```

### Request Example - Đặt chế độ

```json
{
	"naturalId": "LABAC1",
	"category": "AIR_CONDITION",
	"mode": "COOL"
}
```

### Request Example - Đặt nhiệt độ

```json
{
	"naturalId": "LABAC1",
	"category": "AIR_CONDITION",
	"temperature": 24
}
```

### Request Example - Đặt tốc độ quạt

```json
{
	"naturalId": "LABAC1",
	"category": "AIR_CONDITION",
	"fanSpeed": 2
}
```

### Request Example - Bật swing

```json
{
	"naturalId": "LABAC1",
	"category": "AIR_CONDITION",
	"swing": "ON"
}
```

### Request Example - Kết hợp nhiều tham số

```json
{
	"naturalId": "LABAC1",
	"category": "AIR_CONDITION",
	"power": "ON",
	"temperature": 24,
	"mode": "COOL",
	"fanSpeed": 2
}
```

### Tham số AIR_CONDITION

| Tên trường | Loại | Bắt buộc | Giá trị | Mô tả |
| :--------- | :--- | :------- | :------ | :---- |
| power | string | Không | `ON`, `OFF` | Bật/tắt AC |
| mode | string | Không | `COOL`, `HEAT`, `DRY`, `FAN`, `AUTO` | Chế độ AC |
| temperature | number | Không | `16-30` | Nhiệt độ (độ C) |
| speed hoặc fanSpeed | number | Không | `1-3` | Tốc độ quạt AC (tùy cấu hình, LG hỗ trợ 1-3) |
| swing | string | Không | `ON`, `OFF` | Bật/tắt lưới tượng (tùy cấu hình, LG chỉ có ON) |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Điều khiển AC thành công",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Response (200 OK - Không có thay đổi)

```json
{
	"status": 200,
	"message": "Không có cấu hình nào được thay đổi",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

---

## Lỗi chung

### Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Body bắt buộc phải có các trường: naturalId, category",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Response (401 Unauthorized)

```json
{
	"status": 401,
	"message": "Token hết hạn hoặc không đúng",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Response (404 Not Found)

```json
{
	"status": 404,
	"message": "Không tìm thấy thiết bị có naturalId tương ứng",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Response (500 Internal Server Error)

```json
{
	"status": 500,
	"message": "Lỗi đọc cấu hình AC",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

---

## Ghi chú

- Mỗi category có tham số riêng, tham số không phù hợp sẽ bị bỏ qua
- JWT token phải được gửi qua header `Authorization: Bearer <token>`
- Nếu gửi nhiều tham số, firmware sẽ xử lý theo thứ tự: power → temperature → mode → speed/fanSpeed → swing
- Giữa mỗi lệnh IR có độ trễ 100ms để tránh xung đột
- Hỗ trợ mở rộng AC brand bằng cách thêm hàm gửi IR và đăng ký vào `BrandIrSender` registry
- Số lượng speed levels và có/không có swing OFF phụ thuộc vào cấu hình `codeConfigs` của từng device
- Có thể dùng `speed` hoặc `fanSpeed` (cả 2 đều được hỗ trợ)

---

## Cấu trúc IR Code Config

Mỗi thiết bị AC cần có `internal.codeConfigs` trong device config:

```json
{
	"internal": {
		"peripheralType": "IR_SENDER",
		"brand": "LG",
		"codeConfigs": {
			"power": { "ON": "0x8800F43", "OFF": "0x88C0051" },
			"mode": { 
				"COOL": "0x8808F4B",
				"HEAT": "0x880C556",
				"DRY": "0x880910A",
				"FAN": "0x880A30D",
				"AUTO": "0x880B151"
			},
			"speed": { 
				"1": "0x880A30D",
				"2": "0x880A32F",
				"3": "0x880A341"
			},
			"temperature": { 
				"16": "0x880A14F",
				"17": "0x880A163",
				...
				"30": "0x880A267"
			},
			"swing": { "ON": "0x8810001" }
		}
	}
}
```

**Lưu ý về cấu trúc IR codes:**
- Mã IR phải là chuỗi hex bắt đầu với `0x`
- Số bit (bits) mặc định là 28 cho LG, có thể thêm field `"bits": 28` vào `codeConfigs` nếu cần
- Các hãng khác nhau có số bit khác nhau (LG: 28, Samsung: 36, Panasonic: 48)
- Nếu thiếu một key (ví dụ không có `swing.OFF`), firmware sẽ báo lỗi khi cố gửi lệnh đó


