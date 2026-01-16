# Automation Module

## Automation API Documentation (v1)

---

## GET /api/v1/automations

> Lấy danh sách tất cả các kịch bản tự động hóa (phân trang).

#### Tham số Truy vấn (Query Parameters)

| Tên  | Loại | Mô tả                           | Mặc định |
| :--- | :--- | :------------------------------ | :------- |
| page | int  | Trang hiện tại (bắt đầu từ 0)   | 0        |
| size | int  | Số lượng phần tử trên mỗi trang | 20       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"name": "Tắt đèn buổi tối",
				"cronExpression": "0 18 * * ?",
				"isActive": true,
				"description": "Tự động tắt đèn vào lúc 18h hàng ngày",
				"createdAt": "2024-06-07T09:00:00Z",
				"updatedAt": "2024-06-07T09:00:00Z",
				"actions": [
					{
						"id": 1,
						"targetType": "DEVICE",
						"targetId": 5,
						"actionType": "OFF",
						"parameterValue": null,
						"executionOrder": 0,
						"targetName": "Đèn phòng họp"
					}
				]
			}
		],
		"page": 0,
		"size": 20,
		"totalElements": 1,
		"totalPages": 1
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## GET /api/v1/automations/{id}

> Lấy thông tin chi tiết của một kịch bản tự động hóa theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                   | Bắt buộc |
| :-- | :--- | :---------------------- | :------- |
| id  | Long | ID của kịch bản cần lấy | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Tắt đèn buổi tối",
		"cronExpression": "0 18 * * ?",
		"isActive": true,
		"description": "Tự động tắt đèn vào lúc 18h hàng ngày",
		"createdAt": "2024-06-07T09:00:00Z",
		"updatedAt": "2024-06-07T09:00:00Z",
		"actions": [
			{
				"id": 1,
				"targetType": "DEVICE",
				"targetId": 5,
				"actionType": "OFF",
				"parameterValue": null,
				"executionOrder": 0,
				"targetName": "Đèn phòng họp"
			}
		]
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## POST /api/v1/automations

> Tạo mới một kịch bản tự động hóa.

#### Request Body Fields

| Tên trường     | Loại    | Bắt buộc | Mô tả                                                   |
| :------------- | :------ | :------- | :------------------------------------------------------ |
| name           | string  | Có       | Tên kịch bản (không được để trống)                      |
| cronExpression | string  | Có       | Biểu thức Cron xác định lịch chạy (vd: "0 18 \* \* ?")  |
| isActive       | boolean | Không    | Trạng thái kích hoạt (mặc định: true)                   |
| description    | string  | Không    | Mô tả kịch bản (tối đa 255 ký tự)                       |
| actions        | array   | Có       | Danh sách hành động cần thực hiện (không được để trống) |

#### Request Body - Actions Fields

| Tên trường     | Loại   | Bắt buộc | Mô tả                                          |
| :------------- | :----- | :------- | :--------------------------------------------- |
| targetType     | enum   | Có       | Loại mục tiêu: DEVICE, ROOM, FLOOR, BUILDING   |
| targetId       | Long   | Có       | ID của mục tiêu                                |
| actionType     | enum   | Có       | Loại hành động: ON, OFF, SET_VALUE, etc.       |
| parameterValue | string | Không    | Giá trị tham số (dùng cho hành động SET_VALUE) |
| executionOrder | int    | Không    | Thứ tự thực hiện hành động (mặc định: 0)       |

#### Ví dụ Request Body

```json
{
	"name": "Tắt đèn buổi tối",
	"cronExpression": "0 18 * * ?",
	"isActive": true,
	"description": "Tự động tắt đèn vào lúc 18h hàng ngày",
	"actions": [
		{
			"targetType": "DEVICE",
			"targetId": 5,
			"actionType": "OFF",
			"parameterValue": null,
			"executionOrder": 0
		},
		{
			"targetType": "DEVICE",
			"targetId": 6,
			"actionType": "OFF",
			"parameterValue": null,
			"executionOrder": 1
		}
	]
}
```

