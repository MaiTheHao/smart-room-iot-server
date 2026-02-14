# Rule Module

## Rule API Documentation

Quy tắc (Rule) là cơ chế để tự động điều khiển các thiết bị dựa trên các điều kiện được định nghĩa. Mỗi quy tắc có:
- **Điều kiện (Conditions)**: Các tiêu chí cần được đáp ứng (ví dụ: nhiệt độ > 30°C)
- **Hành động (Action)**: Lệnh được thực thi khi điều kiện được đáp ứng (ví dụ: bật điều hòa)
- **Độ ưu tiên (Priority)**: Được sử dụng để chọn quy tắc khi có nhiều quy tắc thỏa mãn

---

### GET /api/v1/rules

> Lấy danh sách tất cả các quy tắc.

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"name": "Bật điều hòa khi nóng",
			"priority": 10,
			"isActive": true,
			"roomId": 5,
			"targetDeviceId": 101,
			"targetDeviceCategory": "AIR_CONDITION",
			"actionParams": "{\"temperature\": 22, \"mode\": \"cool\"}",
			"conditions": [
				{
					"id": 1,
					"sortOrder": 0,
					"dataSource": "DEVICE",
					"resourceParam": "{\"deviceId\": 50, \"property\": \"temperature\"}",
					"operator": ">",
					"value": "30",
					"nextLogic": "AND",
					"createdAt": "2024-06-07T09:00:00Z",
					"updatedAt": "2024-06-07T09:00:00Z"
				}
			],
			"createdAt": "2024-06-07T09:00:00Z",
			"updatedAt": "2024-06-07T09:00:00Z"
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### GET /api/v1/rules/{id}

> Lấy thông tin chi tiết của một quy tắc theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                | Bắt buộc |
| :-- | :--- | :------------------- | :------- |
| id  | Long | ID của quy tắc cần lấy | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Bật điều hòa khi nóng",
		"priority": 10,
		"isActive": true,
		"roomId": 5,
		"targetDeviceId": 101,
		"targetDeviceCategory": "AIR_CONDITION",
		"actionParams": "{\"temperature\": 22, \"mode\": \"cool\"}",
		"conditions": [
			{
				"id": 1,
				"sortOrder": 0,
				"dataSource": "DEVICE",
				"resourceParam": "{\"deviceId\": 50, \"property\": \"temperature\"}",
				"operator": ">",
				"value": "30",
				"nextLogic": "AND",
				"createdAt": "2024-06-07T09:00:00Z",
				"updatedAt": "2024-06-07T09:00:00Z"
			},
			{
				"id": 2,
				"sortOrder": 1,
				"dataSource": "DEVICE",
				"resourceParam": "{\"deviceId\": 101, \"property\": \"status\"}",
				"operator": "==",
				"value": "OFF",
				"nextLogic": null,
				"createdAt": "2024-06-07T09:00:00Z",
				"updatedAt": "2024-06-07T09:00:00Z"
			}
		],
		"createdAt": "2024-06-07T09:00:00Z",
		"updatedAt": "2024-06-07T09:00:00Z"
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

#### Lỗi Có thể xảy ra

