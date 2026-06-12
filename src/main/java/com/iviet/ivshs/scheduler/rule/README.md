# Rule Module (Tự động hóa theo Điều kiện)

## 1. Tổng quan
Khác với các kịch bản chạy ngay lập tức, module **Rule (Quy tắc tự động)** giúp hệ thống liên tục giám sát và tự động đưa ra các phản hồi phù hợp với môi trường thực tế dựa trên nguyên lý **"Nếu... Thì..."**.

*Ví dụ:* "Nếu nhiệt độ phòng vượt quá 30°C **thì** tự động bật quạt máy."

## 2. Đặc điểm cốt lõi

*   **Chu kỳ kiểm tra linh hoạt cho từng quy tắc:** Mỗi quy tắc có thể thiết lập tần suất tự kiểm tra riêng biệt. Ví dụ, một quy tắc giám sát an toàn (rò rỉ khí gas, báo cháy) cần kiểm tra liên tục mỗi 5 giây, trong khi quy tắc tưới cây chỉ cần tự động chạy kiểm tra một lần mỗi ngày vào lúc 8h sáng.
*   **Chỉ hành động khi đủ điều kiện:** Khi đến chu kỳ kiểm tra, hệ thống sẽ xem xét các thông số đo được (như nhiệt độ, ánh sáng, trạng thái thiết bị). Nếu các thông số này khớp với điều kiện đã đặt, hành động (bật/tắt thiết bị, gửi cảnh báo) mới được kích hoạt.

**Tóm lại:** Rule là tính năng giúp thiết bị tự động "tự kiểm tra điều kiện sau mỗi khoảng thời gian đã hẹn trước, và tự xử lý nếu điều kiện đó đúng".
