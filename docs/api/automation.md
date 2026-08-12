# Automation Module

## Automation API Documentation

---

### 1. Automation Management (Cha)

<details>
<summary><b>GET</b> <code>/api/v1/automations</code> - Lấy danh sách tự động hóa (phân trang)</summary>

> Lấy danh sách tất cả các kịch bản tự động hóa (phân trang). Nội dung không bao gồm danh sách actions.

### Query Parameters

| Tên | Loại | Mô tả | Yêu cầu / Mặc định |
| :--- | :--- | :--- | :--- |
| page | int | Trang hiện tại (bắt đầu từ 0) | Không / Mặc định: 0 |
| size | int | Số lượng phần tử trên mỗi trang | Không / Mặc định: 20 |

### Response (200 OK)

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
        "createdAt": "2026-05-27T09:00:00Z",
        "updatedAt": "2026-05-27T09:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  },
  "timestamp": "2026-05-27T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/automations/{id}</code> - Lấy chi tiết tự động hóa</summary>

> Lấy thông tin chi tiết của một kịch bản tự động hóa theo ID.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| id | Long | ID của kịch bản cần lấy | Có |

### Response (200 OK)

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
    "createdAt": "2026-05-27T09:00:00Z",
    "updatedAt": "2026-05-27T09:00:00Z"
  },
  "timestamp": "2026-05-27T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/automations</code> - Tạo mới một kịch bản tự động hóa</summary>

> Tạo mới một kịch bản tự động hóa và tự động đồng bộ hóa lịch trình với Quartz scheduler.

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| name | string | Có | Tên kịch bản (Không được trống, phải duy nhất). |
| cronExpression | string | Có | Biểu thức Cron cho Quartz scheduler (Không được trống). |
| isActive | boolean | Không | Trạng thái kích hoạt (Mặc định: true). |
| description | string | Không | Mô tả kịch bản. |

### Request Example

```json
{
  "name": "Tắt đèn buổi tối",
  "cronExpression": "0 18 * * ?",
  "isActive": true,
  "description": "Tự động tắt đèn vào lúc 18h"
}
```

### Response (201 Created)

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
    "createdAt": "2026-05-27T09:00:10Z",
    "updatedAt": "2026-05-27T09:00:10Z"
  },
  "timestamp": "2026-05-27T09:00:10Z"
}
```

</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/automations/{id}</code> - Cập nhật kịch bản</summary>

> Cập nhật thông tin kịch bản (Tên, Cron, Mô tả...). Lịch trình sẽ tự động được cập nhật trong Quartz. _Lưu ý: API này không cập nhật actions._

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| id | Long | ID của kịch bản cần sửa | Có |

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| name | string | Có | Tên kịch bản (Không được null/trống, sẽ kiểm tra duy nhất nếu thay đổi). |
| cronExpression | string | Có | Biểu thức Cron (Không được null/trống, phải tuân theo cấu trúc Cron hợp lệ của Quartz). |
| isActive | boolean | Không | Trạng thái kích hoạt. |
| description | string | Không | Mô tả kịch bản. |

### Request Example

```json
{
  "name": "Tắt đèn buổi tối (Updated)",
  "cronExpression": "0 19 * * ?",
  "isActive": true,
  "description": "Cập nhật thời gian tắt đèn sang 19h"
}
```

### Response (200 OK)

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
    "createdAt": "2026-05-27T09:00:00Z",
    "updatedAt": "2026-05-27T10:00:00Z"
  },
  "timestamp": "2026-05-27T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/automations/{id}</code> - Xóa kịch bản tự động hóa</summary>

> Xóa kịch bản tự động hóa. Xóa Automation sẽ hủy lịch trình trong Quartz và xóa tất cả actions con của nó.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| id | Long | ID của kịch bản cần xóa | Có |

### Response (204 No Content)

```json
{
  "status": 204,
  "message": "Automation deleted successfully",
  "data": null,
  "timestamp": "2026-05-27T10:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/automations/active</code> - Lấy kịch bản đang hoạt động</summary>

> Lấy danh sách tất cả các kịch bản đang ở trạng thái hoạt động.

### Response (200 OK)

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
      "createdAt": "2026-05-27T09:00:00Z",
      "updatedAt": "2026-05-27T09:00:00Z"
    }
  ],
  "timestamp": "2026-05-27T09:00:00Z"
}
```

</details>

<br>

---

### 2. Action Management (Con)

<details>
<summary><b>GET</b> <code>/api/v1/automations/{id}/actions</code> - Lấy danh sách actions</summary>

> Lấy danh sách các hành động thuộc về một kịch bản cụ thể, được sắp xếp theo thứ tự thực thi.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| id | Long | ID của Automation | Có |

### Response (200 OK)

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
      "executionOrder": 0,
      "targetName": "Đèn phòng khách"
    }
  ],
  "timestamp": "2026-05-27T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/automations/{id}/actions</code> - Thêm hành động mới</summary>

> Thêm một hành động mới vào kịch bản. ID thiết bị mục tiêu sẽ được kiểm tra sự tồn tại trong CSDL.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| id | Long | ID của Automation | Có |

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| targetType | enum | Có | Loại mục tiêu hỗ trợ: `LIGHT`, `FAN`, `AIR_CONDITION`. |
| targetId | Long | Có | ID của thiết bị mục tiêu (Phải tồn tại trong database). |
| actionType | enum | Có | Loại hành động: `ON`, `OFF`. |
| parameterValue | string | Không | Giá trị tham số cho hành động. |
| executionOrder | int | Có | Thứ tự thực hiện (Phải >= 0 và <= 1000). |

### Request Example

```json
{
  "targetType": "LIGHT",
  "targetId": 5,
  "actionType": "ON",
  "executionOrder": 1
}
```

### Response (201 Created)

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
  "timestamp": "2026-05-27T09:05:00Z"
}
```

