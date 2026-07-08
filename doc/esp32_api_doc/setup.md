# Setup Module - SmartRoom Client

## Setup API Documentation

---

### **GET** `/setup` - Lấy JSON cấu hình thiết bị hiện tại

> API này dùng để lấy toàn bộ JSON cấu hình thiết bị đang được firmware sử dụng.
> 
> Request bắt buộc phải có JWT token hợp lệ trong header `Authorization`.

### Request Headers

| Tên header | Giá trị | Bắt buộc | Mô tả |
| :--------- | :------ | :------- | :---- |
| Authorization | Bearer <token> | Có | JWT token lấy từ API đăng nhập |
| Origin | string | Không | Nguồn gốc request (hỗ trợ CORS) |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Lấy JSON cấu hình thiết bị thành công",
	"data": {
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
	},
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
