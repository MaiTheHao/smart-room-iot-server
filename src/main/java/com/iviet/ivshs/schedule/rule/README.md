# Rule Module (Tự động hóa theo Điều kiện)

## 1. Tổng quan
Module **Rule** quản lý các kịch bản logic dựa trên trạng thái hệ thống. Đây là nơi xử lý các quy tắc "Nếu... thì...", ví dụ: "Nếu nhiệt độ > 30°C thì bật quạt cấp độ 3".

*   **Vai trò của Quartz:** Đảm bảo hệ thống hoạt động liên tục bằng cách kích hoạt một Job quét định kỳ (Global Job).
*   **Job & Processor:**
    *   **RuleEngineJob:** Quét toàn bộ hệ thống định kỳ.
    *   **RuleProcessor:** Đánh giá các điều kiện hiện tại của môi trường/thiết bị và thực thi hành động nếu điều kiện thỏa mãn.

## 2. Đặc điểm cốt lõi: Global Period
Điểm khác biệt quan trọng nhất của Rule so với Automation là cơ chế thời gian "hệ thống":

*   **Thời gian định kỳ (System Period):** Tất cả các Rules đều được kiểm tra trong cùng một chu kỳ thời gian (ví dụ: quét mỗi 5 phút/lần). Thời gian này do hệ thống cấu hình chung (`scanInterval`), người dùng không thể chỉnh sửa riêng lẻ.
*   **Phụ thuộc điều kiện:** Thời gian chạy là cố định, nhưng hành động chỉ xảy ra khi điều kiện (Nhiệt độ, Độ ẩm, Trạng thái khác) được đáp ứng.

**Tóm lại:** Rule là "Kiểm tra điều kiện mỗi X phút, nếu đúng thì làm việc Y".
