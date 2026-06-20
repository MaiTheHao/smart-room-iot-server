# Alert Module

## Quản lý cấu hình alert (Rule alert configurations) và các sự kiện alert (Operational alert instances)

---

### 1. Cấu hình Alert lồng trong Rule CRUD
Cấu hình alert (`alertConfigs`) giờ đây được gộp chung trực tiếp vào payload khi tạo/cập nhật Rule. Xem tài liệu Rule API để biết thêm chi tiết.

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
<summary><b>POST</b> <code>/api/v1/alerts/{id}/acknowledge</code> - Xác nhận sự kiện alert</summary>

> Cập nhật trạng thái sự kiện alert sang `ACKNOWLEDGED` (Xác nhận đã nhận thông tin).

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

<details>
<summary><b>POST</b> <code>/api/v1/alerts/{id}/resolve</code> - Giải quyết sự kiện alert</summary>

> Cập nhật trạng thái sự kiện alert sang `RESOLVED` (Xác nhận sự cố đã được xử lý/giải quyết).

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
		"status": "RESOLVED",
		"triggeredAt": "2026-06-20T11:45:00Z",
		"acknowledgedAt": "2026-06-20T11:55:00Z",
		"acknowledgedById": 2,
		"acknowledgedByUsername": "maintenance_user",
		"resolvedAt": "2026-06-20T12:00:00Z",
		"resolvedById": 2,
		"resolvedByUsername": "maintenance_user"
	},
	"timestamp": "2026-06-20T12:00:00Z"
}
```

</details>

<br>

---
