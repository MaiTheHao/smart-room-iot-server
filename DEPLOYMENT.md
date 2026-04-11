## 1. Yêu cầu Hệ thống (Tech Stack)

Hệ thống được xây dựng trên nền tảng Java hiện đại:

- **Runtime**: ![Java 21](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white)
- **Application Server**: ![Tomcat 10.1](https://img.shields.io/badge/Tomcat-10.1-F8DC75?style=flat-square&logo=apachetomcat&logoColor=black)
- **Database**: ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white) ![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=mariadb&logoColor=white)

---

## 2. Quy trình Triển khai lên Tomcat

### 2.1. Chuẩn bị file WAR
Build ứng dụng bằng Maven để tạo file `ROOT.war`:
```bash
mvn clean package
```
File kết quả sẽ nằm tại `target/ROOT.war`.

### 2.2. Triển khai vào Tomcat
1. Xóa thư mục `webapps/ROOT/` và file `webapps/ROOT.war` cũ trong Tomcat.
2. Copy file `ROOT.war` mới vào thư mục `webapps/`.
3. Tomcat sẽ tự động giải nén và triển khai khi khởi động.

---

## 3. Cấu hình Thư viện Server (Tomcat Lib)

Do hệ thống sử dụng **JNDI DataSource**, các thư viện quản lý kết nối và logging PHẢI được đặt trong thư mục `{TOMCAT_HOME}/lib`.

| Phân nhóm | Thư viện đề xuất | Vai trò |
| :--- | :--- | :--- |
| **Pool** | `HikariCP-5.x.jar` | Quản lý kết nối DB |
| **Drivers** | `mysql-connector-j-8.x.jar` | Kết nối MySQL/MariaDB |
| **SLF4J** | `slf4j-api-2.0.x.jar` | Interface logging |
| **Log4j2** | `log4j-api-2.x.jar`, `log4j-core-2.x.jar` | Implementation logging |
| **Bridge** | `log4j-slf4j2-impl-2.x.jar` | Cầu nối Logging |

---

## 4. Hướng dẫn Cấu hình Hệ thống (Developer Guidelines)

Hệ thống cung cấp khả năng tinh chỉnh linh hoạt qua các tệp cấu hình. Dưới đây là các tệp quan trọng nhất:

### 4.1 Cấu hình Container & JNDI (Server Configurations)
Dưới đây là nội dung chi tiết cho các tệp cấu hình tại server (thư mục `conf/` của Tomcat).

#### **A. Tệp `context.xml`**
Tệp này định nghĩa các tài nguyên JNDI (DataSource) và các biến môi trường cho ứng dụng. 
> ⚠️ **QUAN TRỌNG:** Hãy thay đổi các giá trị `value`, `username`, `password` để khớp với cấu hình môi trường thực tế của bạn.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <!-- 1) Tài nguyên theo dõi để tự reload -->
    <WatchedResource>WEB-INF/web.xml</WatchedResource>
    <WatchedResource>WEB-INF/tomcat-web.xml</WatchedResource>
    <WatchedResource>${catalina.base}/conf/web.xml</WatchedResource>

    <!-- 2) Cấu hình JWT -->
    <Environment
        name="iviet.app.jwtSecret"
        value="YOUR_SECRET_JWT_KEY_MIN_64_CHARS"
        type="java.lang.String"
        override="false" />

    <Environment
        name="iviet.app.jwtExpirationMs"
        value="172800000"
        type="java.lang.String"
        override="false" />

    <!-- 3) Cấu hình Hibernate -->
    <Environment name="hibernate.show_sql" value="false" type="java.lang.String" override="false" />
    <Environment name="hibernate.hbm2ddl.auto" value="validate" type="java.lang.String" override="false" />

    <!-- 4) Cấu hình Engine chu kỳ quét (giây) -->
    <Environment name="app.engine.rule.scanIntervalSeconds" value="10" type="java.lang.Integer" override="false" />
    <Environment name="app.engine.telemetry.scanIntervalSeconds" value="10" type="java.lang.Integer" override="false" />

    <!-- 5) JNDI DATASOURCE (Dùng HikariCP) -->
    <!-- Chọn 1 trong 2 Resource bên dưới -->
    
    <!-- MYSQL -->
    <Resource
        name="jdbc/smartroom_db"
        auth="Container"
        type="javax.sql.DataSource"
        factory="com.zaxxer.hikari.HikariJNDIFactory"
        driverClassName="com.mysql.cj.jdbc.Driver"
        jdbcUrl="jdbc:mysql://localhost:3306/smart_room_iot?useSSL=false&amp;serverTimezone=UTC&amp;allowPublicKeyRetrieval=true&amp;useUnicode=true&amp;characterEncoding=UTF-8"
        username="your_username"
        password="your_password"
        maximumPoolSize="30"
        minimumIdle="5"
        connectionTimeout="30000"
        idleTimeout="600000"
        maxLifetime="1800000"
        poolName="SmartRoomHikariPool"
        connectionTestQuery="SELECT 1"
        registerMbeans="true"
    />
</Context>
```

#### **B. Tệp `server.xml`**
Tệp này quản lý cổng kết nối và hiệu năng luồng xử lý của Tomcat.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Server port="8005" shutdown="SHUTDOWN">
    <Service name="Catalina">
        <!-- Connector: Cấu hình Port và Virtual Threads -->
        <Connector connectionTimeout="20000"
                   maxParameterCount="1000"
                   port="8080"
                   protocol="HTTP/1.1"
                   redirectPort="8443"
                   useVirtualThreads="true" />

        <Engine defaultHost="localhost" name="Catalina">
            <Host appBase="webapps" autoDeploy="true" name="localhost" unpackWARs="true">
                <!-- Log truy cập -->
                <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
                       pattern="%h %l %u %t &quot;%r&quot; %s %b" prefix="localhost_access_log"
                       suffix=".txt" />
            </Host>
        </Engine>
    </Service>
</Server>
```

### 4.2 Cấu hình Ứng dụng (App Properties)
- **`application.properties`** [[Link](./src/main/resources/application.properties)]:
  - Cấu hình **CORS** (Allowed Origins).
  - Tối ưu **Hibernate Batching** (`batch_size=50`).
  - Múi giờ hệ thống (`app.timezone`).
- **`config.properties`** [[Link](./src/main/resources/config.properties)]:
  - Danh sách Port nội bộ (Main, Data, Registry).
  - Mapping trạng thái thiết bị và cơ chế điều khiển (GPIO, Bluetooth).

### 4.3 Cấu hình Logging
- **`log4j2.properties`** [[Link](./src/main/resources/log4j2.properties)]:
  - Cấu hình **RollingFile** và log level động qua `APP_LOG_LEVEL`.

---

## 5. Kiểm tra & Xử lý Sự cố (Troubleshooting)

| Phân loại | Lỗi thường gặp / Tình trạng | Giải pháp xử lý |
| :--- | :--- | :--- |
| **Thư viện** | `NoClassDefFoundError: org/slf4j/...` | Kiểm tra lại [Mục 3](#3-cấu-hình-thư-viện-server-tomcat-lib). Đảm bảo đã copy đầy đủ các file JAR vào thư mục `lib` của Tomcat. |
| **Kết nối DB** | `NameNotFoundException: [jdbc/smartroom_db]` | Kiểm tra khai báo `<Resource>` trong `context.xml`. Tên JNDI phải khớp chính xác. |
| **Cấu hình** | Thay đổi trong `context.xml` không có tác dụng | Cần **khởi động lại Tomcat** để các thay đổi trong file cấu hình server được áp dụng. |

---
*Cập nhật lần cuối: 11-Apr-2026*
