# MÔ TẢ LUỒNG XỬ LÝ NGOẠI LỆ

Hệ thống xử lý ngoại lệ được tổ chức theo cơ chế phân tầng dựa trên mức độ ưu tiên và loại đối tượng yêu cầu (API hoặc Web).

### 1. Phân tầng theo mức độ ưu tiên (@Order)

Khi có ngoại lệ phát sinh, Spring sẽ duyệt qua các Handler theo thứ tự từ thấp đến cao:

-   **Tầng 1 - Dữ liệu (Persistence):** Bắt các lỗi liên quan đến Database (SQL, giao dịch dữ liệu) đầu tiên để bảo vệ thông tin hạ tầng.
-   **Tầng 2 - Tích hợp (Integration):** Bắt các lỗi kết nối thiết bị IoT hoặc dịch vụ bên thứ ba.
-   **Tầng 3 - Tổng thể (Global):** Chốt chặn cuối cùng cho các lỗi nghiệp vụ và lỗi hệ thống chưa xác định.

### 2. Luồng rẽ nhánh theo loại Request

Tại tầng xử lý cuối cùng, hệ thống phân tách phản hồi dựa trên URL của yêu cầu:

#### Đối với Request từ Giao diện (Web UI)

-   Ngoại lệ được chuyển đến `WebGlobalExceptionHandler`.
-   Handler kiểm tra URL không thuộc mẫu `/api/`.
-   Hệ thống trả về `ModelAndView` tương ứng với các trang lỗi HTML (404, 403, 500) nằm trong thư mục `/WEB-INF/views/error/`.

#### Đối với Request từ API (Mobile/IoT)

-   Ngoại lệ đi vào `WebGlobalExceptionHandler` trước.
-   Do URL chứa `/api/`, Handler này thực hiện **nhường quyền** bằng cách trả về `null`.
-   Spring tiếp tục chuyển ngoại lệ sang `ApiGlobalExceptionHandler`.
-   Dữ liệu lỗi được đóng gói vào cấu trúc `ApiResponseV1` và trả về định dạng JSON cho client.

---
