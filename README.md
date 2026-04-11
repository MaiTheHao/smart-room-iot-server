<div align="center">

# SMART ROOM IoT SERVER

**An Advanced Orchestrator for Building & Device Management**

[![Java Version](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Version](https://img.shields.io/badge/Spring-6.x-green?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/)
[![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

---

### _"The brain behind your building's infrastructure"_

_A high-performance stateless orchestrator designed to bridge the gap between human management and sensor-driven automation._

</div>

---

> 📖 **Tài Liệu Đặc Tả Kiến Trúc (Architecture Specification):**  
> Để xem phân tích chuyên sâu về luồng hệ thống, sơ đồ tuần tự (Sequence Diagram), chi tiết Package và thiết kế Frontend/Backend, xin vui lòng xem file đặc tả kỹ thuật chính tại **👉 [SYSTEM.md](./SYSTEM.md)**.

---

## 1. Project Overview (Tổng quan dự án)

**Smart Room IoT Server** là hệ thống Web nguyên khối (Monolith) chạy trên Spring Framework. Nền tảng đóng vai trò cung cấp giao diện quản trị trung tâm và điều phối dữ liệu với phần cứng qua bộ API REST chuẩn hóa.

Hệ thống được thiết kế theo nguyên tắc **"Quản lý tập trung - Thực thi phân tán"**, đóng vai trò là "Bộ não trung tâm" chỉ đạo, nhưng không can thiệp sâu vào các tác vụ đòi hỏi thời gian thực (Real-time Mils) tại cấp độ thiết bị biên (Edge Devices). Toàn bộ hạ tầng có thể quản lý sâu theo cấu trúc: **Building (Tòa nhà) → Floor (Tầng) → Room (Phòng)**.

## 2. Architecture Principles (Nguyên lý kiến trúc)

### 2.1 Stateless Orchestrator
Máy chủ hoạt động ưu tiên cơ chế Stateless để tối ưu bộ nhớ băng thông:
- **Độc lập:** Lệnh điều khiển không cần khởi tạo lại connection (Thông qua quy trình xác thực JWT).
- **Ủy quyền:** Server chỉ mang trách nhiệm quản lý Rule Automation và lưu trữ thông số. Việc bật/tắt relay chính xác giao toàn quyền cho Gateway Local đảm bảo độ tin cậy.

### 2.2 Hardware Agnostic Design
- **Tách biệt Logic & Vật lý:** Tầng lập trình tại Server chỉ tương tác với các Class hướng đối tượng (`Light`, `Fan`, `Temperature Sensor`) một cách “mù lòa” với cấu trúc phần cứng. 
- **Cơ chế Mapping:** Việc thiết bị kết nối bằng Bluetooth, Zigbee hay GPIO đều được xử lý và phiên dịch tại Gateway, giúp Backend sống bền vững kể cả khi thay máu hạ tầng phần cứng.

### 2.3 Data Normalization
- **Tính trọn vẹn:** Các chuẩn dữ liệu truyền về như Nhiệt độ, Báo điện đều được Data Normalize quy hoạch chung 1 format tại DAO Layer, tôn trọng tuyệt đối Timestamp được dán nhãn từ dưới Hardware gửi lên để chống lỗi dội mạng (Network lag).

## 3. System Capabilities (Chức năng cốt lõi)

### Security & Authentication
- Tách biệt kiểm soát Session cho người dùng Dashboard Web bằng Cookie và Token Bearer đối với thiết bị App/Gateway.
- Áp dụng triệt để nền tảng **Role-Based Access Control (RBAC)** với cơ chế mapping User vào cấu trúc `SysGroup` gắn liền với vô hạn `SysFunction`.

### Telemetry & Automation
- **Mô hình Pull (Chủ động Lên lịch):** Tự động gửi Dispatch lấy số đo môi trường định kỳ qua bộ lập lịch (Quartz Job Scheduler).
- Bộ máy **Rule Engine V2** liên tục kiểm tra trạng thái lệch chuẩn so với dung sai `EPSILON` để kích hoạt thiết bị chạy tự động hoàn toàn lập trình độc lập.

## 4. Database Organization (Phân nhóm cấu trúc DB)

Hệ thống SQL được tổ chức theo tính chất dòng đời dữ liệu:

| Nhóm dữ liệu            | Nhóm bảng đại diện                                             | Đặc điểm truy xuất                                                      |
| ----------------------- | -------------------------------------------------------------- | ----------------------------------------------------------------------- |
| **Hạ tầng & Vị trí**    | `floor`, `room`                                                | *Static* - Ít thay đổi, quy hoạch khung xương cấp cao.                  |
| **Thiết bị & Cấu hình** | `client`, `device_control`, `light`, `fan`, `power_consumption`| *Configuration* - Cấu trúc đối tượng điều hướng vật lý ở dưới Gateway.  |
| **Dữ liệu cảm biến**    | `temperature_value`, `power_consumption_value`                 | *Append-only* - Trữ lượng chuỗi thời gian, INSERT 1 chiều, cấm xóa.     |
| **Tầng phân quyền**     | `client` (User), `sys_group`, `sys_role`, `sys_function`       | *Administrative* - Ràng buộc phân hạn quản trị Web.                     |