#### Ví dụ Response (201 Created)

```json
{
	"status": 201,
	"message": "Created successfully",
	"data": {
		"id": 1,
		"name": "Tắt đèn buổi tối",
		"cronExpression": "0 18 * * ?",
		"isActive": true,
		"description": "Tự động tắt đèn vào lúc 18h hàng ngày",
		"createdAt": "2024-06-07T09:00:00Z",
		"updatedAt": "2024-06-07T09:00:00Z",
		"actions": [
			{
				"id": 1,
				"targetType": "DEVICE",
				"targetId": 5,
				"actionType": "OFF",
				"parameterValue": null,
				"executionOrder": 0,
				"targetName": "Đèn phòng họp"
			},
			{
				"id": 2,
				"targetType": "DEVICE",
				"targetId": 6,
				"actionType": "OFF",
				"parameterValue": null,
				"executionOrder": 1,
				"targetName": "Đèn hành lang"
			}
		]
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## PUT /api/v1/automations/{id}

> Cập nhật thông tin kịch bản tự động hóa theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                    | Bắt buộc |
| :-- | :--- | :----------------------- | :------- |
| id  | Long | ID kịch bản cần cập nhật | Có       |

#### Request Body Fields

| Tên trường     | Loại    | Bắt buộc | Mô tả                              |
| :------------- | :------ | :------- | :--------------------------------- |
| name           | string  | Có       | Tên kịch bản (không được để trống) |
| cronExpression | string  | Có       | Biểu thức Cron xác định lịch chạy  |
| isActive       | boolean | Không    | Trạng thái kích hoạt               |
| description    | string  | Không    | Mô tả kịch bản (tối đa 255 ký tự)  |
| actions        | array   | Không    | Danh sách hành động cần thực hiện  |

#### Ví dụ Request Body

```json
{
	"name": "Tắt đèn buổi tối - Đã sửa",
	"cronExpression": "0 19 * * ?",
	"isActive": true,
	"description": "Tự động tắt đèn vào lúc 19h hàng ngày",
	"actions": [
		{
			"targetType": "DEVICE",
			"targetId": 5,
			"actionType": "OFF",
			"parameterValue": null,
			"executionOrder": 0
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
		"name": "Tắt đèn buổi tối - Đã sửa",
		"cronExpression": "0 19 * * ?",
		"isActive": true,
		"description": "Tự động tắt đèn vào lúc 19h hàng ngày",
		"createdAt": "2024-06-07T09:00:00Z",
		"updatedAt": "2024-06-07T10:00:00Z",
		"actions": [
			{
				"id": 1,
				"targetType": "DEVICE",
				"targetId": 5,
				"actionType": "OFF",
				"parameterValue": null,
				"executionOrder": 0,
				"targetName": "Đèn phòng họp"
			}
		]
	},
	"timestamp": "2024-06-07T10:00:00Z"
}
```

---

## DELETE /api/v1/automations/{id}

> Xóa kịch bản tự động hóa theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả               | Bắt buộc |
| :-- | :--- | :------------------ | :------- |
| id  | Long | ID kịch bản cần xóa | Có       |

#### Ví dụ Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2024-06-07T10:00:00Z"
}
```

---

## GET /api/v1/automations/active

> Lấy danh sách tất cả các kịch bản tự động hóa đang hoạt động (không phân trang).

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"name": "Tắt đèn buổi tối",
			"cronExpression": "0 18 * * ?",
			"isActive": true,
			"description": "Tự động tắt đèn vào lúc 18h hàng ngày",
			"createdAt": "2024-06-07T09:00:00Z",
			"updatedAt": "2024-06-07T09:00:00Z",
			"actions": [
				{
					"id": 1,
					"targetType": "DEVICE",
					"targetId": 5,
					"actionType": "OFF",
					"parameterValue": null,
					"executionOrder": 0,
					"targetName": "Đèn phòng họp"
				}
			]
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

## PATCH /api/v1/automations/{id}/toggle

> Bật/tắt kích hoạt kịch bản tự động hóa.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả               | Bắt buộc |
| :-- | :--- | :------------------ | :------- |
| id  | Long | ID kịch bản cần sửa | Có       |

#### Tham số Truy vấn (Query Parameters)

| Tên      | Loại    | Mô tả                    | Bắt buộc |
| :------- | :------ | :----------------------- | :------- |
| isActive | boolean | Trạng thái kích hoạt mới | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Automation status updated successfully",
	"data": null,
	"timestamp": "2024-06-07T10:00:00Z"
}
```

---

## POST /api/v1/automations/{id}/execute

> Thực thi ngay lập tức một kịch bản tự động hóa mà không cần chờ lịch Cron.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                    | Bắt buộc |
| :-- | :--- | :----------------------- | :------- |
| id  | Long | ID kịch bản cần thực thi | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Automation executed successfully",
	"data": null,
	"timestamp": "2024-06-07T10:00:00Z"
}
```

