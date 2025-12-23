# Device Control Module

## Device Control API v1

---

### GET /api/v1/device-controls/{id}

> Lấy thông tin chi tiết một Device Control theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                 | Bắt buộc |
| :-- | :--- | :-------------------- | :------- |
| id  | Long | ID của Device Control | Có       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 1,
		"deviceControlType": "GPIO",
		"gpioPin": 12,
		"bleMacAddress": null,
		"apiEndpoint": null,
		"clientId": 100,
		"roomId": 200
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### POST /api/v1/device-controls

> Tạo mới một Device Control.

#### Request Body Fields

| Tên trường        | Loại                | Bắt buộc | Mô tả                                     |
| :---------------- | :------------------ | :------- | :---------------------------------------- |
| deviceControlType | DeviceControlTypeV1 | Có       | Loại điều khiển (GPIO, BLUETOOTH, API)    |
| gpioPin           | Integer             | Không    | Số chân GPIO (0-40, chỉ cho GPIO)         |
| bleMacAddress     | String              | Không    | Địa chỉ MAC BLUETOOTH (chỉ cho BLUETOOTH) |
| apiEndpoint       | String              | Không    | Endpoint API (chỉ cho API)                |
| clientId          | Long                | Có       | ID của Client                             |
| roomId            | Long                | Có       | ID của Room                               |

#### Ví dụ Request Body

```json
{
	"deviceControlType": "GPIO",
	"gpioPin": 12,
	"clientId": 100,
	"roomId": 200
}
```

#### Ví dụ Response (201 Created)

```json
{
	"status": 201,
	"message": "Created",
	"data": {
		"id": 2,
		"deviceControlType": "GPIO",
		"gpioPin": 12,
		"bleMacAddress": null,
		"apiEndpoint": null,
		"clientId": 100,
		"roomId": 200
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### PUT /api/v1/device-controls/{id}

> Cập nhật thông tin một Device Control theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                 | Bắt buộc |
| :-- | :--- | :-------------------- | :------- |
| id  | Long | ID của Device Control | Có       |

#### Request Body Fields

| Tên trường        | Loại                | Bắt buộc | Mô tả                                     |
| :---------------- | :------------------ | :------- | :---------------------------------------- |
| deviceControlType | DeviceControlTypeV1 | Không    | Loại điều khiển (GPIO, BLUETOOTH, API)    |
| gpioPin           | Integer             | Không    | Số chân GPIO (0-40, chỉ cho GPIO)         |
| bleMacAddress     | String              | Không    | Địa chỉ MAC BLUETOOTH (chỉ cho BLUETOOTH) |
| apiEndpoint       | String              | Không    | Endpoint API (chỉ cho API)                |
| clientId          | Long                | Không    | ID của Client                             |
| roomId            | Long                | Không    | ID của Room                               |

#### Ví dụ Request Body

```json
{
	"deviceControlType": "BLUETOOTH",
	"bleMacAddress": "AA:BB:CC:DD:EE:FF",
	"clientId": 100,
	"roomId": 200
}
```

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"id": 2,
		"deviceControlType": "BLUETOOTH",
		"gpioPin": null,
		"bleMacAddress": "AA:BB:CC:DD:EE:FF",
		"apiEndpoint": null,
		"clientId": 100,
		"roomId": 200
	},
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### DELETE /api/v1/device-controls/{id}

> Xóa một Device Control theo ID.

#### Tham số Đường dẫn (Path Parameters)

| Tên | Loại | Mô tả                 | Bắt buộc |
| :-- | :--- | :-------------------- | :------- |
| id  | Long | ID của Device Control | Có       |

#### Ví dụ Response (204 No Content)

```json
{
	"status": 204,
	"message": "Deleted successfully",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

---

### GET /api/v1/device-controls/client/{clientId}

> Lấy danh sách Device Control theo Client ID (có phân trang).

#### Tham số Đường dẫn (Path Parameters)

| Tên      | Loại | Mô tả         | Bắt buộc |
| :------- | :--- | :------------ | :------- |
| clientId | Long | ID của Client | Có       |

#### Tham số Truy vấn (Query Parameters)

| Tên  | Loại | Mô tả                  | Mặc định |
| :--- | :--- | :--------------------- | :------- |
| page | int  | Trang hiện tại         | 0        |
| size | int  | Số lượng phần tử/trang | 10       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"deviceControlType": "GPIO",
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

---

### GET /api/v1/device-controls/room/{roomId}

> Lấy danh sách Device Control theo Room ID (có phân trang).

#### Tham số Đường dẫn (Path Parameters)

| Tên    | Loại | Mô tả       | Bắt buộc |
| :----- | :--- | :---------- | :------- |
| roomId | Long | ID của Room | Có       |

#### Tham số Truy vấn (Query Parameters)

| Tên  | Loại | Mô tả                  | Mặc định |
| :--- | :--- | :--------------------- | :------- |
| page | int  | Trang hiện tại         | 0        |
| size | int  | Số lượng phần tử/trang | 10       |

#### Ví dụ Response (200 OK)

```json
{
	"status": 200,
	"message": "Success",
	"data": {
		"content": [
			{
				"id": 1,
				"deviceControlType": "GPIO",
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

---
