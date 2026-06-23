# Alert Module

## API Documentation

### 1. Quản lý cấu hình cảnh báo (Alert Configurations)

<details>
<summary><b>POST</b> <code>/api/v1/alerts</code> - Tạo cấu hình cảnh báo</summary>

> Tạo mới cấu hình cảnh báo (Alert Configuration).

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| namespace | AlertNamespace | Có | Phân vùng nghiệp vụ (`RULE`, `GATEWAY`, `SYSTEM`) |
| alertCode | string | Có | Mã định danh duy nhất của cảnh báo |
| sourceId | string | Có | ID nguồn phát sinh cảnh báo (ví dụ ID của Rule hoặc Gateway) |
| alertName | string | Có | Tên hiển thị của cấu hình cảnh báo |
| severity | Severity | Có | Mức độ nghiêm trọng (`INFO`, `WARNING`, `CRITICAL`) |
| recipientGroupCodes | array[string] | Có | Danh sách mã nhóm nhận tin cảnh báo |
| channels | array[string] | Có | Danh sách kênh gửi tin (giá trị hợp lệ: `PUSH`, `EMAIL`, `SMS`) |
| messageTemplate | string | Có | Mẫu tin nhắn cảnh báo |
| cooldownMinutes | integer | Có | Thời gian chờ tối thiểu giữa các lần gửi cảnh báo (phút, tối thiểu 0) |

### Request Example

```json
{
	"namespace": "RULE",
	"alertCode": "TEMP_HIGH_ALERT",
	"sourceId": "rule_10",
	"alertName": "Cấu hình cảnh báo nhiệt độ cao",
	"severity": "CRITICAL",
	"recipientGroupCodes": ["ADMIN_GROUP", "OPERATOR_GROUP"],
	"channels": ["PUSH", "EMAIL"],
	"messageTemplate": "Quy tắc {{rule_name}} bị kích hoạt: Nhiệt độ đạt {{cond0_value}}°C (ngưỡng {{cond0_threshold}}°C).",
	"cooldownMinutes": 15
}
```

### Response (201 Created)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 1,
		"namespace": "RULE",
		"alertCode": "TEMP_HIGH_ALERT",
		"sourceId": "rule_10",
		"alertName": "Cấu hình cảnh báo nhiệt độ cao",
		"severity": "CRITICAL",
		"recipientGroupCodes": ["ADMIN_GROUP", "OPERATOR_GROUP"],
		"channels": ["PUSH", "EMAIL"],
		"messageTemplate": "Quy tắc {{rule_name}} bị kích hoạt: Nhiệt độ đạt {{cond0_value}}°C (ngưỡng {{cond0_threshold}}°C).",
		"cooldownMinutes": 15,
		"createdAt": "2026-06-22T09:30:00Z",
		"updatedAt": "2026-06-22T09:30:00Z"
	},
	"timestamp": "2026-06-22T09:30:00Z"
}
```

</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/alerts/{id}</code> - Cập nhật cấu hình cảnh báo</summary>

> Cập nhật thông tin cấu hình cảnh báo theo ID.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :--- | :--- | :--- | :--- |
| id | Long | ID của cấu hình cảnh báo | Có |

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| alertName | string | Có | Tên hiển thị mới của cấu hình cảnh báo |
| severity | Severity | Có | Mức độ nghiêm trọng mới (`INFO`, `WARNING`, `CRITICAL`) |
| recipientGroupCodes | array[string] | Có | Danh sách mã nhóm nhận tin cảnh báo mới |
| channels | array[string] | Có | Danh sách kênh gửi tin mới |
| messageTemplate | string | Có | Mẫu tin nhắn cảnh báo mới |
| cooldownMinutes | integer | Có | Thời gian chờ tối thiểu mới (phút, tối thiểu 0) |

### Request Example

```json
{
	"alertName": "Cấu hình cảnh báo nhiệt độ quá cao",
	"severity": "CRITICAL",
	"recipientGroupCodes": ["ADMIN_GROUP"],
	"channels": ["PUSH"],
	"messageTemplate": "Quy tắc {{rule_name}} bị kích hoạt: Nhiệt độ đạt {{cond0_value}}°C (ngưỡng {{cond0_threshold}}°C).",
	"cooldownMinutes": 10
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"namespace": "RULE",
		"alertCode": "TEMP_HIGH_ALERT",
		"sourceId": "rule_10",
		"alertName": "Cấu hình cảnh báo nhiệt độ quá cao",
		"severity": "CRITICAL",
		"recipientGroupCodes": ["ADMIN_GROUP"],
		"channels": ["PUSH"],
		"messageTemplate": "Quy tắc {{rule_name}} bị kích hoạt: Nhiệt độ đạt {{cond0_value}}°C (ngưỡng {{cond0_threshold}}°C).",
		"cooldownMinutes": 10,
		"createdAt": "2026-06-22T09:30:00Z",
		"updatedAt": "2026-06-22T09:32:00Z"
	},
	"timestamp": "2026-06-22T09:32:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/alerts/{id}</code> - Lấy cấu hình cảnh báo theo ID</summary>