---

## POST /api/v1/automations/reload

> Tải lại tất cả các kịch bản tự động hóa (thường dùng sau khi cập nhật hoặc thêm mới).

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "All automations reloaded successfully",
	"data": null,
	"timestamp": "2024-06-07T10:00:00Z"
}
```

---

## Data Models

### AutomationDto

Đối tượng đầy đủ thông tin của một kịch bản tự động hóa.

```json
{
	"id": 1,
	"name": "Tắt đèn buổi tối",
	"cronExpression": "0 18 * * ?",
	"isActive": true,
	"description": "Tự động tắt đèn vào lúc 18h hàng ngày",
	"createdAt": "2024-06-07T09:00:00Z",
	"updatedAt": "2024-06-07T09:00:00Z",
	"actions": [
		{
			"id": 1,
			"targetType": "DEVICE",
			"targetId": 5,
			"actionType": "OFF",
			"parameterValue": null,
			"executionOrder": 0,
			"targetName": "Đèn phòng họp"
		}
	]
}
```

### PaginatedResponse

Đối tượng phản hồi phân trang.

```json
{
	"content": [],
	"page": 0,
	"size": 20,
	"totalElements": 0,
	"totalPages": 0
}
```

### ApiResponse

Cấu trúc phản hồi chung cho tất cả các API.

```json
{
	"status": 200,
	"message": "Success",
	"data": {},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

#### Các mã trạng thái thường gặp:

| Mã  | Ý nghĩa                        |
| :-- | :----------------------------- |
| 200 | Thành công - OK                |
| 201 | Thành công - Tạo mới           |
| 204 | Thành công - Không có nội dung |
| 400 | Lỗi - Yêu cầu không hợp lệ     |
| 401 | Lỗi - Chưa xác thực            |
| 403 | Lỗi - Không có quyền           |
| 404 | Lỗi - Không tìm thấy           |
| 500 | Lỗi - Lỗi máy chủ nội bộ       |

---

## Enum Values

### JobTargetType

```
- DEVICE: Thiết bị
- ROOM: Phòng
- FLOOR: Tầng
- BUILDING: Tòa nhà
```

### JobActionType

```
- ON: Bật thiết bị
- OFF: Tắt thiết bị
- SET_VALUE: Đặt giá trị
- INCREASE: Tăng giá trị
- DECREASE: Giảm giá trị
```

---

## Cron Expression Examples

| Biểu thức      | Ý nghĩa                         |
| :------------- | :------------------------------ |
| 0 18 \* \* ?   | Hàng ngày lúc 18:00             |
| 0 9,18 \* \* ? | Hàng ngày lúc 09:00 và 18:00    |
| 0 _/6 _ \* ?   | Mỗi 6 giờ                       |
| 0 0 \* \* 1    | Hàng tuần vào thứ Hai lúc 00:00 |
| 0 0 1 \* ?     | Hàng tháng vào ngày 1 lúc 00:00 |
| _/30 _ \* \* ? | Mỗi 30 phút                     |

---
