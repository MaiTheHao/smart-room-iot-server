# Client Device Module [Giai đoạn thử nghiệm 🧪]

> **Lưu ý:** Các API trong module này đang trong **giai đoạn thử nghiệm (Experimental)**. Cấu trúc payload, endpoint hoặc phản hồi có thể thay đổi trong quá trình phát triển. Dành riêng cho Frontend/Mobile developer sử dụng để test tính năng push notification.

---

## 📖 Các khái niệm quan trọng

Để sử dụng API nhận thông báo đẩy một cách chính xác, dev cần hiểu rõ 2 tham số quan trọng:

- **FCM Token (`fcmToken` / `token`)**: Là một chuỗi (string) dài duy nhất được Firebase SDK tạo ra ở phía client (Web/Android/iOS) khi ứng dụng đăng ký nhận thông báo. Server sử dụng token này làm "địa chỉ nhận" để gửi tin nhắn thông qua hạ tầng Firebase. Token này **không cố định**, nó có thể thay đổi (refresh) khi người dùng cài lại app, xoá dữ liệu, hoặc sau một thời gian dài. Do đó, client cần gửi token này lên server ngay khi có sự thay đổi.
- **Device Identifier (`deviceIdentifier`)**: Là một mã định danh duy nhất gắn liền với thiết bị vật lý hoặc phiên trình duyệt (Ví dụ: UUIDv4 tự sinh và lưu vĩnh viễn vào `localStorage` trên Web, hoặc Device ID phần cứng trên Mobile). Server dùng mã này làm gốc để cập nhật lại `fcmToken` mới nhất cho cùng một thiết bị, tránh tình trạng rác dữ liệu sinh ra nhiều bản ghi mỗi khi FCM Token thay đổi.

---

## 🚀 Client Device API Documentation

<details open>
<summary><b>POST</b> <code>/v1/client-devices/register</code> - Đăng ký thiết bị nhận thông báo đẩy</summary>

> Dùng để đăng ký hoặc cập nhật thông tin thiết bị (FCM token) của user đang đăng nhập. 
> API sẽ tự động lấy thông tin user từ **JWT token** gửi lên. Cơ chế hoạt động là "upsert" dựa vào `deviceIdentifier` (nếu thiết bị đã tồn tại thì cập nhật `fcmToken` mới, chưa thì tạo mới).

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--------- | :--- | :------- | :----- |
| fcmToken | string | Có | Token Firebase Cloud Messaging (độ dài tối đa: 512). |
| deviceIdentifier | string | Có | Mã định danh duy nhất của thiết bị (độ dài tối đa: 255). |
| platform | string | Không | Hệ điều hành/nền tảng của thiết bị. Giá trị cho phép: `WEB`, `ANDROID`, `IOS`. |

### Request Example

```json
{
	"fcmToken": "APA91bE... (long token string)",
	"deviceIdentifier": "123e4567-e89b-12d3-a456-426614174000",
	"platform": "ANDROID"
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

### Response (400 Bad Request)

```json
{
	"status": 400,
	"message": "Validation error (e.g., fcmToken or deviceIdentifier is empty or exceeds max length)",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

### Response (401 Unauthorized)

```json
{
	"status": 401,
	"message": "Missing, expired, or invalid JWT token",
	"data": null,
	"timestamp": "2024-06-07T09:00:00Z"
}
```

</details>

<br>

<details open>
<summary><b>POST</b> <code>/v1/client-devices/test-fcm</code> - Gửi thông báo đẩy thử nghiệm (Test Push)</summary>

> Dùng để bắn thẳng một tin nhắn Push Notification tới một hoặc nhiều thiết bị cụ thể thông qua FCM Token. 
> **Lưu ý:** API này không cần xác thực JWT (có thể không qua Filter) phục vụ cho dev dễ dàng sử dụng Postman để test xem app có hiển thị thông báo hay không. 
> Nếu truyền cả `tokens` và `token`, API sẽ ưu tiên mảng `tokens`.

### Request Body

| Tên trường | Loại | Bắt buộc | Mô tả |
| :--------- | :--- | :------- | :----- |
| token | string | Có* | FCM Token của một thiết bị duy nhất. (*Bắt buộc nếu không có `tokens`)* |
| tokens | array[string]| Có* | Danh sách các FCM Token để gửi đến nhiều thiết bị (Multicast). (*Bắt buộc nếu không có `token`)* |
| title | string | Không | Tiêu đề của thông báo hiển thị trên máy. |
| body | string | Không | Nội dung/thông điệp của thông báo. |
| data | object | Không | Payload tuỳ chọn (key-value) chứa data ẩn gửi kèm thông báo để client tự xử lý logic (điều hướng màn hình, ...). Các giá trị trong object bắt buộc là dạng `string`. |

### Request Example (Gửi một thiết bị)

```json
{
  "token": "eX_token_123...",
  "title": "Cảnh báo nhiệt độ!",
  "body": "Nhiệt độ phòng ngủ vượt quá mức cho phép.",
  "data": {
    "roomId": "room-1",
    "action": "open_alert"
  }
}
```

### Request Example (Gửi nhiều thiết bị)

```json
{
  "tokens": [
    "eX_token_123...",
    "eY_token_456..."
  ],
  "title": "Thông báo chung",
  "body": "Server vừa khởi động lại.",
  "data": {}
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

---

## 📝 Enumerations

### DevicePlatform

| Giá trị | Mô tả |
| :--- | :--- |
| WEB | Nền tảng trình duyệt Web |
| ANDROID | Hệ điều hành Android |
| IOS | Hệ điều hành iOS |