> Lấy chi tiết thông tin cấu hình cảnh báo theo ID.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :--- | :--- | :--- | :--- |
| id | Long | ID của cấu hình cảnh báo | Có |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"namespace": "RULE",
		"alertCode": "TEMP_HIGH_ALERT",
		"sourceId": "rule_10",
		"alertName": "Cấu hình cảnh báo nhiệt độ quá cao",
		"severity": "CRITICAL",
		"recipientGroupCodes": ["ADMIN_GROUP"],
		"channels": ["PUSH"],
		"messageTemplate": "Quy tắc {{rule_name}} bị kích hoạt: Nhiệt độ đạt {{cond0_value}}°C (ngưỡng {{cond0_threshold}}°C).",
		"cooldownMinutes": 10,
		"createdAt": "2026-06-22T09:30:00Z",
		"updatedAt": "2026-06-22T09:32:00Z"
	},
	"timestamp": "2026-06-22T09:32:00Z"
}
```

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/alerts/{id}</code> - Xóa cấu hình cảnh báo</summary>

> Xóa một cấu hình cảnh báo theo ID.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :--- | :--- | :--- | :--- |
| id | Long | ID của cấu hình cảnh báo | Có |

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "Alert configuration deleted successfully",
	"data": null,
	"timestamp": "2026-06-22T09:33:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/alerts</code> - Tìm kiếm cấu hình cảnh báo theo nguồn</summary>

> Lấy danh sách cấu hình cảnh báo lọc theo Namespace và Source ID.

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :--- | :--- | :--- | :--- |
| namespace | AlertNamespace | Phân vùng nghiệp vụ của cảnh báo | Có |
| sourceId | string | ID nguồn phát sinh cảnh báo | Có |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"namespace": "RULE",
				"alertCode": "TEMP_HIGH_ALERT",
				"sourceId": "rule_10",
				"alertName": "Cấu hình cảnh báo nhiệt độ quá cao",
				"severity": "CRITICAL",
				"recipientGroupCodes": ["ADMIN_GROUP"],
				"channels": ["PUSH"],
				"messageTemplate": "Quy tắc {{rule_name}} bị kích hoạt: Nhiệt độ đạt {{cond0_value}}°C (ngưỡng {{cond0_threshold}}°C).",
				"cooldownMinutes": 10,
				"createdAt": "2026-06-22T09:30:00Z",
				"updatedAt": "2026-06-22T09:32:00Z"
			}
		],
		"page": 0,
		"size": 20,
		"totalElements": 1,
		"totalPages": 1
	},
	"timestamp": "2026-06-22T09:34:00Z"
}
```

</details>

<br>

---

### 2. Quản lý các sự kiện cảnh báo thực tế (Alert Instances)

<details>
<summary><b>GET</b> <code>/api/v1/alerts/instances</code> - Danh sách tất cả sự kiện cảnh báo</summary>

