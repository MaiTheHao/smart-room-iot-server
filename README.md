<div align="center">

# SMART ROOM IoT SERVER

**An Advanced Orchestrator for Building & Device Management**

[![Java Version](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Version](<https://img.shields.io/badge/Spring-6.x_(Custom)-green?style=for-the-badge&logo=spring>)](https://spring.io/)
[![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)

---

### "The brain behind your building's infrastructure"

_A high-performance server-side orchestrator designed to bridge the gap between human management and sensor-driven automation._

</div>

## Overview

**Smart Room IoT Server** là ứng dụng web Spring MVC (WAR) cung cấp giao diện quản lý trung tâm cho hệ thống điều khiển thông minh.

Hệ thống quản lý thực thể trực tiếp từ cấp độ **Floor** (tầng) và **Room** (phòng), đại diện cho toàn bộ hạ tầng của một **Building** (tòa nhà). Ứng dụng cung cấp:

-   Quản lý và giám sát thiết bị IoT cấp độ phòng
-   Điều khiển thiết bị thông qua giao diện web và API
-   Quản lý quyền truy cập cho cả người dùng và hardware gateway
-   Theo dõi mức tiêu thụ năng lượng từ các thiết bị
-   Ghi log toàn bộ hoạt động và sự kiện hệ thống

## Tech Stack

### Backend & Core

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Tomcat](https://img.shields.io/badge/Tomcat-F8DC75?style=for-the-badge&logo=apache&logoColor=black)

-   **Java 21 LTS:** Dựa trên Java 21 LTS cho hiệu năng và tính ổn định cao.
-   **Spring Framework 6.1.x:** Sử dụng Spring Framework trực tiếp với Spring MVC, Spring Data JPA, Spring Security.
-   **Spring Security:** Xác thực và phân quyền người dùng, hỗ trợ JWT.
-   **Apache Tomcat 10.1:** Servlet container để chạy ứng dụng web.
-   **Hibernate 6.4.x:** ORM framework để quản lý dữ liệu.

### Frontend (Server Side Rendering)

![HTML5](https://img.shields.io/badge/html5-%23E34F26.svg?style=for-the-badge&logo=html5&logoColor=white)
![AdminLTE](https://img.shields.io/badge/AdminLTE-3c8dbc?style=for-the-badge&logo=adminlte&logoColor=white)
![JavaScript](https://img.shields.io/badge/javascript-%23F7DF1E.svg?style=for-the-badge&logo=javascript&logoColor=black)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)

-   **AdminLTE 3:** Giao diện Dashboard quản trị với thiết kế responsive và hiện đại.
-   **Thymeleaf:** Hỗ trợ template engine hiện đại cho Spring MVC, dễ dàng tích hợp và mở rộng giao diện.
-   **JavaScript:** Hỗ trợ tương tác động trên client-side.

### Database

![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)

-   **MySQL:** Lưu trữ dữ liệu quan hệ chặt chẽ giữa các cấp độ Building -> Floor -> Room -> Device.

## Tính Năng Chính

### Xác Thực & Phân Quyền (RBAC)

-   Đăng nhập/đăng xuất với xác thực người dùng
-   Phân quyền dựa trên vai trò (Role-Based Access Control) cho cả **User** (người dùng) và **Hardware Gateway** (thiết bị điều khiển)
-   Quản lý quyền truy cập chi tiết trên từng tầng (Floor) và phòng (Room) thông qua hệ thống sys_group và sys_function tập trung
-   Kiểm soát quyền quản lý cho từng loại tài nguyên (tầng, phòng, thiết bị) một cách độc lập

### Quản Lý Hạ Tầng

-   Quản lý tầng (Floor) - phân chia không gian trong tòa nhà
-   Quản lý phòng (Room) - định vị chi tiết các phòng trên mỗi tầng
-   Quản lý thiết bị (Device) - theo dõi và kiểm soát các thiết bị IoT

### Điều Khiển Thiết Bị

-   Hỗ trợ đa phương thức điều khiển (Multi-protocol Control) thông qua lớp **Device Control**:
    -   **GPIO:** Điều khiển trực tiếp phần cứng tại chỗ
    -   **BLE (Bluetooth Low Energy):** Điều khiển thiết bị không dây
    -   **Web API:** Tích hợp hệ thống từ xa qua API

### Giám Sát & Theo dõi Thiết bị

-   **Light (Điều khiển ánh sáng):** Bật/tắt và điều chỉnh độ sáng
-   **Temperature (Cảm biến nhiệt độ):** Theo dõi nhiệt độ môi trường
-   **Power Consumption (Giám sát điện năng):** Theo dõi mức tiêu thụ năng lượng của từng thiết bị

### Tính Năng Khác

-   Hỗ trợ đa ngôn ngữ (Tiếng Việt, Tiếng Anh)
-   Ghi log tất cả các hoạt động quan trọng
-   Kiểm tra trạng thái sức khoẻ của hệ thống

## Key Features

-   **Hierarchical Management:** Quản lý cấu trúc Building → Floor → Room → Device.
-   **Device Control:** Điều khiển thiết bị thông qua giao diện web (bật/tắt, điều chỉnh cài đặt).
-   **Real-time Monitoring:** Theo dõi trạng thái thiết bị và cảm biến qua dashboard.
-   **Energy Monitoring:** Theo dõi mức tiêu thụ năng lượng của các thiết bị.
-   **User Authentication & Authorization:** Xác thực người dùng, phân quyền truy cập dựa trên vai trò.
-   **Multi-language Support:** Hỗ trợ đa ngôn ngữ (Tiếng Việt, Tiếng Anh).
-   **Logging & Audit:** Ghi log các hoạt động và sự kiện quan trọng.

---

<p align="center">
  Developed with ❤️
</p>