```json
{
	"status": 404,
	"message": "Not Found",
	"errors": ["Rule not found"],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/rules

> Tạo mới một quy tắc điều khiển thiết bị.

#### Request Body Fields

| Tên trường            | Loại   | Bắt buộc | Mô tả                                           |
| :-------------------- | :----- | :------- | :---------------------------------------------- |
| name                  | string | Có       | Tên quy tắc (không rỗng)                       |
| priority              | int    | Có       | Độ ưu tiên (>= 0, cao hơn = ưu tiên hơn)       |
| roomId                | Long   | Có       | ID của phòng (dùng để xác định ngữ cảnh dữ liệu) |
| targetDeviceId        | Long   | Có       | ID của thiết bị mục tiêu                        |
| targetDeviceCategory  | string | Có       | Loại thiết bị (ví dụ: AIR_CONDITION, LIGHT)    |
| actionParams          | string | Không    | Tham số hành động (JSON format)                |
| conditions            | array  | Có       | Danh sách điều kiện (ít nhất 1 điều kiện)      |

#### Request Body Conditions Fields

| Tên trường    | Loại   | Bắt buộc | Mô tả                                              |
| :------------ | :----- | :------- | :------------------------------------------------- |
| sortOrder     | int    | Có       | Thứ tự đánh giá (>= 0)                             |
| dataSource    | enum   | Có       | Nguồn dữ liệu: DEVICE, AUTOMATION, CONSTANT, RULE |
| resourceParam | string | Có       | Tham số tài nguyên (JSON format)                   |
| operator      | string | Có       | Toán tử so sánh: ==, !=, >, <, >=, <=, IN, etc    |
| value         | string | Có       | Giá trị so sánh                                    |
| nextLogic     | string | Không    | Logic cho điều kiện tiếp theo: AND, OR (Mặc định: AND) |

#### Ví dụ Request Body

```json
{
	"name": "Bật điều hòa khi nóng",
	"priority": 10,
	"roomId": 5,
	"targetDeviceId": 101,
	"targetDeviceCategory": "AIR_CONDITION",
	"actionParams": "{\"temperature\": 22, \"mode\": \"cool\"}",
	"conditions": [
		{
			"sortOrder": 0,
			"dataSource": "DEVICE",
			"resourceParam": "{\"deviceId\": 50, \"property\": \"temperature\"}",
			"operator": ">",
			"value": "30",
			"nextLogic": "AND"
		},
		{
			"sortOrder": 1,
			"dataSource": "DEVICE",
			"resourceParam": "{\"deviceId\": 101, \"property\": \"status\"}",
			"operator": "==",
			"value": "OFF",
			"nextLogic": null
		}
	]
}
```

#### Ví dụ Response (201 CREATED)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 1,
		"name": "Bật điều hòa khi nóng",
		"priority": 10,
		"isActive": true,
		"roomId": 5,
		"targetDeviceId": 101,
		"targetDeviceCategory": "AIR_CONDITION",
		"actionParams": "{\"temperature\": 22, \"mode\": \"cool\"}",
		"conditions": [
			{
				"id": 1,
				"sortOrder": 0,
				"dataSource": "DEVICE",
				"resourceParam": "{\"deviceId\": 50, \"property\": \"temperature\"}",
				"operator": ">",
				"value": "30",
				"nextLogic": "AND",
				"createdAt": "2024-06-07T09:00:00Z",
				"updatedAt": "2024-06-07T09:00:00Z"
			},
			{
				"id": 2,
				"sortOrder": 1,
				"dataSource": "DEVICE",
				"resourceParam": "{\"deviceId\": 101, \"property\": \"status\"}",
				"operator": "==",
				"value": "OFF",
				"nextLogic": null,
				"createdAt": "2024-06-07T09:00:00Z",
				"updatedAt": "2024-06-07T09:00:00Z"
			}
		],
		"createdAt": "2024-06-07T09:00:00Z",
		"updatedAt": "2024-06-07T09:00:00Z"
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

#### Lỗi Có thể xảy ra

```json
{
	"status": 400,
	"message": "Bad Request",
	"errors": ["Rule name exists"],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### PUT /api/v1/rules/{id}

> Cập nhật thông tin của một quy tắc hiện có.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                | Bắt buộc |
| :-- | :--- | :------------------- | :------- |
| id  | Long | ID của quy tắc cần cập nhật | Có       |

#### Request Body Fields

| Tên trường            | Loại   | Bắt buộc | Mô tả                                           |
| :-------------------- | :----- | :------- | :---------------------------------------------- |
| name                  | string | Có       | Tên quy tắc (không rỗng)                       |
| priority              | int    | Có       | Độ ưu tiên (>= 0)                              |
| targetDeviceId        | Long   | Có       | ID của thiết bị mục tiêu                        |
| targetDeviceCategory  | string | Có       | Loại thiết bị                                  |
| actionParams          | string | Không    | Tham số hành động (JSON format)                |
| isActive              | boolean| Không    | Trạng thái hoạt động                           |
| conditions            | array  | Có       | Danh sách điều kiện (ít nhất 1 điều kiện)      |

#### Ví dụ Request Body

```json
{
	"name": "Bật điều hòa khi nóng (cập nhật)",
	"priority": 15,
	"targetDeviceId": 101,
	"targetDeviceCategory": "AIR_CONDITION",
	"actionParams": "{\"temperature\": 20, \"mode\": \"cool\"}",
	"isActive": true,
	"conditions": [
		{
			"id": 1,
			"sortOrder": 0,
			"dataSource": "DEVICE",
			"resourceParam": "{\"deviceId\": 50, \"property\": \"temperature\"}",
			"operator": ">",
			"value": "32",
			"nextLogic": "AND"
		}
	]
}
```

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Bật điều hòa khi nóng (cập nhật)",
		"priority": 15,
		"isActive": true,
		"roomId": 5,
		"targetDeviceId": 101,
		"targetDeviceCategory": "AIR_CONDITION",
		"actionParams": "{\"temperature\": 20, \"mode\": \"cool\"}",
		"conditions": [
			{
				"id": 1,
				"sortOrder": 0,
				"dataSource": "DEVICE",
				"resourceParam": "{\"deviceId\": 50, \"property\": \"temperature\"}",
				"operator": ">",
				"value": "32",
				"nextLogic": "AND",
				"createdAt": "2024-06-07T09:00:00Z",
				"updatedAt": "2024-06-07T10:00:00Z"
			}
		],
		"createdAt": "2024-06-07T09:00:00Z",
		"updatedAt": "2024-06-07T10:00:00Z"
	},
	"timestamp": "2024-06-07T10:00:00Z"
}
```

---

### DELETE /api/v1/rules/{id}

> Xóa một quy tắc.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                | Bắt buộc |
| :-- | :--- | :------------------- | :------- |
| id  | Long | ID của quy tắc cần xóa | Có       |

#### Ví dụ Response (204 NO_CONTENT)

```json
{
	"status": 204,
	"message": "No Content",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

#### Lỗi Có thể xảy ra

```json
{
	"status": 404,
	"message": "Not Found",
	"errors": ["Rule not found"],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### PATCH /api/v1/rules/{id}/status

> Thay đổi trạng thái hoạt động (active/inactive) của một quy tắc.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                | Bắt buộc |
| :-- | :--- | :------------------- | :------- |
| id  | Long | ID của quy tắc       | Có       |

#### Tham số Truy vấn (Query Parameters)

| Tên      | Loại   | Mô tả              | Bắt buộc |
| :------- | :----- | :----------------- | :------- |
| isActive | boolean| Trạng thái mới     | Có       |

#### Ví dụ Request

```
PATCH /api/v1/rules/1/status?isActive=false
```

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/rules/scan

> Thực thi quét toàn cục tất cả các quy tắc đang hoạt động.

Quá trình quét:
1. Lấy tất cả các quy tắc đang hoạt động (isActive = true)
2. Nhóm các quy tắc theo thiết bị mục tiêu (category:id)
3. Đánh giá điều kiện của từng quy tắc trong mỗi nhóm
4. Chọn quy tắc có độ ưu tiên cao nhất (Winner-Takes-All)
5. Thực thi hành động của quy tắc chiến thắng

#### Ví dụ Request

```
POST /api/v1/rules/scan
```

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/rules/reload

> Tái tải cấu hình tất cả các quy tắc từ cơ sở dữ liệu.

Sử dụng trong trường hợp cần đồng bộ hóa cấu hình quy tắc sau khi có thay đổi trong database.

#### Ví dụ Request

```
POST /api/v1/rules/reload
```

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## Ghi chú

### Data Source Types (RuleDataSource)
- **DEVICE**: Lấy dữ liệu từ thiết bị (ví dụ: nhiệt độ, trạng thái)
- **AUTOMATION**: Lấy dữ liệu từ trạng thái tự động hóa
- **CONSTANT**: Giá trị hằng định
- **RULE**: Lấy kết quả từ quy tắc khác

### Operators
- **Comparison**: `==`, `!=`, `>`, `<`, `>=`, `<=`
- **Membership**: `IN`, `NOT_IN`
- **Pattern**: `LIKE`, `NOT_LIKE`

### Logic Gates
- **AND**: Cả hai điều kiện phải đúng
- **OR**: Ít nhất một điều kiện phải đúng (mặc định là AND)