> Lấy danh sách phân trang tất cả sự kiện cảnh báo thực tế xảy ra trong hệ thống.

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :--- | :--- | :--- | :--- |
| status | AlertStatus | Lọc theo trạng thái cảnh báo | Không |
| severity | Severity | Lọc theo mức độ nghiêm trọng | Không |
| page | int | Trang hiện tại | 0 |
| size | int | Số lượng phần tử/trang | 10 |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 101,
				"alertConfigId": 1,
				"alertConfigName": "Cấu hình cảnh báo nhiệt độ quá cao",
				"namespace": "RULE",
				"sourceId": "rule_10",
				"title": "Cảnh báo nhiệt độ quá cao",
				"body": "Quy tắc Cấu hình cảnh báo nhiệt độ quá cao bị kích hoạt: Nhiệt độ đạt 45°C (ngưỡng 40°C).",
				"severity": "CRITICAL",
				"status": "ACTIVE",
				"triggerCount": 1,
				"triggeredAt": "2026-06-22T09:31:00Z",
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
		"totalElements": 1,
		"totalPages": 1
	},
	"timestamp": "2026-06-22T09:35:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/alerts/{alertConfigId}/instances</code> - Danh sách sự kiện cảnh báo theo ID cấu hình</summary>

> Lấy danh sách phân trang các sự kiện cảnh báo thuộc về một ID cấu hình cảnh báo (alertId/alertConfigId).

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :--- | :--- | :--- | :--- |
| alertConfigId | Long | ID của cấu hình cảnh báo | Có |

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :--- | :--- | :--- | :--- |
| status | AlertStatus | Lọc theo trạng thái cảnh báo | Không |
| severity | Severity | Lọc theo mức độ nghiêm trọng | Không |
| page | int | Trang hiện tại | 0 |
| size | int | Số lượng phần tử/trang | 10 |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 101,
				"alertConfigId": 1,
				"alertConfigName": "Cấu hình cảnh báo nhiệt độ quá cao",
				"namespace": "RULE",
				"sourceId": "rule_10",
				"title": "Cảnh báo nhiệt độ quá cao",
				"body": "Quy tắc Cấu hình cảnh báo nhiệt độ quá cao bị kích hoạt: Nhiệt độ đạt 45°C (ngưỡng 40°C).",
				"severity": "CRITICAL",
				"status": "ACTIVE",
				"triggerCount": 1,
				"triggeredAt": "2026-06-22T09:31:00Z",
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
		"totalElements": 1,
		"totalPages": 1
	},
	"timestamp": "2026-06-22T09:35:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/alerts/{alertConfigId}/instances/{instanceId}</code> - Chi tiết một sự kiện cảnh báo</summary>

> Lấy chi tiết thông tin một sự kiện cảnh báo cụ thể.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :--- | :--- | :--- | :--- |
| alertConfigId | Long | ID của cấu hình cảnh báo | Có |
| instanceId | Long | ID của sự kiện cảnh báo | Có |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 101,
		"alertConfigId": 1,
		"alertConfigName": "Cấu hình cảnh báo nhiệt độ quá cao",
		"namespace": "RULE",
		"sourceId": "rule_10",
		"title": "Cảnh báo nhiệt độ quá cao",
		"body": "Quy tắc Cấu hình cảnh báo nhiệt độ quá cao bị kích hoạt: Nhiệt độ đạt 45°C (ngưỡng 40°C).",
		"severity": "CRITICAL",
		"status": "ACTIVE",
		"triggerCount": 1,
		"triggeredAt": "2026-06-22T09:31:00Z",
		"acknowledgedAt": null,
		"acknowledgedById": null,
		"acknowledgedByUsername": null,
		"resolvedAt": null,
		"resolvedById": null,
		"resolvedByUsername": null
	},
	"timestamp": "2026-06-22T09:35:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/alerts/{alertConfigId}/instances/{instanceId}/acknowledge</code> - Xác nhận sự kiện cảnh báo</summary>

