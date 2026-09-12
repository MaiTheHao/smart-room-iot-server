## Mục lục

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)

2. <details><summary><b><a href="#2-kiến-trúc-backend">2. Kiến trúc Backend</a></b></summary>

   - [2.1 Công nghệ sử dụng](#21-công-nghệ-sử-dụng)
   - [2.2 Danh sách package](#22-danh-sách-package)
   - [2.3 Luồng kiến trúc lõi](#23-luồng-kiến-trúc-lõi)
   - [2.4 Cấu hình hệ thống](#24-cấu-hình-hệ-thống)
</details>

3. <details><summary><b><a href="#3-kiến-trúc-frontend">3. Kiến trúc Frontend</a></b></summary>

   - [3.1 Công nghệ sử dụng](#31-công-nghệ-sử-dụng)
   - [3.2 Cơ chế render (SSR + CSR)](#32-cơ-chế-render-ssr--csr)
</details>

4. <details><summary><b><a href="#4-các-luồng-nghiệp-vụ">4. Các luồng nghiệp vụ</a></b></summary>

   - [4.1 Luồng xác thực và Phân quyền (Security & RBAC)](#41-luồng-xác-thực-và-phân-quyền-security--rbac)
   - [4.2 Luồng xử lý API tiêu chuẩn](#42-luồng-xử-lý-api-tiêu-chuẩn)
   - [4.3 So sánh API và View Controller](#43-so-sánh-api-và-view-controller)
   - [4.4 Luồng telemetry (thu thập dữ liệu)](#44-luồng-telemetry-thu-thập-dữ-liệu)
   - [4.5 Luồng điều khiển thiết bị (Strategy Pattern)](#45-luồng-điều-khiển-thiết-bị-strategy-pattern)
   - [4.6 Luồng Rule Engine](#46-luồng-rule-engine)
   - [4.7 Luồng Alert System](#47-luồng-alert-system)
   - [4.8 Luồng Automation Engine](#48-luồng-automation-engine)
   - [4.9 Luồng Energy Metric](#49-luồng-energy-metric)
   - [4.10 Luồng Gateway Integration (Adapter Pattern)](#410-luồng-gateway-integration-adapter-pattern)
</details>

5. <details><summary><b><a href="#5-cấu-trúc-database">5. Cấu trúc Database</a></b></summary>

   - [5.1 Danh sách thực thể (Entity List)](#51-danh-sách-thực-thể-entity-list)
   - [5.2 Phân nhóm dữ liệu nghiệp vụ (Business Grouping)](#52-phân-nhóm-dữ-liệu-nghiệp-vụ-business-grouping)
</details>

---

---

## 1. Tổng quan hệ thống

**Đặc điểm kiến trúc Monolith:** Smart Room Server được xây dựng dựa trên kiến trúc nguyên khối (Monolithic Application). Khối Frontend (giao diện Web Admin) và Khối Backend (xử lý logic, API) **KHÔNG** phải là hai dự án tách rời. Cả hai khối này được đóng gói chung trên cùng một cấu trúc mã nguồn (Codebase) và hoạt động tại một tiến trình Tomcat Server duy nhất.

Toàn bộ các giao tiếp từ ứng dụng Client (Mobile App, Web Browser) và phần cứng IoT (ESP32 Gateway, Raspberry Pi Gateway) đều định tuyến trực tiếp qua Server. Server đảm nhận vai trò trung tâm xử lý logic đồng bộ dữ liệu hai chiều.

### 1.1 Nền tảng công nghệ

| Thành phần | Công nghệ |
| :--- | :--- |
| **Runtime** | ![Java 21](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white) |
| **Server** | ![Tomcat 10.1](https://img.shields.io/badge/Tomcat-10.1-F8DC75?style=flat-square&logo=apachetomcat&logoColor=black) |
| **Database** | ![MySQL 8.0](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white) |
| **Build Tool** | ![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white) |

### 1.2 Sơ đồ kiến trúc tổng thể

```mermaid
graph TD
    subgraph ClientLayer [Lớp Giao diện]
        Mobile([Mobile App / IoT Client])
        Web([Web Browser / Admin])
    end

    subgraph ServerLayer [Smart Room Server - Monolith]
        direction TB
        FESSR(Frontend SSR - Thymeleaf)
        API(REST API - 32 Controllers)
        Core(Service Layer - Business Logic)
        Sch(Quartz Scheduler)
        FESSR <--> Core
        API <--> Core
        Core <--> DB[(MySQL Database)]
    end

    subgraph GatewayLayer [Lớp Gateway - Adapter Pattern]
        ESP32[ESP32 Gateway Adapter]
        RPi[Raspberry Pi Gateway Adapter]
    end

    subgraph ExternalLayer [Tích hợp ngoài]
        FCM[Firebase Cloud Messaging - FCM]
    end

    Mobile <-->|REST API + JWT| API
    Web <-->|HTTP + Session| FESSR
    Sch -->|HTTP Pull Telemetry| GatewayLayer
    Core -->|HTTP Command Control| GatewayLayer
    Core -->|Push Notification| FCM
```

> **Lưu ý về kiến trúc:** `FESSR` (Frontend SSR - Thymeleaf) và `API` (REST API) chạy trong **hai `DispatcherServlet` riêng biệt** — Web Dispatcher (`webDispatcher`, mapping `/*`) và API Dispatcher (`apiDispatcher`, mapping `/api/*`). Mỗi DispatcherServlet có ApplicationContext riêng (child của Root Context), quản lý các bean Controller độc lập. Xem chi tiết tại [2.4 Cấu hình hệ thống](#24-cấu-hình-hệ-thống).

---

## 2. Kiến trúc Backend

Hệ thống sử dụng **Spring Framework 6.2.17 (Custom Spring — Non Boot)** để đảm bảo quyền kiểm soát chi tiết vòng đời khởi tạo của các component.

### 2.1 Công nghệ sử dụng

| Thành phần | Công nghệ |
| :--- | :--- |
| **Framework** | ![Spring 6.2](https://img.shields.io/badge/Spring-6.2.17-green?style=flat-square&logo=spring&logoColor=white) |
| **ORM** | ![Hibernate 6.4](https://img.shields.io/badge/Hibernate-6.4.4-59666C?style=flat-square&logo=hibernate&logoColor=white) ![Spring Data JPA 3.5](https://img.shields.io/badge/Spring_Data_JPA-3.5.10-blue?style=flat-square) |
| **Security** | ![Spring Security 6.4](https://img.shields.io/badge/Spring_Security-6.4.13-70B060?style=flat-square&logo=springsecurity&logoColor=white) |
| **Scheduler** | ![Quartz 2.5](https://img.shields.io/badge/Quartz-2.5.2-white?style=flat-square&logo=quartz&logoColor=black) |
| **Serialization** | ![Jackson 2.18](https://img.shields.io/badge/Jackson-2.18.2-blue?style=flat-square) ![JJWT 0.11](https://img.shields.io/badge/JJWT-0.11.5-blueviolet?style=flat-square) |
| **Rate Limiting** | ![Bucket4j 8.10](https://img.shields.io/badge/Bucket4j-8.10.1-red?style=flat-square) |
| **HTTP Client** | ![HttpClient5](https://img.shields.io/badge/Apache_HttpClient5-5.2.3-green?style=flat-square) |
| **Cache** | ![Caffeine](https://img.shields.io/badge/Caffeine-3.1.8-yellow?style=flat-square) |
| **Push Notification** | ![Firebase](https://img.shields.io/badge/Firebase_Admin-9.9.0-FFCA28?style=flat-square&logo=firebase&logoColor=black) |
| **Logging** | ![Log4j 2.25](https://img.shields.io/badge/Log4j-2.25.4-orange?style=flat-square) ![SLF4J 2.0](https://img.shields.io/badge/SLF4J-2.0.16-blue?style=flat-square) |
| **Utilities** | ![Lombok 1.18](https://img.shields.io/badge/Lombok-1.18.30-blue?style=flat-square) ![MapStruct 1.5](https://img.shields.io/badge/MapStruct-1.5.5-orange?style=flat-square) ![AspectJ 1.9](https://img.shields.io/badge/AspectJ-1.9.21-purple?style=flat-square) ![Commons Lang3](https://img.shields.io/badge/Commons_Lang3-3.14.0-lightgrey?style=flat-square) |

### 2.2 Danh sách package

| Package | Vai trò |
|--------|--------|
| `core/config` | Cấu hình hệ thống: Application, DataSource, Security, MVC, Quartz, Async, Firebase, RestClient |
| `core/component` | Component hỗ trợ: AutowiringSpringBeanJobFactory, SpringSecurityAuditorAware |
| `core/properties` | Application properties binding: Database, Engine, Firebase, Gateway, HttpClient, Jwt, Security, Token |
| `core/startup` | Khởi tạo dữ liệu và scheduler khi Servlet khởi động |
| `controller/api/v1` | **32 REST Controller** (Action, AirCondition, Alert, Auth, Automation, Client, ClientDevice, Condition, DeviceMetadata, Fan, Floor, HardwareConfig, HealthCheck, Language, Light, Metric, PowerConsumption, PublicApi, Room, RoomEvent, RoomEventMaster, Rule, SensorEvent, SensorMetadata, SensorTelemetry, Setup, SysFunction, SysGroup, SysRole, Telemetry, Temperature, TemperatureValue) |
| `controller/view` | **6 View Controller** (Index, Login, Management, Room, SmartSystem, ViewJS) |
| `service/*` | Xử lý logic nghiệp vụ (57 interface + 68 implementation): device control (light, fan, aircondition), sensor & metric (temperature, powerconsumption, co2, humidity, lux, motion), alert & notification, automation, rule/condition/action, room/floor/roomevent, client/clientdevice, system (sysgroup, sysfunction, sysrole, permission), telemetry, metric, auth/token, view (index, login, room), i18n, setup, hardwareconfig |
| `service/registry` | Registry tra cứu strategy: ConditionDataSourceRegistry, DeviceControlStrategyRegistry, DeviceStateStrategyRegistry, SensorStateStrategyRegistry, NotificationStrategyRegistry, EventTelemetryStrategyRegistry, TelemetryCRUDStrategyRegistry, TokenRegistry |
| `service/strategy` | Strategy interface: DeviceControlServiceStrategy, ConditionDataSourceStrategy, ConditionEvaluationService, ActionExecutionService, DeviceStateStrategy, SensorStateStrategy, NotificationStrategy, MetricServiceStrategy, TelemetryCRUDServiceStrategy, TokenStrategy, ... |
| `dao` | 38 DAO interface tương tác database qua Spring Data JPA |
| `dao/base` | Base DAO hierarchy: BaseDao → BaseEntityDao → (BaseAuditEntityDao, BaseTranslatableEntityDao, BaseIoTEntityDao, BaseIoTActuatorDao, BaseIoTSensorDao, BaseTelemetryDao) |
| `dao/setup` | Device Setup Strategy Pattern: AbstractDeviceSetupStrategy, DeviceSetupOrchestrator, + 9 implementations (Light, Fan, AirCondition, Temperature, PowerConsumption, Co2, Humidity, Lux, MotionDetector) |
| `entities` | 53+ Entity classes, base classes, composite keys, JPA converters |
| `dto` | 136+ DTO classes: Request/Response, ViewModel, ApiResponse, PaginatedResponse, sealed interfaces (`SensorSpecificData`, `DeviceSpecificData`) và data records (`TemperatureSensorData`, `PowerConsumptionSensorData`, `Co2SensorData`, `HumiditySensorData`, `LuxSensorData`, `MotionDetectorData`, `LightData`, `FanData`, `AirConditionData`) |
| `mapper` | MapStruct interfaces: CreateMapper, UpdateMapper, BaseMapper (`mapper/base`) |
| `integration/gateway` | Gateway Adapter Pattern: GatewayAdapter interface, GatewayAdapterRegistry, GatewayCommand, GatewayFetchResult, GatewayOperationResult |
| `integration/gateway/base` | BaseGatewayClient — logic HTTP client dùng chung cho các Gateway |
| `integration/gateway/impl/esp32` | ESP32 Gateway implementation: Esp32GatewayAdapter, Esp32BaseClient, Esp32AuthClient, Esp32TelemetryClient, Esp32LightControlClient, Esp32FanControlClient, Esp32AcControlClient, Esp32SystemClient |
| `integration/gateway/impl/raspi` | Raspberry Pi Gateway implementation: RaspiGatewayAdapter, RaspiAuthClient, RaspiTelemetryClient, RaspiDeviceControlClient, RaspiLightControlClient, RaspiFanControlClient, RaspiAcControlClient, RaspiSystemClient, RaspiMaintenanceClient |
| `integration/gateway/interceptor` | GatewayAuthInterceptor, TraceForwardingInterceptor |
| `scheduler/system/telemetry` | TelemetryJob, TelemetryProcessor — thu thập dữ liệu định kỳ |
| `scheduler/system/metric` | Metric system: `energy/` (EnergyMetricTelemetryJob, EnergyMetricResetJob, EnergyMetricJobProvider), `status/` (DeviceStatusMetricJob, DeviceStatusMetricJobProvider), MetricJobProvider, MetricJobRegistration |
| `scheduler/dynamic/base` | Generic job framework: GenericSchedulableJob, SchedulableJobProcessor, JobProcessorFactory, JobProcessorType |
| `scheduler/dynamic/rule` | RuleProcessor — xử lý đánh giá Condition → Action (dùng `service/strategy` + `service/registry`) |
| `scheduler/dynamic/automation` | AutomationProcessor — xử lý tác vụ tự động hóa theo cron |
| `scheduler/dynamic/automation/strategy` | AutomationActionStrategy + 3 impl (Light, Fan, AirCondition) |
| `scheduler` | TraceJobListener — gắn Trace ID vào Quartz job |
| `event` | Application event: EventTelemetryApplicationEvent, RoomEventApplicationEvent, RoomMotionDetectedEvent |
| `shared/constant` | Hằng số hệ thống: AppConstant, I18nMessageConstant |
| `shared/enumeration` | 32 Enum classes: DeviceCategory, SensorCategory, ConditionOperator, ConditionLogic, ConditionDataSource, RuleDataSource, ConditionOwnerCategory, ActionOwnerCategory, AlertActionType, AlertActorType, AlertNamespace, AlertStatus, NotificationChannel, MetricDomain, TokenType, ClientType, Platform, Severity, GatewayCommand, ActuatorMode, ActuatorPower, ActuatorSwing, DeviceControlType, DeviceSpecificType, EnergyMetricCategory, JobActionType, JobTargetType, RoomEventCode, TelemetryTimeGroup,... |
| `shared/exception` | 13 Custom exceptions + Global exception handlers (Api, Web, Integration, Persistence, RestTemplateResponseErrorHandler) |
| `shared/filter` | JwtAuthenticationFilter, RateLimitingFilter (Bucket4j), RequestTraceFilter |
| `shared/security` | AuthEntryPointJwt, AuthErrorHandler, AuthenticationSuccessListener, JwtUtils |
| `shared/logging` | RestRequestLoggingAspect, ViewRequestLoggingAspect, TraceLogger |
| `shared/util` | Utilities: Calculator, CronExpressionUtil, DeviceCapabilityRegistry, FunctionCodeHelper, JsonUtil, LocalContextUtil, MdcTaskWrapper, RequestContextUtil, SecurityContextUtil |
| `shared/web` | GlobalModelAttributes — attributes gắn vào mọi View |

### 2.3 Luồng kiến trúc lõi

Hệ thống tuân thủ nghiêm ngặt kiến trúc phân tầng (Layered Architecture). Controller không được truy cập trực tiếp tới tầng DAO.

```mermaid
flowchart LR
    Client([Client]) <--> Controller
    Controller <--> Service
    Service <--> DAO
    DAO <--> DB[(Database)]
```

> **Ghi chú:** `Controller` trong sơ đồ trên thực tế được phân tách thành **hai tầng Controller riêng biệt**, mỗi tầng thuộc một `DispatcherServlet` context khác nhau: **API Controller** (`@RestController`, xử lý `/api/*`) thuộc API Dispatcher Context và **View Controller** (`@Controller`, xử lý `/*`) thuộc Web Dispatcher Context — xem chi tiết tại [2.4 Cấu hình hệ thống](#24-cấu-hình-hệ-thống).

### 2.4 Cấu hình hệ thống

Hệ thống sử dụng kiến trúc **Double DispatcherServlet Context** — hai `DispatcherServlet` độc lập chạy trong cùng một ứng dụng, chia sẻ `Root ApplicationContext` chung. Cấu trúc này được khai báo tại:

- **`SmrcApplication.java`**: Lớp khởi tạo ứng dụng, implements `WebApplicationInitializer`. Đây là entry point của toàn bộ ứng dụng, chịu trách nhiệm thiết lập hệ thống phân cấp (hierarchical) gồm 3 ApplicationContext:

  | Context | DispatcherServlet | Mapping | Config class | Vai trò |
  |---------|-------------------|---------|-------------|--------|
  | **Root Context** | — (không có servlet) | — | `ApplicationConfig`, `AsyncConfig`, `DataSourceConfig`, `WebSecurityConfig`, `QuartzSchedulerConfig`, `RestClientConfig`, `FirebaseSDKConfig` | Chứa các bean chung (Service, DAO, Security, Scheduler, RestClient, Firebase) — được kế thừa bởi cả hai child context |
  | **API Dispatcher Context** | `apiDispatcher` | `/api/*` | `WebMvcApiConfig` | Xử lý REST API (JSON), `@RestController`, Jackson serialization |
  | **Web Dispatcher Context** | `webDispatcher` | `/*` | `WebMvcViewConfig` | Xử lý View SSR (Thymeleaf), `@Controller`, multipart upload, resource handler |

  **Cơ chế hoạt động:**
  - Hai `DispatcherServlet` hoạt động như hai Spring context riêng biệt, mỗi context quản lý các bean Controller riêng.
  - Cả hai đều là **child context** của `Root Context` — có thể truy cập tất cả bean ở Root (Service, DAO, Security,…).
  - Bean khai báo trong API Context **không thể** truy cập từ Web Context và ngược lại — giúp cách ly hoàn toàn tầng Controller.
  - Filters được đăng ký ở cấp `ServletContext`: Encoding (UTF-8), RequestTrace, Spring Security Chain, Rate Limiting — hoạt động trên **mọi request** trước khi đến DispatcherServlet.

  ▸ **File:** `src/main/java/com/iviet/ivshs/SmrcApplication.java`

- **`ApplicationConfig.java`**: Cấu hình gốc của ứng dụng — `@EnableJpaAuditing`, `@EnableAspectJAutoProxy`, `ComponentScan` (loại trừ controller), `ObjectMapper` (UTC timezone, JavaTimeModule), `MessageSource` i18n, JNDI property source cho profile prod.
- **`WebSecurityConfig.java`**: Khai báo **hai SecurityFilterChain** với `@Order(1)` và `@Order(2)`:
  - `apiFilterChain` (`/api/**`): Stateless, JWT authentication, CORS enabled, CSRF disabled.
  - `webFilterChain` (catch-all): Stateful form login, Remember-Me với `JdbcTokenRepositoryImpl`.
- **`WebMvcViewConfig.java`**: Cấu hình Thymeleaf ViewResolver, resource handlers (`/css/**`, `/js/**`, `/imgs/**`, ...), error pages (`/error/401`, `/error/403`, `/error/404`, `/error/500`), MultipartResolver.
- **`WebMvcApiConfig.java`**: Cấu hình Jackson `MappingJackson2HttpMessageConverter`, `@ComponentScan` cho controller API và exception handlers.
- **`DataSourceConfig.java`**: DataSource (JNDI + JDBC fallback), EntityManagerFactory, JdbcTemplate, PlatformTransactionManager, Hibernate properties.
- **`QuartzSchedulerConfig.java`**: `SchedulerFactoryBean` với JDBC JobStore, virtual threads executor, `AutowiringSpringBeanJobFactory`, `TraceJobListener`.
- **`RestClientConfig.java`**: 4 `RestTemplate` beans riêng biệt (default, GatewayControl, GatewayTelemetry, GatewayApiClient) với Apache HC5 connection pooling và `TraceForwardingInterceptor`.
- **`FirebaseSDKConfig.java`**: Cấu hình `FirebaseApp` và `FirebaseMessaging` cho FCM push notifications.
- **`AsyncConfig.java`**: `@EnableAsync` với virtual threads `TaskExecutorAdapter`.

---

## 3. Kiến trúc Frontend

Mã nguồn Frontend (HTML, JS, CSS) được tích hợp trong cùng môi trường ứng dụng của Server tại `./src/main/webapp/WEB-INF`.

### 3.1 Công nghệ sử dụng

| Thành phần | Công nghệ |
| :--- | :--- |
| **Template Engine** | ![Thymeleaf 3.1](https://img.shields.io/badge/Thymeleaf-3.1.3-005F0F?style=flat-square&logo=thymeleaf&logoColor=white) |
| **Layout & UI** | ![AdminLTE 4.0](https://img.shields.io/badge/AdminLTE-4.0.0-blueviolet?style=flat-square) ![Bootstrap 5.3](https://img.shields.io/badge/Bootstrap-5.3.2-563D7C?style=flat-square&logo=bootstrap&logoColor=white) ![Lucide Icons](https://img.shields.io/badge/Lucide_Icons-lightgrey?style=flat-square) ![OverlayScrollbars](https://img.shields.io/badge/OverlayScrollbars-blue?style=flat-square) |
| **Interactivity** | ![SweetAlert2 11.26](https://img.shields.io/badge/SweetAlert2-11.26-F8BB86?style=flat-square) ![Flatpickr 4.6.13](https://img.shields.io/badge/Flatpickr-4.6.13-orange?style=flat-square) |
| **Visualization** | ![ApexCharts 5.11](https://img.shields.io/badge/ApexCharts-5.11-FF6384?style=flat-square) ![Tabulator 6.4](https://img.shields.io/badge/Tabulator-6.4-F5F5F5?style=flat-square) |

**Lưu ý:** Hệ thống **KHÔNG** sử dụng jQuery, Chart.js hay DataTables như phiên bản tài liệu cũ. Toàn bộ JavaScript được viết bằng **Vanilla JS** với cú pháp **ES Module** (`import`/`export`).

### 3.2 Cơ chế render (SSR + CSR)
Quá trình phân giải UI được kết hợp từ hai cơ chế:
- **Server-Side Rendering (SSR)**: Controller gọi render file HTML. Mã nguồn xử lý ghép View với Layout Dialect (Thymeleaf Layout Dialect 4.0) hỗ trợ thiết lập template lặp lại như Header, Sidebar. Phản hồi hoàn thiện từ Backend gửi kèm Model Attribute.
- **Client-Side Rendering (CSR)**: Script JS khởi chạy Dynamic DOM. Khi gọi module Charts (ApexCharts) hoặc Table (Tabulator), JS gọi HTTP Request đến REST API lấy dữ liệu JSON (`/api/v1/*`) để render components mà không cần reload View chính.

---

## 4. Các luồng nghiệp vụ

### 4.1 Luồng xác thực và Phân quyền (Security & RBAC)

Hệ thống cung cấp cơ chế bảo mật khép kín thông qua mô hình phân tầng: Auth Filter (xác thực danh tính) và RBAC (kiểm soát quyền truy cập).

**A. Cơ cấu Security FilterChains**
Hệ thống cấu hình **hai luồng Security FilterChain độc lập** (đánh thứ tự bằng `@Order`):
- **RESTful API (`apiFilterChain`)** — `@Order(1)`: Định tuyến Request có tiền tố `/api/**`. Middleware `JwtAuthenticationFilter` bóc tách JSON Web Token qua Header `Authorization`. Session policy `SessionCreationPolicy.IF_REQUIRED` (JWT không lưu server-side nhưng vẫn cho phép session khi cần), vô hiệu hóa CSRF, bật CORS. Các endpoint `/api/v1/auth/signin`, `/api/v1/auth/signup` và `/api/v1/public/**` được public. Ngoài ra còn có `RateLimitingFilter` (Bucket4j) và `RequestTraceFilter` hoạt động ở tầng filter.
- **SSR Web (`webFilterChain`)** — `@Order(2)`: Stateful cho Web Admin Dashboard. Xác thực qua Spring Form Login (`/login` → `/loginAction`), quản lý session qua Cookie `JSESSIONID`. Hỗ trợ Remember-Me với `JdbcTokenRepositoryImpl` lưu token dưới Database.

**B. Mô hình phân quyền RBAC (Role-Based Access Control)**
Hệ thống quản lý quyền truy cập qua 3 cấu trúc cốt lõi:

- **Group (Nhóm người dùng - `SysGroup`)**: Định nghĩa User thuộc nhóm nào (Admin `G_ADMIN`, User `G_USER`). Khai báo trong `SysGroupEnum`.
- **Function (Quyền thao tác - `SysFunction`)**: Định nghĩa hành động được phép (sửa thiết bị `F_MANAGE_DEVICE`, xem phòng `F_ACCESS_ROOM_ALL`). Khai báo trong `SysFunctionEnum`.
- **Role (Bảng trung gian - `SysRole`)**: Map quan hệ giữa Group và Function.

```mermaid
sequenceDiagram
    autonumber
    participant App as Client (Mobile/Web)
    participant Ctrl as AuthController
    participant Svc as AuthServiceImpl
    participant Filter as JwtAuthenticationFilter
    participant Ctx as SecurityContext
    
    App->>Ctrl: POST /api/v1/auth/signin
    Ctrl->>Svc: Validate Username & Password
    Svc-->>Ctrl: Tạo JWT Token
    Ctrl-->>App: HTTP 200 + Token payload
    
    App->>Filter: Call API (Gắn Bearer Token)
    activate Filter
    Filter->>Filter: Verify JWT signature
    Filter->>Ctx: Load quyền từ DB (Client → Group → Function)
    Filter->>Ctx: Nạp List<SysFunction> vào Security Context
    Filter->>Ctrl: Cho phép Request xuống Controller
    deactivate Filter
```

### 4.2 Luồng xử lý API tiêu chuẩn

Cấu trúc luân chuyển dữ liệu theo chuẩn 3 lớp Spring Framework.

```mermaid
sequenceDiagram
    autonumber
    participant App as Client Request
    participant Ctrl as REST Controller
    participant Svc as Logic Service
    participant Dao as DAO Repository
    
    App->>Ctrl: Request GET /api/v1/*
    activate Ctrl
    Ctrl->>Svc: Gọi phương thức Service
    activate Svc
    Svc->>Dao: Tương tác DAO Repository
    Dao-->>Svc: Query Entity Data
    Svc->>Svc: MapStruct Entity → DTO
    Svc-->>Ctrl: DTO đã đóng gói
    deactivate Svc
    Ctrl-->>App: JSON Response
    deactivate Ctrl
```

### 4.3 So sánh API và View Controller

| Đặc tả | RESTful API Controller (`api.v1.*`) | View Controller (`view.*`) |
| ----------------------- | ---------------------------------------------------------- | ----------------------------------------------------------- |
| **Annotation** | `@RestController` | `@Controller` |
| **Payload** | JSON Object | Tên template Thymeleaf + Model |
| **Input Data** | `@RequestBody` JSON mapping | `Model` attribute (Spring View) |

### 4.4 Luồng telemetry (thu thập dữ liệu)

Hệ thống thiết lập cơ chế xử lý qua Quartz Job Schedule để tự động lấy data từ Gateway.

```mermaid
sequenceDiagram
    autonumber
    participant Quartz as Quartz Scheduler
    participant Job as TelemetryJob
    participant Processor as TelemetryProcessor
    participant Svc as TelemetryService
    participant Gateway as ESP32 / RPi Gateway
    participant Strategy as SensorTelemetryOrchestratorService
    
    Quartz->>Job: Khởi chạy Trigger (execute)
    activate Job
    Job->>Processor: processAllGateways()
    activate Processor
    Processor->>Svc: takeGlobalTelemetry()
    activate Svc

    loop Duyệt danh sách Gateway
        Svc->>Gateway: HTTP GET /telemetry
        Gateway-->>Svc: Response JSON
        Svc->>Svc: Parse → List<TelemetryResponseDto>
        Svc->>Strategy: Orchestrate sensor data storage
        activate Strategy
        Strategy->>Strategy: Strategy Pattern per sensor type
        deactivate Strategy
    end

    deactivate Svc
    deactivate Processor
    deactivate Job
```

### 4.5 Luồng điều khiển thiết bị (Strategy Pattern)

Sử dụng Design Strategy Pattern để giảm phụ thuộc logic xử lý Controller, dễ dàng thao tác xuống từng chuẩn Interface Implementation của Category (Quạt, Đèn, Điều hòa).

```mermaid
sequenceDiagram
    autonumber
    participant App as API/UI Call
    participant Ctrl as Device Controller
    participant Strategy as DeviceControlServiceStrategy
    participant SvcImpl as Specific Handler (Fan, Light, AC)
    
    App->>Ctrl: PUT /api/v1/devices/{id}/control
    activate Ctrl
    Ctrl->>Strategy: control()
    activate Strategy
    Strategy->>SvcImpl: Route theo DeviceCategory
    activate SvcImpl
    SvcImpl->>SvcImpl: Tạo JSON payload → gửi Gateway
    SvcImpl-->>Strategy: Result
    deactivate SvcImpl
    Strategy-->>Ctrl: Ack
    deactivate Strategy
    Ctrl-->>App: HTTP 200/204
    deactivate Ctrl
```

### 4.6 Luồng Rule Engine

Hệ thống Rule Engine đóng vai trò nòng cốt xử lý công việc tự động qua nguyên tắc quét **Điều kiện (Condition)** và gọi **Hành động (Action)**.

- **Khối đối chiếu Condition:** `RuleProcessor` ủy quyền cho `ConditionEvaluationService.evaluateAll()`; service này tra `ConditionDataSourceRegistry` để lấy `ConditionDataSourceStrategy` tương ứng và gọi `fetchValue()` (nhiệt độ, độ ẩm, trạng thái thiết bị, giờ hệ thống...). Các datasource hiện có: Device, Room, Sensor, System. So sánh số thập phân dùng ngưỡng cấu hình `app.engine.rule.computeEpsilon` (mặc định 0.05). Hỗ trợ kết hợp AND/OR.
- **Khối kết xuất Action:** Khi thỏa mãn, `ActionExecutionService` thực thi các action qua `DeviceControlServiceStrategy` để biến đổi Param thành Object payload động gọi lệnh điều khiển phần cứng. Ngoài ra `RuleProcessor` có thể kích hoạt Alert qua `AlertTriggerService` cho các `AlertConfig` gắn với Rule.

```mermaid
sequenceDiagram
    autonumber
    participant Job as GenericSchedulableJob (Quartz)
    participant Factory as JobProcessorFactory
    participant Processor as RuleProcessor
    participant Eval as ConditionEvaluationService
    participant Registry as ConditionDataSourceRegistry
    participant Action as ActionExecutionService
    participant AlertSvc as AlertTriggerService

    Job->>Factory: getProcessor(RULE)
    Factory->>Processor: RuleProcessor
    Job->>Processor: processJob(ruleId)
    activate Processor
    Processor->>Processor: Load Rule + Conditions + Actions từ DB
    alt Rule isActive
        Processor->>Eval: evaluateAll(conditions, null)
        activate Eval
        loop Xử lý Conditions
            Eval->>Registry: getStrategy(sourceCategory)
            Registry-->>Eval: ConditionDataSourceStrategy
            Eval->>Eval: fetchValue() + so sánh (computeEpsilon)
        end
        Eval-->>Processor: EvaluationResult
        deactivate Eval
        alt isMatched == true
            Processor->>Action: executeAll(actions)
            activate Action
            Action->>Action: DeviceControlServiceStrategy control(deviceId, params)
            Action-->>Processor: List<ActionResult>
            deactivate Action
            Processor->>AlertSvc: Trigger alert (nếu có AlertConfig)
        end
    end
    deactivate Processor
```

### 4.7 Luồng Alert System

Hệ thống Alert quản lý vòng đời cảnh báo từ cấu hình → kích hoạt → ghi nhật ký → thông báo.

```mermaid
sequenceDiagram
    autonumber
    participant Source as Alert Source (Rule/System/Gateway)
    participant Trigger as AlertTriggerService
    participant Config as AlertConfig
    participant Instance as AlertInstance
    participant Log as AlertInstanceLog
    participant Notif as NotificationService
    
    Source->>Trigger: alertTriggerService.trigger(request)
    activate Trigger
    Trigger->>Config: Kiểm tra AlertConfig (namespace, source)
    Trigger->>Instance: Tạo AlertInstance (status: ACTIVE)
    Trigger->>Log: Ghi AlertInstanceLog
    Trigger->>Notif: Gửi thông báo
    
    alt NotificationStrategy == FCM
        Notif->>FCM: Firebase Cloud Messaging
    else Email
        Notif->>Email: SMTP
    else SMS
        Notif->>SMS: Gateway SMS
    end
    
    deactivate Trigger
```

### 4.8 Luồng Automation Engine

Automation Engine cho phép lên lịch các tác vụ tự động theo cron, khác với Rule Engine ở chỗ nó hoạt động theo thời gian biểu thay vì đánh giá điều kiện.

```mermaid
sequenceDiagram
    autonumber
    participant Job as GenericSchedulableJob
    participant Proc as AutomationProcessor
    participant Auto as Automation + Actions
    participant Strategy as AutomationActionStrategy
    participant Ctrl as DeviceControlServiceStrategy
    
    Job->>Proc: processJob(id)
    activate Proc
    Proc->>Auto: Load Automation & Actions từ DB
    Proc->>Strategy: Resolve strategy theo DeviceCategory
    activate Strategy
    Strategy->>Ctrl: control(deviceId, params)
    deactivate Strategy
    deactivate Proc
```

### 4.9 Luồng Energy Metric & Device Status Backup

#### 4.9.1 Energy Metric

Hệ thống theo dõi điện năng tiêu thụ với hai tác vụ Quartz:

| Job | Mô tả | Cron |
|:---|:---|:---|
| `EnergyMetricTelemetryJob` | Thu thập chỉ số điện năng từ Gateway, tính toán consumption (kW/h) | Mỗi 5 phút |
| `EnergyMetricResetJob` | Reset chỉ số hàng ngày, lưu snapshot vào bảng `energy_metrics` | 00:00 hàng ngày |

#### 4.9.2 Device Status Backup

`DeviceStatusMetricJob` (Quartz, chạy mỗi **200 giây** — cấu hình `app.engine.metric_status.intervalSeconds`) thu thập trạng thái hoạt động của các thiết bị actuator (đèn, quạt, điều hòa) và lưu vào bảng `device_status_metrics` dưới dạng JsonNode. Job chỉ backup các thiết bị actuator, không bao gồm cảm biến.

**Kiến trúc xử lý dữ liệu business:**

Hệ thống sử dụng sealed interface hierarchy để đồng bộ hóa cách trích xuất dữ liệu business từ entity (Job hiện chỉ dùng nhánh `DeviceSpecificData`):

```
BaseIoTEntity.extractBusinessData() → Object
├── BaseIoTSensor.extractBusinessData() → SensorSpecificData (sealed)
│   ├── Temperature          → TemperatureSensorData(currentValue)
│   ├── PowerConsumption    → PowerConsumptionSensorData(currentWatt)
│   ├── Co2Sensor           → Co2SensorData(currentCo2)
│   ├── HumiditySensor      → HumiditySensorData(currentHumidity)
│   ├── LuxSensor           → LuxSensorData(currentLux)
│   └── MotionDetector      → MotionDetectorData(motionDetected)
└── BaseIoTDevice.extractBusinessData() → DeviceSpecificData (sealed)
    ├── Light               → LightData(power, level)
    ├── Fan                 → FanData(power, speed, duration, mode, swing, light)
    └── AirCondition        → AirConditionData(power, temperature, mode, fanSpeed, swing, duration)
```

**Luồng xử lý `DeviceStatusMetricJob`:**

```mermaid
sequenceDiagram
    participant Job as DeviceStatusMetricJob (Quartz)
    participant Svc as DeviceStatusMetricServiceImpl
    participant Entity as BaseIoTDevice (Light/Fan/AC)
    participant DTO as DeviceStatusMetricDto
    participant Dao as DeviceStatusMetricDao

    Job->>Svc: backupDeviceStatuses()
    activate Svc

    loop For each category (LIGHT, FAN, AIR_CONDITION)
        Svc->>Dao: findAllLatestForEachDevice()
        Dao-->>Svc: Latest version map

        Svc->>Entity: findAllActive()
        activate Entity
        Svc->>Entity: extractBusinessData()
        Entity-->>Svc: DeviceSpecificData (typed record)
        deactivate Entity

        Svc->>DTO: businessDataToJsonNode(businessData, objectMapper)
        activate DTO
        DTO->>DTO: mapper.valueToTree(businessData)
        DTO-->>Svc: JsonNode
        deactivate DTO

        Svc->>Svc: createMetricEntity(category, id, timestamp, jsonNode, version)
    end

    Svc->>Dao: save(metricsToSave)
    deactivate Svc
```

**Điểm nổi bật của kiến trúc:**

- **Generic processor**: Một method `processCategory()` duy nhất xử lý tất cả category thiết bị, loại bỏ hoàn toàn code hardcode build ObjectNode thủ công.
- **Type safety**: Mỗi entity trả về đúng kiểu dữ liệu business của nó thông qua sealed interface (compile-time check).
- **DTO chịu trách nhiệm convert**: `DeviceStatusMetricDto.businessDataToJsonNode()` dùng `ObjectMapper.valueToTree()` để chuyển đổi POJO → JsonNode — tách biệt hoàn toàn khỏi entity và service.
- **Dễ mở rộng**: Thêm device type mới chỉ cần tạo record implement `DeviceSpecificData` + implement `extractBusinessData()` — không cần sửa service.

**Backward compatibility:** JSON output giữ nguyên format (field names, null exclusion, enum serialization) so với code hardcode trước đây.

### 4.10 Luồng Gateway Integration (Adapter Pattern)

Hệ thống sử dụng **Adapter Pattern** để trừu tượng hóa giao tiếp với các nền tảng Gateway khác nhau.

```mermaid
classDiagram
    class GatewayAdapter {
        <<interface>>
        +getSupportedType() ClientType
        +login(ip, LoginDto) ResponseEntity
        +fetchSetup(ip) ResponseEntity
        +fetchHealthCheck(ip) GatewayOperationResult
        +controlDevice(ip, GatewayCommand) GatewayOperationResult
        +fetchEnergyMetric(ip, GatewayCommand) GatewayFetchResult
        +fetchGlobalTelemetry(ip) GatewayFetchResult
        +resetEnergy(ip, GatewayCommand) GatewayOperationResult
    }
    
    class Esp32GatewayAdapter {
        +esp32HttpClient
        +getSupportedType()
        +controlDevice()
        +fetchGlobalTelemetry()
    }
    
    class RaspiGatewayAdapter {
        +raspiHttpClient
        +getSupportedType()
        +controlDevice()
        +fetchGlobalTelemetry()
    }
    
    class GatewayAdapterRegistry {
        +get(ClientType) GatewayAdapter
    }
    
    GatewayAdapter <|.. Esp32GatewayAdapter
    GatewayAdapter <|.. RaspiGatewayAdapter
    GatewayAdapterRegistry --> GatewayAdapter
```

Mỗi Gateway Adapter triển khai các client con riêng:
- **ESP32**: BaseClient, AuthClient, TelemetryClient, LightControlClient, FanControlClient, AcControlClient, SystemClient
- **Raspberry Pi**: AuthClient, TelemetryClient, DeviceControlClient, LightControlClient, FanControlClient, AcControlClient, MaintenanceClient, SystemClient

---

## 5. Cấu trúc Database

Hệ thống không sử dụng khóa ngoại RDBMS truyền thống mà tổ chức theo cụm Logic Nghiệp vụ, giúp mở rộng không giới hạn quy mô.

### 5.1 Danh sách thực thể (Entity List)

Toàn bộ entity được tổ chức theo nhóm nghiệp vụ:

### 5.2 Phân nhóm dữ liệu nghiệp vụ (Business Grouping)

**1. Nhóm Địa điểm (Locations):**
- **Bảng:** `floor`, `floor_lan`, `room`, `room_lan`
- **Nghiệp vụ:** Xây dựng sơ đồ cây không gian Tầng → Phòng

**2. Nhóm Thiết bị điều khiển (Devices):**
- **Bảng:** `light`, `light_lan`, `fan`, `fan_lan`, `air_condition`, `air_condition_lan`
- **Nghiệp vụ:** Mỗi thiết bị actuator có bảng riêng; liên kết vật lý (GPIO, BLE MAC, API endpoint) nằm ở `hardware_config`. Không còn bảng `device_metadata` — `DeviceMetadataDao` chỉ là DAO tổng hợp đếm thiết bị theo phòng.
- **Các thực thể thiết bị chuyên biệt:** `Light`, `Fan`, `AirCondition` kế thừa `BaseIoTDevice`

**3. Nhóm Cảm biến & Dữ liệu (Sensors & Logs):**
- **Bảng cảm biến:** `temperature`, `temperature_lan`, `power_consumption`, `power_consumption_lan`, `co2_sensor`, `co2_sensor_lan`, `humidity_sensor`, `humidity_sensor_lan`, `lux_sensor`, `lux_sensor_lan`, `motion_detector`, `motion_detector_lan`
- **Bảng dữ liệu đo:** `temperature_value`, `temperature_metrics`, `energy_metrics`, `device_status_metrics`, `co2_metrics`, `humidity_metrics`, `lux_metrics`, `motion_metrics`
- **Nghiệp vụ:** `device_status_metrics` lưu trạng thái thiết bị actuator dạng JsonNode theo thời gian, phục vụ giám sát lịch sử hoạt động.
- **Nghiệp vụ:** Tách biệt "Trạng thái hiện tại" và "Lịch sử dữ liệu". Dữ liệu lịch sử dạng **Append-only**.

**4. Nhóm Tự động hóa (Rules & Automation):**
- **Bảng:** `rule`, `condition`, `action`, `automation`, `automation_action`
- **Nghiệp vụ:** Từ migration `V6`, hai bảng `rule_condition`/`rule_action` được thay bằng bảng polymorphic `condition`/`action` (dùng chung cho nhiều owner, phân biệt qua `owner_category`).

**5. Nhóm Alert & Notification:**
- **Bảng:** `alert_config`, `alert_config_group`, `alert_instance`, `alert_instance_group`, `alert_instance_log`
- **Nghiệp vụ:** Quản lý vòng đời cảnh báo: Config (ngưỡng) → Instance (sự kiện) → Log (nhật ký). Nhóm `_group` phục vụ phân quyền alert.

**6. Nhóm Room Event:**
- **Bảng:** `room_event`, `room_event_config`
- **Nghiệp vụ:** Cấu hình và nhật ký sự kiện theo phòng (ví dụ phát hiện chuyển động), liên kết với cảm biến.

**7. Nhóm Metadata & Hardware:**
- **Bảng:** `hardware_config`, `client_device`, `client_group`
- **Nghiệp vụ:** Cấu hình phần cứng Gateway/thiết bị và thiết bị client đã đăng ký. Sensor/Device metadata hiện là DTO tổng hợp (`SensorMetadataDao`, `DeviceMetadataDao`), không phải bảng riêng.

**8. Nhóm Người dùng & Bảo mật (Users & RBAC):**
- **Bảng:** `client`, `client_group`, `sys_group`, `sys_group_lan`, `sys_function`, `sys_function_lan`, `sys_role`, `persistent_logins`
- **Nghiệp vụ:** `client` dùng chung cho tài khoản người dùng và định danh Gateway. Phân quyền Group → Function → Role.

**9. Nhóm Hệ thống hỗ trợ (Support & Infrastructure):**
- **Bảng:** `language`, `QRTZ_*` (Quartz Scheduler tables)
- **Nghiệp vụ:** Đa ngôn ngữ UI, lịch trình Quartz. `persistent_logins` (Remember-Me) nằm ở nhóm Người dùng & Bảo mật.

**Chi tiết cấu trúc bảng và migration:** Xem các file SQL tại [infra/database/](./infra/database/) bao gồm init, migration và seed scripts.
