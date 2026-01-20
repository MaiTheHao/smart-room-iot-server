# Automation Module

## Automation API Documentation

---

### 1. Automation Management (Cha)

---

### GET /api/v1/automations

> Lấy danh sách tất cả các kịch bản tự động hóa (phân trang). Nội dung không bao gồm danh sách actions.

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
				"description": "Tự động tắt đèn vào lúc 18h",
				"createdAt": "2024-06-07T09:00:00Z",
				"updatedAt": "2024-06-07T09:00:00Z"
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

### GET /api/v1/automations/{id}

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
		"description": "Tự động tắt đèn vào lúc 18h",
		"createdAt": "2024-06-07T09:00:00Z",
		"updatedAt": "2024-06-07T09:00:00Z"
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/automations

> Tạo mới một kịch bản tự động hóa.

#### Request Body Fields

| Tên trường     | Loại    | Bắt buộc | Mô tả                                  |
| :------------- | :------ | :------- | :------------------------------------- |
| name           | string  | Có       | Tên kịch bản                           |
| cronExpression | string  | Có       | Biểu thức Cron (ví dụ: "0 18 \* \* ?") |
| isActive       | boolean | Không    | Trạng thái kích hoạt (Mặc định: true)  |
| description    | string  | Không    | Mô tả kịch bản                         |

#### Ví dụ Request Body

```json
{
	"name": "Tắt đèn buổi tối",
	"cronExpression": "0 18 * * ?",
	"isActive": true,
	"description": "Tự động tắt đèn vào lúc 18h"
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
		"description": "Tự động tắt đèn vào lúc 18h",
		"createdAt": "2024-06-07T09:00:10Z",
		"updatedAt": "2024-06-07T09:00:10Z"
	},
	"timestamp": "2024-06-07T09:00:10Z"
}
```

---

### PUT /api/v1/automations/{id}

> Cập nhật thông tin kịch bản (Tên, Cron, Mô tả...). _Lưu ý: API này không cập nhật actions._

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                   | Bắt buộc |
| :-- | :--- | :---------------------- | :------- |
| id  | Long | ID của kịch bản cần sửa | Có       |

#### Request Body Fields

| Tên trường     | Loại    | Bắt buộc | Mô tả                |
| :------------- | :------ | :------- | :------------------- |
| name           | string  | Không    | Tên kịch bản         |
| cronExpression | string  | Không    | Biểu thức Cron       |
| isActive       | boolean | Không    | Trạng thái kích hoạt |
| description    | string  | Không    | Mô tả kịch bản       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"name": "Tắt đèn buổi tối (Updated)",
		"cronExpression": "0 19 * * ?",
		"isActive": true,
		"description": "Cập nhật thời gian tắt đèn sang 19h",
		"createdAt": "2024-06-07T09:00:00Z",
		"updatedAt": "2024-06-07T10:00:00Z"
	},
	"timestamp": "2024-06-07T10:00:00Z"
}
```

---

### DELETE /api/v1/automations/{id}

> Xóa kịch bản tự động hóa. Xóa Automation sẽ xóa tất cả actions con của nó.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                   | Bắt buộc |
| :-- | :--- | :---------------------- | :------- |
| id  | Long | ID của kịch bản cần xóa | Có       |

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

### GET /api/v1/automations/active

> Lấy danh sách tất cả các kịch bản đang ở trạng thái hoạt động.

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
			"description": "Tự động tắt đèn vào lúc 18h",
			"createdAt": "2024-06-07T09:00:00Z",
			"updatedAt": "2024-06-07T09:00:00Z"
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### 2. Action Management (Con)

---

### GET /api/v1/automations/{id}/actions

> Lấy danh sách các hành động thuộc về một kịch bản cụ thể.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả             | Bắt buộc |
| :-- | :--- | :---------------- | :------- |
| id  | Long | ID của Automation | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"automationId": 1,
			"targetType": "LIGHT",
			"targetId": 5,
			"actionType": "OFF",
			"parameterValue": null,
			"executionOrder": 0,
			"targetName": "Đèn phòng khách"
		}
	],
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/automations/{id}/actions

> Thêm một hành động mới vào kịch bản.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả             | Bắt buộc |
| :-- | :--- | :---------------- | :------- |
| id  | Long | ID của Automation | Có       |

#### Request Body Fields

| Tên trường     | Loại   | Bắt buộc | Mô tả                                 |
| :------------- | :----- | :------- | :------------------------------------ |
| targetType     | enum   | Có       | Loại mục tiêu (Ví dụ: `LIGHT`)        |
| targetId       | Long   | Có       | ID của thiết bị mục tiêu              |
| actionType     | enum   | Có       | Loại hành động (`ON`, `OFF`)          |
| parameterValue | string | Không    | Tham số bổ sung (Ví dụ: độ sáng "80") |
| executionOrder | int    | Không    | Thứ tự thực hiện (Mặc định: 0)        |

#### Ví dụ Request Body

```json
{
	"targetType": "LIGHT",
	"targetId": 5,
	"actionType": "ON",
	"parameterValue": "100",
	"executionOrder": 1
}
```

#### Ví dụ Response (201 Created)

```json
{
	"status": 201,
	"message": "Created successfully",
	"data": {
		"id": 2,
		"automationId": 1,
		"targetType": "LIGHT",
		"targetId": 5,
		"actionType": "ON",
		"executionOrder": 1,
		"targetName": "Đèn phòng khách"
	},
	"timestamp": "2024-06-07T09:05:00Z"
}
```

---

### PUT /api/v1/automations/actions/{actionId}

> Cập nhật thông tin một hành động hiện có.

#### Tham số Đường dẫn (Path Parameters)

| Tên      | Loại | Mô tả            | Bắt buộc |
| :------- | :--- | :--------------- | :------- |
| actionId | Long | ID của hành động | Có       |

#### Request Body Fields

Tương tự POST /actions.

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 2,
		"automationId": 1,
		"targetType": "LIGHT",
		"targetId": 5,
		"actionType": "OFF",
		"executionOrder": 1,
		"targetName": "Đèn phòng khách"
	},
	"timestamp": "2024-06-07T09:10:00Z"
}
```

---

### DELETE /api/v1/automations/actions/{actionId}

> Xóa một hành động khỏi kịch bản.

#### Tham số Đường dẫn (Path Parameters)

| Tên      | Loại | Mô tả            | Bắt buộc |
| :------- | :--- | :--------------- | :------- |
| actionId | Long | ID của hành động | Có       |

#### Ví dụ Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2024-06-07T09:15:00Z"
}
```

---

### 3. System / Control Endpoints

---

### PATCH /api/v1/automations/{id}/status

> Bật hoặc tắt trạng thái hoạt động của kịch bản.

#### Tham số Truy vấn (Query Parameters)

| Tên      | Loại    | Mô tả                       | Bắt buộc |
| :------- | :------ | :-------------------------- | :------- |
| isActive | boolean | Trạng thái mới (true/false) | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Status updated successfully",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/automations/{id}/execute

> Thực thi ngay lập tức kịch bản tự động hóa (Manual Trigger).

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Execution triggered",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/automations/reload-job

> Tải lại hệ thống Scheduler (Quartz Jobs). Dùng khi cần đồng bộ lại toàn bộ lịch trình.

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "All jobs reloaded successfully",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### Data Enumerations

| Tên Enum          | Giá trị hỗ trợ |
| :---------------- | :------------- |
| **JobTargetType** | `LIGHT`        |
| **JobActionType** | `ON`, `OFF`    |
