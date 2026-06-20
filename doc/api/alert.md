# Alert Module

## Quản lý cấu hình alert (Rule alert configurations) và các sự kiện alert (Operational alert instances)

---

### 1. Quản lý cấu hình Alert của Rule

<details>
<summary><b>GET</b> <code>/api/v1/rules/{ruleId}/alert-configs</code> - Lấy cấu hình alert của Rule</summary>

> Lấy danh sách toàn bộ cấu hình alert (RuleActionAlert) gắn liền với Rule cụ thể.

#### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| ruleId | Long | ID của Rule cần lấy cấu hình alert | Có |

#### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"ruleId": 10,
			"alertName": "Cảnh báo nhiệt độ cao phòng Server",
			"severity": "CRITICAL",
			"recipientGroups": ["G_ADMIN", "G_MAINTENANCE"],
			"channels": ["PUSH", "EMAIL"],
			"messageTemplate": "Nhiệt độ phòng server đạt {temperature}°C, vượt ngưỡng an toàn.",
			"cooldownMinutes": 15,
			"autoResolve": true
		}
	],
	"timestamp": "2026-06-20T12:00:00Z"
}
```

</details>

<details>
<summary><b>POST / PUT</b> <code>/api/v1/rules/{ruleId}/alert-configs</code> - Lưu / Cập nhật cấu hình alert của Rule (UPSERT)</summary>

> Lưu hoặc cập nhật cấu hình alert cho Rule (cơ chế UPSERT). Ghi đè toàn bộ giá trị cấu hình alert hiện tại của rule.

#### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| ruleId | Long | ID của Rule cần lưu/cập nhật cấu hình | Có |

#### Request Body

```json
[
	{
		"id": 1, // Optional khi tạo mới, bắt buộc khi cập nhật
		"ruleId": 10,
		"alertName": "Cảnh báo nhiệt độ cao phòng Server",
		"severity": "CRITICAL",
		"recipientGroups": ["G_ADMIN", "G_MAINTENANCE"],
		"channels": ["PUSH", "EMAIL"],
		"messageTemplate": "Nhiệt độ phòng server đạt {temperature}°C, vượt ngưỡng an toàn.",
		"cooldownMinutes": 15,
		"autoResolve": true
	}
]
```

#### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 1,
			"ruleId": 10,
			"alertName": "Cảnh báo nhiệt độ cao phòng Server",
			"severity": "CRITICAL",
			"recipientGroups": ["G_ADMIN", "G_MAINTENANCE"],
			"channels": ["PUSH", "EMAIL"],
			"messageTemplate": "Nhiệt độ phòng server đạt {temperature}°C, vượt ngưỡng an toàn.",
			"cooldownMinutes": 15,
			"autoResolve": true
		}
	],
	"timestamp": "2026-06-20T12:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/rules/{ruleId}/alert-configs</code> - Xóa cấu hình alert của Rule</summary>

> Xóa tất cả cấu hình alert của một Rule.

#### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| ruleId | Long | ID của Rule cần xóa cấu hình alert | Có |

#### Response (204 No Content)

Không trả về nội dung response body.

</details>

<br>

---

### 2. Quản lý các Sự kiện Cảnh báo (Alert Instances)

<details>
<summary><b>GET</b> <code>/api/v1/alerts</code> - Lấy danh sách toàn bộ sự kiện alert</summary>

> Lấy danh sách các sự kiện alert xảy ra trong hệ thống, có phân trang, lọc theo quyền truy cập của User hiện tại (RBAC).

#### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc | Mặc định |
| :--- | :--- | :--- | :--- | :--- |
| status | string | Trạng thái của alert (`ACTIVE`, `ACKNOWLEDGED`, `RESOLVED`) | Không | |
| severity | string | Mức độ nghiêm trọng (`INFO`, `WARNING`, `CRITICAL`) | Không | |
| page | integer | Số thứ tự trang (0-based) | Không | `0` |
| size | integer | Số lượng phần tử mỗi trang (tối đa 100) | Không | `10` |

#### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 101,
				"ruleId": 10,
				"ruleName": "Rule kiểm tra nhiệt độ",
				"title": "Cảnh báo nhiệt độ cao phòng Server",
				"body": "Nhiệt độ phòng server đạt 42°C, vượt ngưỡng an toàn.",
				"severity": "CRITICAL",
				"status": "ACTIVE",
				"triggeredAt": "2026-06-20T11:45:00Z",
				"acknowledgedAt": null,
				"acknowledgedById": null,
				"acknowledgedByUsername": null,
				"resolvedAt": null,
				"resolvedById": null,
				"resolvedByUsername": null
			}
		],
		"page": 0,
		"size": 10,
		"totalElements": 1
	},
	"timestamp": "2026-06-20T12:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/rules/{ruleId}/alerts</code> - Lấy sự kiện alert của riêng 1 Rule</summary>

> Lấy danh sách các sự kiện alert phát sinh từ một Rule cụ thể, lọc theo quyền truy cập (RBAC) của User hiện tại.

#### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| ruleId | Long | ID của Rule cần lấy các sự kiện alert | Có |

#### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc | Mặc định |
| :--- | :--- | :--- | :--- | :--- |
| status | string | Trạng thái lọc (`ACTIVE`, `ACKNOWLEDGED`, `RESOLVED`) | Không | |
| severity | string | Mức độ lọc (`INFO`, `WARNING`, `CRITICAL`) | Không | |
| page | integer | Số thứ tự trang | Không | `0` |
| size | integer | Số lượng phần tử mỗi trang | Không | `10` |

#### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 101,
				"ruleId": 10,
				"ruleName": "Rule kiểm tra nhiệt độ",
				"title": "Cảnh báo nhiệt độ cao phòng Server",
				"body": "Nhiệt độ phòng server đạt 42°C, vượt ngưỡng an toàn.",
				"severity": "CRITICAL",
				"status": "ACTIVE",
				"triggeredAt": "2026-06-20T11:45:00Z",
				"acknowledgedAt": null,
				"acknowledgedById": null,
				"acknowledgedByUsername": null,
				"resolvedAt": null,
				"resolvedById": null,
				"resolvedByUsername": null
			}
		],
		"page": 0,
		"size": 10,
		"totalElements": 1
	},
	"timestamp": "2026-06-20T12:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/alerts/{id}</code> - Chi tiết 1 sự kiện alert</summary>

> Lấy thông tin chi tiết một sự kiện alert cụ thể theo ID.

#### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| id | Long | ID của sự kiện alert | Có |

#### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 101,
		"ruleId": 10,
		"ruleName": "Rule kiểm tra nhiệt độ",
		"title": "Cảnh báo nhiệt độ cao phòng Server",
		"body": "Nhiệt độ phòng server đạt 42°C, vượt ngưỡng an toàn.",
		"severity": "CRITICAL",
		"status": "ACKNOWLEDGED",
		"triggeredAt": "2026-06-20T11:45:00Z",
		"acknowledgedAt": "2026-06-20T11:50:00Z",
		"acknowledgedById": 2,
		"acknowledgedByUsername": "maintenance_user",
		"resolvedAt": null,
		"resolvedById": null,
		"resolvedByUsername": null
	},
	"timestamp": "2026-06-20T12:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>PATCH</b> <code>/api/v1/alerts/{id}</code> - Xác nhận / Giải quyết sự kiện alert</summary>

> Cập nhật trạng thái sự kiện alert sang `ACKNOWLEDGED` (Xác nhận đã nhận thông tin) hoặc `RESOLVED` (Xác nhận sự cố đã được xử lý/giải quyết).

#### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| id | Long | ID của sự kiện alert | Có |

#### Request Body

Lưu ý: Chỉ cho phép cập nhật `status` sang `ACKNOWLEDGED` hoặc `RESOLVED`.

```json
{
	"status": "ACKNOWLEDGED"
}
```

hoặc:

```json
{
	"status": "RESOLVED"
}
```

#### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 101,
		"ruleId": 10,
		"ruleName": "Rule kiểm tra nhiệt độ",
		"title": "Cảnh báo nhiệt độ cao phòng Server",
		"body": "Nhiệt độ phòng server đạt 42°C, vượt ngưỡng an toàn.",
		"severity": "CRITICAL",
		"status": "ACKNOWLEDGED",
		"triggeredAt": "2026-06-20T11:45:00Z",
		"acknowledgedAt": "2026-06-20T11:55:00Z",
		"acknowledgedById": 2,
		"acknowledgedByUsername": "maintenance_user",
		"resolvedAt": null,
		"resolvedById": null,
		"resolvedByUsername": null
	},
	"timestamp": "2026-06-20T12:00:00Z"
}
```

</details>

<br>

---
