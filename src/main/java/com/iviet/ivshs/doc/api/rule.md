# Rule Module

## Rule API Documentation

Quy tắc (Rule) là cơ chế để tự động điều khiển các thiết bị dựa trên các điều kiện được định nghĩa. Mỗi quy tắc có:
- **Điều kiện (Conditions)**: Các tiêu chí cần được đáp ứng (ví dụ: nhiệt độ > 30°C)
- **Hành động (Action)**: Lệnh được thực thi khi điều kiện được đáp ứng (ví dụ: bật điều hòa)
- **Độ ưu tiên (Priority)**: Được sử dụng để chọn quy tắc khi có nhiều quy tắc thỏa mãn

---

<details>
<summary><b>GET</b> <code>/api/v1/rules</code> - Lấy danh sách quy tắc (Phân trang)</summary>

> Lấy danh sách quy tắc với hỗ trợ phân trang.

### Query Parameters

| Tên  | Loại | Mô tả               | Bắt buộc/Mặc định |
| :--- | :--- | :------------------ | :---------------- |
| page | int  | Trang cần lấy       | Mặc định: 0       |
| size | int  | Số lượng trên trang | Mặc định: 10      |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
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
		"page": 0,
		"size": 10,
		"totalElements": 1,
		"totalPages": 1
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rules/all</code> - Lấy danh sách tất cả quy tắc</summary>

> Lấy toàn bộ danh sách quy tắc (không phân trang), thường dùng cho dropdown hoặc cache nội bộ.

### Response (200 OK)

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

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rules/{id}</code> - Lấy chi tiết quy tắc</summary>

> Lấy thông tin chi tiết của một quy tắc theo ID.

### Path Parameters

| Tên | Loại | Mô tả                | Bắt buộc/Mặc định |
| :-- | :--- | :------------------- | :------- |
| id  | Long | ID của quy tắc cần lấy | Có       |

### Response (200 OK)

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

### Response (404 Not Found)

