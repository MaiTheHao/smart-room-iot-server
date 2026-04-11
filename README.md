<div align="center">

# SMART ROOM IoT SERVER

**Nền tảng Điều phối Quản trị Tòa nhà & Thiết bị IoT Tiên tiến**

[![Phiên bản Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Phiên bản Spring](https://img.shields.io/badge/Spring-6.x-green?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/)
[![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)

---

### _"Nền tảng IoT Plug-and-Play Đích thực"_

_Tối ưu, Sạch và Được thiết kế để Mở rộng Dễ dàng._

</div>

---

## 1. Project Overview (Tổng quan dự án)

**Smart Room IoT Server** là hệ thống Web nguyên khối (Monolith) chạy trên Spring Framework (Custom Spring 6 - Non Boot). Nền tảng đóng vai trò cung cấp giao diện quản trị trung tâm và điều phối dữ liệu với phần cứng qua bộ API REST chuẩn hóa.

**Đặc điểm kiến trúc Monolith:** Hệ thống được xây dựng trên kiến trúc nguyên khối, trong đó khối Frontend (Web Admin) và Backend (Logic, API) được đóng gói chung trên cùng một cấu trúc mã nguồn và hoạt động tại một tiến trình Tomcat Server duy nhất. Toàn bộ giao tiếp từ ứng dụng Client và phần cứng IoT Gateway đều định tuyến trực tiếp qua Server để xử lý logic đồng bộ hai chiều.

Hệ thống được thiết kế theo nguyên tắc **"Quản lý tập trung - Thực thi phân tán"**, đóng vai trò là "Bộ não trung tâm" chỉ đạo, nhưng không can thiệp sâu vào các tác vụ đòi hỏi thời gian thực (Real-time Mils) tại cấp độ thiết bị biên (Edge Devices). Toàn bộ hạ tầng có thể quản lý sâu theo cấu trúc: **Building (Tòa nhà) → Floor (Tầng) → Room (Phòng)**.

### Công nghệ cốt lõi (Core Tech Stack):

| Phân hệ | Công nghệ sử dụng |
| :--- | :--- |
| **Backend** | ![Java 21](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white) ![Spring 6](https://img.shields.io/badge/Spring-6.2-green?style=flat-square&logo=spring&logoColor=white) ![Hibernate](https://img.shields.io/badge/Hibernate-6.4-59666C?style=flat-square&logo=hibernate&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6.4-70B060?style=flat-square&logo=springsecurity&logoColor=white) ![Quartz](https://img.shields.io/badge/Quartz-2.5-white?style=flat-square&logo=quartz&logoColor=black) |
| **Frontend** | ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-005F0F?style=flat-square&logo=thymeleaf&logoColor=white) ![AdminLTE](https://img.shields.io/badge/AdminLTE-3.2-blueviolet?style=flat-square&logo=adminlte&logoColor=white) ![Bootstrap](https://img.shields.io/badge/Bootstrap-4.6-563D7C?style=flat-square&logo=bootstrap&logoColor=white) ![jQuery](https://img.shields.io/badge/jQuery-3.7-0769AD?style=flat-square&logo=jquery&logoColor=white) ![Chart.js](https://img.shields.io/badge/Chart.js-4.4-FF6384?style=flat-square&logo=chartdotjs&logoColor=white) ![SweetAlert2](https://img.shields.io/badge/SweetAlert2-11.2-F8BB86?style=flat-square&logo=sweetalert2&logoColor=white) |
| **Hạ tầng** | ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white) ![Apache Tomcat](https://img.shields.io/badge/Tomcat-10.1-F8DC75?style=flat-square&logo=apachetomcat&logoColor=black) |

---

## 2. Core Capabilities (Chức năng cốt lõi)

Hệ thống tập trung vào việc cung cấp một nền tảng vận hành IoT ổn định và dễ mở rộng với các nghiệp vụ chính:

### Quản lý Hạ tầng & Thiết bị (Plug-and-Play Management)
- **Quản lý phân cấp**: Tổ chức hạ tầng theo cấu trúc chuyên sâu: **Tòa nhà → Tầng → Phòng**, giúp quản lý hàng ngàn thiết bị một cách khoa học.
- **Định nghĩa thiết bị linh hoạt**: Hỗ trợ đa dạng chủng loại thiết bị (Đèn, Quạt, Điều hòa, Cảm biến môi trường) với cơ chế cấu hình linh hoạt.

### Giám sát & Thu thập dữ liệu (Telemetry & Monitoring)
- **Thu thập tự động**: Tích hợp Quartz Job Scheduler để tự động quét và thu thập dữ liệu từ các Gateway định kỳ.
- **Thời gian thực**: Theo dõi biến động nhiệt độ, độ ẩm và điện năng tiêu thụ ngay lập tức trên giao diện quản trị AdminLTE.

### Điều khiển & Tự động hóa (Control & Automation)
- **Điều khiển từ xa**: Gửi lệnh bật/tắt và điều chỉnh thông số thiết bị tức thì qua REST API.
- **Tự động hóa tác vụ**: Tận dụng sức mạnh của **Quartz Scheduler framework** để triển khai các engine phục vụ tự động hóa tác vụ và vận hành các quy luật hệ thống (Rules) một cách chính xác, linh hoạt.

### Bảo mật & Phân quyền (Security & RBAC)
- **Bảo mật đa tầng**: Sử dụng JWT Token cho các ứng dụng Client/IoT và Session-based cho Dashboard Admin.
- **Kiểm soát truy cập (RBAC)**: Hệ thống phân quyền dựa trên Group và Function, đảm bảo mỗi người dùng chỉ được phép tiếp cận đúng các tính năng và dữ liệu tương ứng.

## 4. Tài liệu liên quan (Reference Files)

- **Chi tiết kỹ thuật:** [SYSTEM.md](./SYSTEM.md) - Phân tích đặc tả kiến trúc, sơ đồ thực thể và quy trình nghiệp vụ.
- **Hướng dẫn vận hành:** [DEPLOYMENT.md](./DEPLOYMENT.md) - Quy trình triển khai, cấu hình môi trường và quản lý thư viện server.

