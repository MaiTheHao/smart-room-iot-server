# Control Module - SmartRoom Client

## Control API Documentation

---

### **POST** `/control` - Điều khiển thiết bị theo `naturalId` và `category`


> API này dùng để điều khiển các thiết bị đang được hỗ trợ trong firmware hiện tại. 
> 
> Hiện tại chỉ hỗ trợ 2 nhóm: `LIGHTING` và `FAN`.
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
| naturalId | string | Có | Định danh thiết bị cần điều khiển |
| category | string | Có | Loại thiết bị. Hiện tại chỉ nhận `LIGHTING` hoặc `FAN` |
| power | string | Không | Trạng thái điều khiển. Nếu có thì với `LIGHTING` nhận `ON` / `OFF`, với `FAN` nhận `ON` / `OFF` |
| speed | number | Không | Chỉ dùng cho `FAN` khi cần chọn mức quạt. Giá trị hợp lệ: 1, 2, 3 |

### Request Example - LIGHTING

```json
{
	"naturalId": "LIGHT_01",
	"category": "LIGHTING",
	"power": "ON"
}
```

### Request Example - FAN

```json
{
	"naturalId": "Fan_01",
	"category": "FAN",
	"speed": 2
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Điều khiển thành công",
	"timestamp": "2026-07-03T12:31:09Z"
}
```

### Response (200 OK - Không có thay đổi)

```json
{
	"status": 200,
	"message": "Không có cấu hình nào được thay đổi",
	"timestamp": "2026-07-03T12:31:09Z"
}
```

### Response (400 Bad Request)

> Xảy ra khi body thiếu trường bắt buộc, định dạng dữ liệu sai, hoặc giá trị không hợp lệ.

```json
{
	"status": 400,
	"message": "Body bắt buộc phải có các trường: naturalId, category",
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

### Response (404 Not Found)

> Xảy ra khi `naturalId` không khớp với bất kỳ thiết bị nào trong cấu hình hiện tại.

```json
{
	"status": 404,
	"message": "Không tìm thấy thiết bị có naturalId tương ứng",
	"timestamp": "2026-07-03T12:31:09Z"
}
```

### Response (500 Internal Server Error)

> Xảy ra khi firmware không parse được JSON cấu hình thiết bị.

```json
{
	"status": 500,
	"message": "Lỗi thiết bị khi parse JSON",
	"timestamp": "2026-07-03T12:31:09Z"
}
```

### Hỗ trợ theo category

#### LIGHTING

- `power` là tùy chọn, nếu gửi thì phải có giá trị `ON` hoặc `OFF`
- Thiết bị được map theo `naturalId`
- Firmware điều khiển relay ở chân GPIO đầu tiên của thiết bị

#### FAN

- `power` là tùy chọn, nếu gửi thì phải có giá trị `ON` hoặc `OFF`
- `speed` là tùy chọn, nếu gửi thì chỉ nhận giá trị `1`, `2`, `3`
- Firmware sẽ tắt toàn bộ chân quạt trước khi bật chân tương ứng với tốc độ hoặc mức GPIO tương ứng

### Lưu ý

- API này chỉ hỗ trợ đúng 2 category đang có trong firmware: `LIGHTING` và `FAN`
- `naturalId` phải trùng với cấu hình thiết bị đã nạp trong firmware
- JWT token phải được gửi qua header `Authorization: Bearer <token>`
- Nếu body trống hoặc JSON không hợp lệ, server sẽ trả lỗi `400`
- Nếu không gửi `power`, firmware sẽ chỉ dựa vào `naturalId`, `category`, và `speed` để xử lý theo luồng hiện có