```json
{
	"status": 404,
	"message": "Not Found",
	"errors": ["Rule not found"],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rules</code> - Tạo mới quy tắc</summary>

> Tạo mới một quy tắc điều khiển thiết bị.

### Request Body

| Tên trường            | Loại   | Bắt buộc | Mô tả                                           |
| :-------------------- | :----- | :------- | :---------------------------------------------- |
| name                  | string | Có       | Tên quy tắc (không rỗng)                       |
| priority              | int    | Có       | Độ ưu tiên (>= 0, cao hơn = ưu tiên hơn)       |
| roomId                | Long   | Có       | ID của phòng (dùng để xác định ngữ cảnh dữ liệu) |
| targetDeviceId        | Long   | Có       | ID của thiết bị mục tiêu                        |
| targetDeviceCategory  | string | Có       | Loại thiết bị (ví dụ: AIR_CONDITION, LIGHT)    |
| actionParams          | string | Không    | Tham số hành động (JSON format)                |
| conditions            | array  | Có       | Danh sách điều kiện (ít nhất 1 điều kiện)      |

#### Details of Request Body Conditions `conditions`

| Tên trường    | Loại   | Bắt buộc | Mô tả                                              |
| :------------ | :----- | :------- | :------------------------------------------------- |
| sortOrder     | int    | Có       | Thứ tự đánh giá (>= 0)                             |
| dataSource    | enum   | Có       | Nguồn dữ liệu (xem chi tiết RuleDataSource dưới Enumerations) |
| resourceParam | string | Có       | Tham số tài nguyên (JSON format)                   |
| operator      | string | Có       | Toán tử so sánh: ==, !=, >, <, >=, <=, IN, etc    |
| value         | string | Có       | Giá trị so sánh                                    |
| nextLogic     | string | Không    | Logic cho điều kiện tiếp theo: AND, OR (Mặc định: AND) |

### Request Example

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

### Response (201 Created)

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

### Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Bad Request",
	"errors": ["Rule name exists"],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/rules/{id}</code> - Cập nhật quy tắc</summary>

> Cập nhật thông tin của một quy tắc hiện có.

### Path Parameters

| Tên | Loại | Mô tả                | Bắt buộc/Mặc định |
| :-- | :--- | :------------------- | :------- |
| id  | Long | ID của quy tắc cần cập nhật | Có       |

### Request Body

| Tên trường            | Loại   | Bắt buộc | Mô tả                                           |
| :-------------------- | :----- | :------- | :---------------------------------------------- |
| name                  | string | Có       | Tên quy tắc (không rỗng)                       |
| priority              | int    | Có       | Độ ưu tiên (>= 0)                              |
| targetDeviceId        | Long   | Có       | ID của thiết bị mục tiêu                        |
| targetDeviceCategory  | string | Có       | Loại thiết bị                                  |
| actionParams          | string | Không    | Tham số hành động (JSON format)                |
| isActive              | boolean| Không    | Trạng thái hoạt động                           |
| conditions            | array  | Có       | Danh sách điều kiện (ít nhất 1 điều kiện)      |

### Request Example

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

### Response (200 OK)

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

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/rules/{id}</code> - Xóa quy tắc</summary>

> Xóa một quy tắc.

### Path Parameters

| Tên | Loại | Mô tả                | Bắt buộc/Mặc định |
| :-- | :--- | :------------------- | :------- |
| id  | Long | ID của quy tắc cần xóa | Có       |

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "No Content",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

### Response (404 Not Found)

```json
{
	"status": 404,
	"message": "Not Found",
	"errors": ["Rule not found"],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>PATCH</b> <code>/api/v1/rules/{id}/status</code> - Đổi trạng thái quy tắc</summary>

> Thay đổi trạng thái hoạt động (active/inactive) của một quy tắc.

### Path Parameters

| Tên | Loại | Mô tả                | Bắt buộc/Mặc định |
| :-- | :--- | :------------------- | :------- |
| id  | Long | ID của quy tắc       | Có       |

### Request Body

| Tên      | Loại   | Mô tả              | Bắt buộc |
| :------- | :----- | :----------------- | :------- |
| isActive | boolean| Trạng thái mới     | Có       |

### Request Example

```json
{
    "isActive": false
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rules:scan</code> - Quét quy tắc (toàn cục)</summary>

> Thực thi quét toàn cục tất cả các quy tắc đang hoạt động.
> 
> Quá trình quét:
> 1. Lấy tất cả các quy tắc đang hoạt động (isActive = true)
> 2. Nhóm các quy tắc theo thiết bị mục tiêu (category:id)
> 3. Đánh giá điều kiện của từng quy tắc trong mỗi nhóm
> 4. Chọn quy tắc có độ ưu tiên cao nhất (Winner-Takes-All)
> 5. Thực thi hành động của quy tắc chiến thắng

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/rules:reload</code> - Tải lại cấu hình quy tắc</summary>

> Tái tải cấu hình tất cả các quy tắc từ cơ sở dữ liệu.
> 
> Sử dụng trong trường hợp cần đồng bộ hóa cấu hình quy tắc sau khi có thay đổi trong database.

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

---

<details>
<summary>Xem chi tiết các hằng số (Enums)</summary>

### RuleDataSource (Data Source Types)

| Giá trị | Mô tả |
| :------ | :---- |
| SYSTEM  | Lấy dữ liệu từ hệ thống (ví dụ: thời gian, ngày, trạng thái hệ thống) |
| ROOM    | Lấy dữ liệu từ phòng (ví dụ: trạng thái phòng, số lượng thiết bị) |
| DEVICE  | Lấy dữ liệu từ thiết bị (ví dụ: trạng thái bật/tắt, chế độ hoạt động) |
| SENSOR  | Lấy dữ liệu từ cảm biến (ví dụ: nhiệt độ, độ ẩm, ánh sáng) |

### DeviceCategory (Device Categories)

| Giá trị           | Mô tả |
| :---------------- | :---- |
| LIGHT             | Thiết bị chiếu sáng (đèn) |
| AIR_CONDITION     | Thiết bị điều hòa không khí |
| TEMPERATURE       | Thiết bị/cảm biến đo nhiệt độ |
| POWER_CONSUMPTION | Thiết bị đo tiêu thụ điện năng |

### Ràng buộc bổ sung

| Tên | Mô tả |
| :-- | :---- |
| Operators (Comparison) | `==`, `!=`, `>`, `<`, `>=`, `<=` |
| Operators (Membership) | `IN`, `NOT_IN` |
| Operators (Pattern)    | `LIKE`, `NOT_LIKE` |
| Logic Gates            | `AND` (Cả hai điều kiện phải đúng), `OR` (Ít nhất một điều kiện phải đúng) |

</details>