# Scheduler

Package này chứa logic scheduler cho Smart Room IoT Server. Để duy trì một clean architecture, các scheduling jobs được chia thành hai namespaces riêng biệt dựa trên bản chất và vòng đời của chúng:

## 1. Dynamic (`com.iviet.ivshs.scheduler.dynamic`)
**User-Defined Entity Jobs**
- Chứa logic cho các jobs được schedule, reschedule, và unschedule một cách dynamic bởi người dùng hệ thống.
- Các jobs này được hỗ trợ bởi các database entities (ví dụ: `Automation`, `Rule`).
- Sử dụng các design patterns Template Method và Factory (gói `base`) để định tuyến một Quartz Job chung duy nhất (`GenericSchedulableJob`) đến các Processors cụ thể dựa trên loại entity.

## 2. System (`com.iviet.ivshs.scheduler.system`)
**Static Backend Jobs**
- Chứa logic cho các background tasks thiết yếu cho hoạt động của hệ thống và được schedule tĩnh khi khởi động ứng dụng.
- Các jobs này không tương ứng với các entities do người dùng tạo ra.
- Ví dụ bao gồm:
  - `metric`: Các background jobs định kỳ để lấy dữ liệu metric từ các thiết bị hoặc thực hiện reset năng lượng hàng ngày.
  - `telemetry`: Các tasks tổng hợp dữ liệu telemetry toàn cục.
- Các jobs này được đăng ký tự động bởi các Initializers (ví dụ: `MetricSystemInitializer`) thông qua interface `MetricJobProvider`.

## Guidelines for Developers
- **Không trộn lẫn các domains**: Nếu bạn đang thêm một tính năng mới hướng tới người dùng chạy theo lịch trình, hãy đặt processor trong `dynamic`. Nếu bạn đang thêm một background cron job hệ thống, hãy đặt nó trong `system`.
- **Không bỏ qua các Initializers**: Khi thêm các System jobs mới, hãy tạo một `JobProvider` mới thay vì đăng ký job thủ công với Quartz.