> Người dùng xác nhận đã biết/đọc thông báo sự kiện cảnh báo này. Trạng thái cảnh báo chuyển sang `ACKNOWLEDGED`.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :--- | :--- | :--- | :--- |
| alertConfigId | Long | ID của cấu hình cảnh báo | Có |
| instanceId | Long | ID của sự kiện cảnh báo | Có |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 101,
		"alertConfigId": 1,
		"alertConfigName": "Cấu hình cảnh báo nhiệt độ quá cao",
		"namespace": "RULE",
		"sourceId": "rule_10",
		"title": "Cảnh báo nhiệt độ quá cao",
		"body": "Quy tắc Cấu hình cảnh báo nhiệt độ quá cao bị kích hoạt: Nhiệt độ đạt 45°C (ngưỡng 40°C).",
		"severity": "CRITICAL",
		"status": "ACKNOWLEDGED",
		"triggerCount": 1,
		"triggeredAt": "2026-06-22T09:31:00Z",
		"acknowledgedAt": "2026-06-22T09:36:00Z",
		"acknowledgedById": 3,
		"acknowledgedByUsername": "operator_user",
		"resolvedAt": null,
		"resolvedById": null,
		"resolvedByUsername": null
	},
	"timestamp": "2026-06-22T09:36:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/alerts/{alertConfigId}/instances/{instanceId}/resolve</code> - Giải quyết sự kiện cảnh báo</summary>

> Xác nhận sự cố cảnh báo đã được giải quyết hoặc xử lý. Trạng thái cảnh báo chuyển sang `RESOLVED`.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :--- | :--- | :--- | :--- |
| alertConfigId | Long | ID của cấu hình cảnh báo | Có |
| instanceId | Long | ID của sự kiện cảnh báo | Có |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 101,
		"alertConfigId": 1,
		"alertConfigName": "Cấu hình cảnh báo nhiệt độ quá cao",
		"namespace": "RULE",
		"sourceId": "rule_10",
		"title": "Cảnh báo nhiệt độ quá cao",
		"body": "Quy tắc Cấu hình cảnh báo nhiệt độ quá cao bị kích hoạt: Nhiệt độ đạt 45°C (ngưỡng 40°C).",
		"severity": "CRITICAL",
		"status": "RESOLVED",
		"triggerCount": 1,
		"triggeredAt": "2026-06-22T09:31:00Z",
		"acknowledgedAt": "2026-06-22T09:36:00Z",
		"acknowledgedById": 3,
		"acknowledgedByUsername": "operator_user",
		"resolvedAt": "2026-06-22T09:40:00Z",
		"resolvedById": 3,
		"resolvedByUsername": "operator_user"
	},
	"timestamp": "2026-06-22T09:40:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/alerts/{alertConfigId}/instances/{instanceId}/logs</code> - Lấy lịch sử log của sự kiện cảnh báo</summary>

