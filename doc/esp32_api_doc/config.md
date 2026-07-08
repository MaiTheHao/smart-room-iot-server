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
| roomCode | string | Có | Mã phòng |
| devices | array | Có | Mảng JSON cấu hình thiết bị mới |

### Request Example

```json
{
	"roomCode": "R-VU",
	"devices": [
		{
			"naturalId": "LABAC1",
			"category": "AIR_CONDITION",
			"controlType": "GPIO",
			"specificType": "IR_SEND",
			"gpioPin": [18],
			"translations": {
				"en": { "name": "Lab AC 1" },
				"vi": { "name": "Máy lạnh phòng lab 1" }
			},
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
					"speed": { "1": "0x880A30D", "2": "0x880A32F", "3": "0x880A341" },
					"temperature": { "16": "0x880A14F", "17": "0x880A163", "18": "0x880A177", "19": "0x880A18B", "20": "0x880A19F", "21": "0x880A1B3", "22": "0x880A1C7", "23": "0x880A1DB", "24": "0x880A1EF", "25": "0x880A203", "26": "0x880A217", "27": "0x880A22B", "28": "0x880A23F", "29": "0x880A253", "30": "0x880A267" },
					"swing": { "ON": "0x8810001" }
				}
			}
		},
		{
			"naturalId": "LIGHT_01",
			"category": "LIGHT",
			"translations": {
				"vi": { "name": "Đèn A101 1", "description": "Đèn số 01 của phòng lab A101" },
				"en": { "name": "Light A101 1", "description": "Light 01 of Lab A101" }
			},
			"specificType": "GPIO",
			"controlType": "GPIO",
			"gpioPin": [13],
			"internal": { "peripheralType": "RELAY" }
		},
		{
			"naturalId": "Fan_01",
			"category": "FAN",
			"translations": {
				"vi": { "name": "Quạt A101 1", "description": "Quạt số 01 của phòng lab A101" },
				"en": { "name": "Fan A101 1", "description": "Fan 01 of Lab A101" }
			},
			"specificType": "GPIO",
			"controlType": "GPIO",
			"gpioPin": [14, 27, 26],
			"internal": { "peripheralType": "RELAY" }
		}
	]
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Cập nhật JSON cấu hình thiết bị thành công",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Response (400 Bad Request)

> Xảy ra khi body trống, JSON không hợp lệ, hoặc thiếu trường `roomCode` hoặc `devices`.

```json
{
	"status": 400,
	"message": "Thiếu trường roomCode hoặc devices",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Response (401 Unauthorized)

> Xảy ra khi thiếu header Authorization, token không đúng định dạng, hoặc token không hợp lệ/hết hạn.

```json
{
	"status": 401,
	"message": "Token hết hạn hoặc không đúng",
	"timestamp": "2026-07-07T03:28:27Z"
}
```

### Cấu trúc Device

#### Thiết bị Relay (LIGHT, FAN)

```json
{
	"naturalId": "LIGHT_01",
	"category": "LIGHT",
	"translations": {
		"vi": { "name": "Tên tiếng Việt", "description": "Mô tả" },
		"en": { "name": "English Name", "description": "Description" }
	},
	"specificType": "GPIO",
	"controlType": "GPIO",
	"gpioPin": [13],
	"internal": {
		"peripheralType": "RELAY"
	}
}
```

#### Thiết bị IR Sender (AIR_CONDITION)

```json
{
	"naturalId": "LABAC1",
	"category": "AIR_CONDITION",
	"controlType": "GPIO",
	"specificType": "IR_SEND",
	"gpioPin": [18],
	"translations": { ... },
	"internal": {
		"peripheralType": "IR_SENDER",
		"brand": "LG",
		"codeConfigs": {
			"power": { "ON": "0x8800F43", "OFF": "0x88C0051" },
			"mode": { 
				"COOL": "0x8808F4B", "HEAT": "0x880C556",
				"DRY": "0x880910A", "FAN": "0x880A30D", "AUTO": "0x880B151"
			},
			"speed": { "1": "0x880A30D", "2": "0x880A32F", "3": "0x880A341" },
			"temperature": { "16": "0x880A14F", "17": "0x880A163", ..., "30": "0x880A267" },
			"swing": { "ON": "0x8810001" }
		}
	}
}
```

**Lưu ý về IR codes:**
- Mã IR phải là chuỗi hex, bắt đầu với `0x`
- Không cần field `bits` (mặc định LG = 28 bits, Samsung = 36, Panasonic = 48)
- Section `swing` có thể chỉ có `ON` (như LG) hoặc có cả `ON` và `OFF` tùy thiết bị
- Section `speed` thường có 3-5 levels tùy hãng và model AC

### Lưu ý

- Cấu hình phải có `roomCode` và mảng `devices`
- Mỗi device phải có `internal` object với `peripheralType`
- **Thiết bị IR_SENDER phải có:**
  - `internal.brand`: Tên hãng (LG, SAMSUNG, PANASONIC)
  - `internal.codeConfigs`: Object chứa tất cả mã IR
  - Các sections trong `codeConfigs`: `power`, `mode`, `speed`, `temperature`, `swing`
  - Mỗi mã IR phải là chuỗi hex bắt đầu với `0x`
- Sau khi cập nhật thành công, firmware sẽ lưu cấu hình mới vào bộ nhớ Preferences
- Firmware sẽ tự động re-init các relay pins khi có thay đổi config
- JWT token phải được gửi qua header `Authorization: Bearer <token>`