</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/automations/actions/{actionId}</code> - Cập nhật hành động</summary>

> Cập nhật thông tin một hành động hiện có.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| actionId | Long | ID của hành động | Có |

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| targetType | enum | Có | Loại mục tiêu: `LIGHT`, `FAN`, `AIR_CONDITION`. |
| targetId | Long | Có | ID của thiết bị mục tiêu (Phải tồn tại trong database). |
| actionType | enum | Có | Loại hành động: `ON`, `OFF`. |
| parameterValue | string | Không | Giá trị tham số cho hành động. |
| executionOrder | int | Không | Thứ tự thực hiện (Mặc định là 0 nếu để null). |

### Request Example

```json
{
  "targetType": "LIGHT",
  "targetId": 5,
  "actionType": "OFF",
  "executionOrder": 1
}
```

### Response (200 OK)

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
  "timestamp": "2026-05-27T09:10:00Z"
}
```

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/automations/actions/{actionId}</code> - Xóa hành động</summary>

> Xóa một hành động khỏi kịch bản.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| actionId | Long | ID của hành động | Có |

### Response (204 No Content)

```json
{
  "status": 204,
  "message": "Action removed successfully",
  "data": null,
  "timestamp": "2026-05-27T09:15:00Z"
}
```

</details>

<br>

---

### 3. System / Control Endpoints

<details>
<summary><b>PATCH</b> <code>/api/v1/automations/{id}/status</code> - Thay đổi trạng thái</summary>

> Bật hoặc tắt trạng thái hoạt động của kịch bản, tự động đồng bộ hóa trạng thái Quartz Job tương ứng.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| id | Long | ID của Automation | Có |

### Query Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| isActive | boolean | Trạng thái mới (true/false) | Có |

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Automation status updated: true",
  "data": null,
  "timestamp": "2026-05-27T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/automations/{id}/execute</code> - Thực thi kịch bản (Manual)</summary>

> Thực thi ngay lập tức kịch bản tự động hóa (Manual Trigger) mà không đợi đến chu kỳ Cron tiếp theo.

### Path Parameters

| Tên | Loại | Mô tả | Bắt buộc |
| :--- | :--- | :--- | :--- |
| id | Long | ID của Automation | Có |

### Response (200 OK)

```json
{
  "status": 200,
  "message": "Automation triggered manually",
  "data": null,
  "timestamp": "2026-05-27T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/automations/reload-job</code> - Tải lại Scheduler</summary>

> Tải lại hệ thống Scheduler (Quartz Jobs). Hủy và đăng ký lại toàn bộ lịch trình cho các kịch bản đang hoạt động.

### Response (200 OK)

```json
{
  "status": 200,
  "message": "System Quartz Jobs reloaded",
  "data": null,
  "timestamp": "2026-05-27T09:00:00Z"
}
```

</details>

<br>

---

### Data Enumerations & Constraints

<details>
<summary>Xem chi tiết Hằng số & Ràng buộc</summary>

### JobTargetType

| Giá trị | Mô tả |
| :--- | :--- |
| `LIGHT` | Thiết bị mục tiêu là Đèn. Kiểm tra tồn tại qua `LightDao`. |
| `FAN` | Thiết bị mục tiêu là Quạt. Kiểm tra tồn tại qua `FanDao`. |
| `AIR_CONDITION` | Thiết bị mục tiêu là Điều hòa. Kiểm tra tồn tại qua `AirConditionDao`. |

### JobActionType

| Giá trị | Mô tả |
| :--- | :--- |
| `ON` | Bật thiết bị mục tiêu. |
| `OFF` | Tắt thiết bị mục tiêu. |

### Ràng buộc bổ sung & Quy tắc nghiệp vụ
- **Kiểm tra trùng tên**: Tạo mới hoặc cập nhật kịch bản sẽ kiểm tra sự tồn tại của tên trong hệ thống nhằm đảm bảo tính duy nhất.
- **Biểu thức Cron**: Bắt buộc phải là biểu thức Cron hợp lệ theo cấu trúc Quartz.
- **Sắp xếp thứ tự thực thi**: Các hành động liên kết sẽ chạy tuần tự theo thứ tự tăng dần của trường `executionOrder`.
- **Tên hiển thị động**: Tên thiết bị mục tiêu (`targetName`) được lấy động dựa trên ngôn ngữ đang cấu hình trong context hiện tại của request (tương ứng header ngôn ngữ), mặc định trả về `"Unknown Device"` nếu không tìm thấy.
</details>
