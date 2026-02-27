# API Documentation Style Guide

Mục đích: Tài liệu này là hướng dẫn (Prompt/Guidelines) dành cho các AI Agent, Copilot và Developer để chuẩn hóa cách viết tài liệu API Markdown trên GitHub.

## 1. Nguyên tắc cốt lõi

- **Nhất quán**: Cấu trúc của mọi API endpoint phải giống nhau.
- **Gọn gàng (Collapsible)**: Sử dụng thẻ HTML `<details>` và `<summary>` cho mỗi endpoint để tài liệu không bị quá dài.
- **Tương thích GitHub**: Phải để 1 dòng trống ngay sau thẻ `<summary>` thì Markdown bên trong thẻ `<details>` mới được GitHub render chính xác.
- **Không thay đổi Interface cũ**: Khi cập nhật doc, tuyệt đối giữ nguyên Request/Response của các API hiện có trừ khi có yêu cầu phá vỡ (breaking changes) rõ ràng.

## 2. Cấu trúc một File Tài liệu API

Một file tài liệu API chuẩn cần có:

- Tiêu đề và Mô tả chung (H1, H2).
- Danh sách các API được bọc trong thẻ `<details>`.
- Phần Enumerations (các hằng số, trạng thái) ở cuối file.
- Phần Error Responses chung (nếu có).

## 3. Template chuẩn cho một Endpoint

AI Agent vui lòng copy format dưới đây khi tạo document cho 1 API mới:

```html
<details>
<summary><b>[HTTP_METHOD]</b> <code>[API_PATH]</code> - [Tên ngắn gọn của API]</summary>

> Mô tả chi tiết về chức năng của API.

### Path/Query Parameters (Nếu có)

| Tên | Loại | Mô tả | Bắt buộc/Mặc định |
| :-- | :--- | :---- | :---------------- |
| id  | Long | Mô tả | Có / Không        |

### Request Body (Nếu có)

| Tên trường | Loại   | Bắt buộc | Mô tả |
| :--------- | :----- | :------- | :---- |
| name       | string | Có       | Mô tả |

### Request Example

\`\`\`json
{
  "key": "value"
}
\`\`\`

### Response ([HTTP_STATUS] [STATUS_TEXT])

\`\`\`json
{
  "status": 200,
  "message": "Success",
  "data": {},
  "timestamp": "2026-01-28T10:00:00Z"
}
\`\`\`

</details>
```

<br>

## 4. Quy tắc về Dữ liệu & Format

- **Bảng (Tables)**: Sử dụng Markdown table, căn lề trái (`:---`). Luôn có cột "Bắt buộc" hoặc "Mặc định".
- **Code Blocks**: Các ví dụ JSON phải được bọc trong ` ```json ` và được format chuẩn xác (Indentation).
- **Tham chiếu Enum**: Nếu request body dùng Enum, phải liên kết hoặc giải thích rõ các giá trị được hỗ trợ (Ví dụ: COOL, HEAT, DRY, FAN).

## 5. Hướng dẫn mô tả Enum & Ràng buộc

- **Vị trí**: Đặt phần "Enumerations & Constraints" trong thẻ `<details>` ở cuối file, trước phần "Error Responses".
- **Định dạng**: Mỗi enum phải có tiêu đề (H3) và bảng liệt kê tất cả các giá trị với mô tả rõ ràng.
- **Ràng buộc bổ sung**: Nếu có giới hạn về giá trị (ví dụ: temperature 16-32, fanSpeed 0-5), phải tạo bảng riêng hoặc thêm dòng giải thích.
- **Tham chiếu chéo**: Trong request body của endpoint, nếu trường sử dụng enum, ghi chú "(xem [Enum_Name] dưới Enumerations)" hoặc liệt kê giá trị trực tiếp.

Ví dụ mẫu (tham khảo #file:air_condition.md):

```html
<details>
<summary>Xem chi tiết các hằng số (Enums)</summary>

### AcMode (ActuatorMode)

| Giá trị | Mô tả |
| :------ | :---- |
| COOL    | Làm lạnh |
| HEAT    | Sưởi ấm |
| DRY     | Hút ẩm |
| FAN     | Chế độ quạt |
| AUTO    | Chế độ tự động |

### Ràng buộc bổ sung

| Tên | Mô tả |
| :-- | :---- |
| Temperature (Nhiệt độ) | Giá trị hợp lệ từ 16 đến 32 |
| FanSpeed (Tốc độ quạt) | Giá trị hợp lệ từ 0 đến 5 |

</details>
```

## 6. Edit prompt
> Prompt này dành cho AI Agent khi được giao nhiệm vụ cập nhật tài liệu API. Mục tiêu là đảm bảo mọi cập nhật đều tuân thủ chuẩn Markdown và không làm mất dữ liệu quan trọng.

```plaintext
Bạn là một Chuyên gia Biên tập Tài liệu Hệ thống. Nhiệm vụ của bạn là định dạng lại (reformat) TOÀN BỘ nội dung file API hiện tại tôi cung cấp sang chuẩn Markdown, tuân thủ nghiêm ngặt các quy tắc và Template trong file `_format.md` (tôi đã đính kèm/link tới).

⚠️ CÁC QUY TẮC BẮT BUỘC PHẢI TUÂN THỦ (CRITICAL RULES):
1. BẢO TOÀN DỮ LIỆU (ZERO DATA LOSS): TUYỆT ĐỐI KHÔNG thay đổi, xóa bỏ, lược bớt hay làm sai lệch bất kỳ thông số nào (tên API, method, endpoint, parameters, request body, response, enum, mô tả, v.v.). Mục tiêu của bạn chỉ là đổi "lớp áo" UI/Format (sử dụng HTML tags, bảng biểu), KHÔNG đổi "nội dung lõi" để tránh phá vỡ giao tiếp của hệ thống.
2. KHÔNG LƯỜI BIẾNG (NO TRUNCATION): Bạn phải đọc và xuất ra kết quả của TOÀN BỘ file từ đầu đến cuối. TUYỆT ĐỐI KHÔNG ĐƯỢC dùng các cụm từ lấp lửng như "... (các phần còn lại giữ nguyên)", "... (existing code omitted)", hay dừng lại giữa chừng. Mọi API phải được viết ra đầy đủ.
3. CHUẨN ĐỊNH DẠNG: Đảm bảo mọi API endpoint đều được bọc trong thẻ `<details>` và `<summary>`. Phải có đúng 1 dòng trống ngay bên dưới thẻ `<summary>` để GitHub render markdown table và code block bên trong được chính xác.

Nếu bạn đã hiểu rõ các mệnh lệnh trên, hãy tiến hành format và trả về cho tôi toàn bộ nội dung file mới!
```