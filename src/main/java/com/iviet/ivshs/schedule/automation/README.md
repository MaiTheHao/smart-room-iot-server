# Automation Module (Tự động hóa theo Lịch trình)

## 1. Tổng quan
Module **Automation** quản lý các tác vụ được kích hoạt dựa trên thời gian thực. Đây là nơi xử lý các kịch bản như "Bật đèn lúc 7:00 sáng mỗi ngày" hoặc "Tắt máy lạnh vào cuối tuần".

*   **Vai trò của Quartz:** Đóng vai trò là bộ định thời gian chính xác. Mỗi Automation được người dùng tạo ra sẽ tương ứng với một Trigger riêng biệt trong Quartz Scheduler.
*   **Job & Processor:**
    *   **AutomationJob:** Nhận tín hiệu từ Quartz khi đến giờ hẹn.
    *   **AutomationProcessor:** Thực thi hành động cụ thể (gửi lệnh xuống thiết bị, gửi thông báo) ngay tại thời điểm đó.

## 2. Đặc điểm cốt lõi: Custom Time
Điểm khác biệt lớn nhất của Automation so với Rule là cơ chế kích hoạt thời gian:

*   **Thời gian tùy chỉnh (Custom Cron):** Mỗi Automation hoạt động độc lập với một biểu thức thời gian (Cron Expression) riêng biệt do người dùng cài đặt.
*   **Cá nhân hóa:** Hệ thống không quy định khi nào chạy, mà hoàn toàn phụ thuộc vào lịch trình cụ thể của từng hành động.

**Tóm lại:** Automation là "Làm việc X vào đúng giờ Y".