> Truy vấn danh sách các hành động lịch sử (logs) tác động lên sự kiện cảnh báo cụ thể.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :--- | :--- | :--- | :--- |
| alertConfigId | Long | ID của cấu hình cảnh báo | Có |
| instanceId | Long | ID của sự kiện cảnh báo | Có |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": [
		{
			"id": 501,
			"alertId": 101,
			"actionType": "TRIGGERED",
			"actorType": "RULE_ENGINE",
			"actorId": "engine_core",
			"message": "Cảnh báo nhiệt độ cao được kích hoạt từ rule_10",
			"payload": {
				"temperature": 45,
				"threshold": 40
			},
			"createdAt": "2026-06-22T09:31:00Z"
		},
		{
			"id": 502,
			"alertId": 101,
			"actionType": "ACKNOWLEDGED",
			"actorType": "USER",
			"actorId": "3",
			"message": "User operator_user đã xác nhận cảnh báo",
			"payload": null,
			"createdAt": "2026-06-22T09:36:00Z"
		}
	],
	"timestamp": "2026-06-22T09:42:00Z"
}
```

</details>

<br>

---

## Enumerations

### AlertNamespace

| Giá trị | Mô tả |
| :--- | :--- |
| `RULE` | Vi phạm điều kiện quy tắc cảm biến |
| `GATEWAY` | Cổng phần cứng/Gateway ngoại tuyến |
| `SYSTEM` | Lỗi hệ thống nội bộ |

### Severity

| Giá trị | Mô tả |
| :--- | :--- |
| `INFO` | Cảnh báo thông tin thông thường |
| `WARNING` | Cảnh báo cần chú ý |
| `CRITICAL` | Cảnh báo nghiêm trọng khẩn cấp |

### AlertStatus

| Giá trị | Mô tả |
| :--- | :--- |
| `ACTIVE` | Cảnh báo vừa được kích hoạt, chưa được xử lý |
| `ACKNOWLEDGED` | Đã có người dùng xác nhận đã biết về cảnh báo |
| `RESOLVED` | Cảnh báo đã được giải quyết xong |

### AlertActionType

| Giá trị | Mô tả |
| :--- | :--- |
| `TRIGGERED` | Lần kích hoạt cảnh báo đầu tiên |
| `RE_TRIGGERED` | Kích hoạt lặp lại trong thời gian chờ (cooldown) |
| `ACKNOWLEDGED` | Người dùng xác nhận đã nhận thông tin |
| `RESOLVED` | Người dùng giải quyết thủ công |
| `AUTO_RESOLVED` | Hệ thống tự động giải quyết cảnh báo |

### AlertActorType

| Giá trị | Mô tả |
| :--- | :--- |
| `USER` | Người dùng |
| `SYSTEM` | Hệ thống tự động |
| `RULE_ENGINE` | Bộ máy quy tắc (Rule Engine) |

### NotificationChannel

| Giá trị | Mô tả |
| :--- | :--- |
| `PUSH` | Đẩy thông báo qua Firebase Cloud Messaging (FCM) |
| `EMAIL` | Gửi email thông báo qua SMTP |
| `SMS` | Gửi tin nhắn SMS qua cổng dịch vụ |

---

## Cơ chế Message Template

Hệ thống sử dụng cơ chế nội suy chuỗi để tạo nội dung chi tiết cho sự kiện cảnh báo (`body` của `AlertInstance`) dựa trên mẫu tin nhắn (`messageTemplate`) của cấu hình cảnh báo.

### Cú pháp mẫu tin nhắn
Mẫu tin nhắn sử dụng cú pháp hai dấu ngoặc nhọn: `{{tên_biến}}`. Bất kỳ chuỗi nào nằm trong `{{...}}` sẽ được thay thế bằng giá trị tương ứng từ dữ liệu ngữ cảnh (nếu có). Nếu không tìm thấy biến trong ngữ cảnh, chuỗi `{{tên_biến}}` sẽ được giữ nguyên dưới dạng văn bản thô.

### Giới hạn hỗ trợ hiện tại
Hiện tại, cơ chế này **chỉ khả thi (hỗ trợ) đối với phân vùng nghiệp vụ `RULE`** (cảnh báo kích hoạt từ Rule Engine). Các phân vùng khác (`GATEWAY`, `SYSTEM`) chưa cung cấp dữ liệu ngữ cảnh để nạp vào template.

### Danh sách các biến khả dụng (cho `RULE`)

Khi một quy tắc (Rule) kích hoạt cảnh báo, các biến sau sẽ tự động được truyền vào để sử dụng trong `messageTemplate`:

| Tên biến | Kiểu dữ liệu | Mô tả |
| :--- | :--- | :--- |
| `rule_id` | Long | ID của quy tắc (Rule ID) |
| `rule_name` | String | Tên của quy tắc (Rule Name) |
| `cond{sortOrder}_value` | Object | Giá trị thực tế đo được tại điều kiện có số thứ tự `sortOrder` |
| `cond{sortOrder}_threshold` | String | Ngưỡng giá trị thiết lập tại điều kiện có số thứ tự `sortOrder` |

> *Ví dụ: Nếu Rule id-10 name-AUTORULE định nghĩa "Nếu nhiệt độ của Room > 30 và Power State của AC là OFF" => các biến là: 
> * rule_id: 10
> * rule_name: AUTORULE
> * cond0_value: <Nhiệt độ thực tế của Room lại thời điểm xử lý Rule>
> * cond0_threshold: 30
> * cond1_value: <Trạng thái Power thực tế của AC tại thời điểm xử lý Rule>
> * cond1_threshold: OFF

### Ví dụ mẫu tin nhắn cho Rule
- **Template:** `Quy tắc {{rule_name}} bị kích hoạt: Nhiệt độ đạt {{cond0_value}}°C (ngưỡng {{cond0_threshold}}°C).`
- **Nội dung thực tế sinh ra (`body`):** `Quy tắc Cấu hình cảnh báo nhiệt độ quá cao bị kích hoạt: Nhiệt độ đạt 45°C (ngưỡng 40°C).`

