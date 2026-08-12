# Hardware Config Module

## Hardware Config API v1 (formerly Device Control)

---

<details>
<summary><b>GET</b> <code>/api/v1/device-controls/{id}</code> - Lấy chi tiết cấu hình phần cứng</summary>

> Lấy thông tin chi tiết một cấu hình phần cứng (Hardware Config) theo ID.

### Path Parameters

| Tên | Loại | Mô tả                 | Bắt buộc |
| :-- | :--- | :-------------------- | :------- |
| id  | Long | ID của Device Control | Có       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"controlType": "GPIO",
		"gpioPin": 12,
		"bleMacAddress": null,
		"apiEndpoint": null,
		"clientId": 100,
		"roomId": 200
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>POST</b> <code>/api/v1/device-controls</code> - Tạo mới cấu hình phần cứng</summary>

> Tạo mới một cấu hình phần cứng (Hardware Config).

### Request Body

| Tên trường        | Loại                | Bắt buộc | Mô tả                                     |
| :---------------- | :------------------ | :------- | :---------------------------------------- |
| controlType | DeviceControlType | Có       | Loại điều khiển (GPIO, BLUETOOTH, API)    |
| gpioPin           | Integer             | Không    | Số chân GPIO (0-40, chỉ cho GPIO)         |
| bleMacAddress     | String              | Không    | Địa chỉ MAC BLUETOOTH (chỉ cho BLUETOOTH) |
| apiEndpoint       | String              | Không    | Endpoint API (chỉ cho API)                |
| clientId          | Long                | Có       | ID của Client                             |
| roomId            | Long                | Có       | ID của Room                               |

### Request Example

```json
{
	"controlType": "GPIO",
	"gpioPin": 12,
	"clientId": 100,
	"roomId": 200
}
```

### Response (201 Created)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 2,
		"controlType": "GPIO",
		"gpioPin": 12,
		"bleMacAddress": null,
		"apiEndpoint": null,
		"clientId": 100,
		"roomId": 200
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>PUT</b> <code>/api/v1/device-controls/{id}</code> - Cập nhật cấu hình phần cứng</summary>

> Cập nhật thông tin một cấu hình phần cứng (Hardware Config) theo ID.

### Path Parameters

| Tên | Loại | Mô tả                 | Bắt buộc |
| :-- | :--- | :-------------------- | :------- |
| id  | Long | ID của cấu hình phần cứng | Có       |

### Request Body

| Tên trường        | Loại                | Bắt buộc | Mô tả                                     |
| :---------------- | :------------------ | :------- | :---------------------------------------- |
| controlType | DeviceControlType | Không    | Loại điều khiển (GPIO, BLUETOOTH, API)    |
| gpioPin           | Integer             | Không    | Số chân GPIO (0-40, chỉ cho GPIO)         |
| bleMacAddress     | String              | Không    | Địa chỉ MAC BLUETOOTH (chỉ cho BLUETOOTH) |
| apiEndpoint       | String              | Không    | Endpoint API (chỉ cho API)                |
| clientId          | Long                | Không    | ID của Client                             |
| roomId            | Long                | Không    | ID của Room                               |

### Request Example

```json
{
	"controlType": "BLUETOOTH",
	"bleMacAddress": "AA:BB:CC:DD:EE:FF",
	"clientId": 100,
	"roomId": 200
}
```

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 2,
		"controlType": "BLUETOOTH",
		"gpioPin": null,
		"bleMacAddress": "AA:BB:CC:DD:EE:FF",
		"apiEndpoint": null,
		"clientId": 100,
		"roomId": 200
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>DELETE</b> <code>/api/v1/device-controls/{id}</code> - Xóa cấu hình phần cứng</summary>

> Xóa một cấu hình phần cứng (Hardware Config) theo ID.

### Path Parameters

| Tên | Loại | Mô tả                 | Bắt buộc |
| :-- | :--- | :-------------------- | :------- |
| id  | Long | ID của cấu hình phần cứng | Có       |

### Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details>
<summary><b>GET</b> <code>/api/v1/device-controls/client/{clientId}</code> - Lấy theo Client ID</summary>

> Lấy danh sách cấu hình phần cứng (Hardware Config) theo Client ID (có phân trang).

### Path Parameters

| Tên      | Loại | Mô tả         | Bắt buộc |
| :------- | :--- | :------------ | :------- |
| clientId | Long | ID của Client | Có       |

### Query Parameters

| Tên  | Loại | Mô tả                  | Mặc định |
| :--- | :--- | :--------------------- | :------- |
| page | int  | Trang hiện tại         | 0        |
| size | int  | Số lượng phần tử/trang | 10       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"controlType": "GPIO",
				"gpioPin": 12,
				"bleMacAddress": null,
				"apiEndpoint": null,
				"clientId": 100,
				"roomId": 200
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
<summary><b>GET</b> <code>/api/v1/device-controls/room/{roomId}</code> - Lấy theo Room ID</summary>

> Lấy danh sách cấu hình phần cứng (Hardware Config) theo Room ID (có phân trang).

### Path Parameters

| Tên    | Loại | Mô tả       | Bắt buộc |
| :----- | :--- | :---------- | :------- |
| roomId | Long | ID của Room | Có       |

### Query Parameters

| Tên  | Loại | Mô tả                  | Mặc định |
| :--- | :--- | :--------------------- | :------- |
| page | int  | Trang hiện tại         | 0        |
| size | int  | Số lượng phần tử/trang | 10       |

### Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"controlType": "GPIO",
				"gpioPin": 12,
				"bleMacAddress": null,
				"apiEndpoint": null,
				"clientId": 100,
				"roomId": 200
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

---
