# Telemetry Module

## Quản lý dữ liệu telemetry từ gateway và cảm biến

---

<details>
<summary><b>POST</b> <code>/api/v1/telemetries/gateway/{gatewayUsername}</code> - Lấy telemetry từ gateway</summary>

> Fetch dữ liệu telemetry từ gateway.

### Path Parameters

| Tên             | Loại   | Mô tả                 | Bắt buộc |
| :-------------- | :----- | :-------------------- | :------- |
| gatewayUsername | string | Tên đăng nhập gateway | Có       |

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
<summary><b>POST</b> <code>/api/v1/telemetries/room/{roomCode}</code> - Lấy telemetry từ phòng</summary>

> Fetch dữ liệu telemetry từ tất cả gateway trong phòng.

### Path Parameters

| Tên      | Loại   | Mô tả           | Bắt buộc |
| :------- | :----- | :-------------- | :------- |
| roomCode | string | Mã phòng (room) | Có       |

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
<summary><b>POST</b> <code>/api/v1/telemetries/temperature/{naturalId}</code> - Lấy telemetry nhiệt độ</summary>

> Fetch dữ liệu nhiệt độ từ cảm biến.

### Path Parameters

| Tên       | Loại   | Mô tả                 | Bắt buộc |
| :-------- | :----- | :-------------------- | :------- |
| naturalId | string | Mã định danh cảm biến | Có       |

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
<summary><b>POST</b> <code>/api/v1/telemetries/power-consumption/{naturalId}</code> - Lấy telemetry điện năng</summary>

> Fetch dữ liệu tiêu thụ điện năng từ cảm biến.

### Path Parameters

| Tên       | Loại   | Mô tả                 | Bắt buộc |
| :-------- | :----- | :-------------------- | :------- |
| naturalId | string | Mã định danh cảm biến | Có       |

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
