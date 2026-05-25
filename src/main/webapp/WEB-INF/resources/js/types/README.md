# Frontend Type Definitions

Thư mục này chứa các file định nghĩa kiểu dữ liệu (`.d.ts`) toàn cục cho môi trường Javascript của Frontend. Do các file này kết thúc bằng đuôi `.d.ts` và nằm trong cấu hình quét của IDE, chúng sẽ tự động được cung cấp dưới dạng kiểu toàn cục mà không cần sử dụng cú pháp `import` trong các file Javascript (`.js`).

## Danh sách các file định nghĩa kiểu

| Tên File | Mô tả | Các kiểu dữ liệu chính |
| :--- | :--- | :--- |
| [common.d.ts](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/webapp/WEB-INF/resources/js/types/common.d.ts) | Các cấu trúc phản hồi chung từ API | `ApiResponse<T>`, `PaginatedResponse<T>` |
| [enums.d.ts](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/webapp/WEB-INF/resources/js/types/enums.d.ts) | Các kiểu Enum tương ứng backend | `MetricDomain`, `DeviceCategory`, `ActuatorPower`, `ActuatorMode`, ... |
| [client.d.ts](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/webapp/WEB-INF/resources/js/types/client.d.ts) | Quản lý người dùng, thiết bị gateway và xác thực | `ClientDto`, `CreateClientDto`, `UpdateClientDto`, `LoginDto`, `JwtResponse` |
| [device.d.ts](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/webapp/WEB-INF/resources/js/types/device.d.ts) | Thông tin thiết bị và cấu trúc lệnh điều khiển | `UnifiedDeviceDto`, `ControlDeviceResult`, các Request Body điều khiển |
| [floor.d.ts](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/webapp/WEB-INF/resources/js/types/floor.d.ts) | Quản lý các tầng lầu | `FloorDto`, `CreateFloorDto`, `UpdateFloorDto` |
| [room.d.ts](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/webapp/WEB-INF/resources/js/types/room.d.ts) | Quản lý các phòng | `RoomDto`, `CreateRoomDto`, `UpdateRoomDto` |
| [rule.d.ts](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/webapp/WEB-INF/resources/js/types/rule.d.ts) | Hệ thống tự động hóa dựa trên kịch bản (Rule) | `RuleDto`, `RuleConditionDto`, `RuleActionDto`, các Create/Update DTO |
| [automation.d.ts](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/webapp/WEB-INF/resources/js/types/automation.d.ts) | Hệ thống tác vụ lập lịch theo Cron | `AutomationDto`, `AutomationActionDto`, các Create/Update DTO |
| [telemetry.d.ts](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/webapp/WEB-INF/resources/js/types/telemetry.d.ts) | Dữ liệu đo đạc (nhiệt độ, điện năng tiêu thụ) | `TemperatureValueDto`, `AverageTemperatureValueDto`, `EnergyMetricDto` |
| [system.d.ts](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/webapp/WEB-INF/resources/js/types/system.d.ts) | Quản lý nhóm phân quyền (RBAC) và kết quả chạy loạt | `SysFunctionDto`, `SysGroupDto`, `BatchOperationResultDto` |

## Hướng dẫn sử dụng trong code Javascript (.js)

Để tận dụng sự hỗ trợ gợi ý code (Intellisense) của VS Code hoặc các IDE khác, sử dụng JSDoc phía trên các tham số hoặc biến:

### 1. Định nghĩa kiểu cho tham số hàm
```javascript
/**
 * Cập nhật trạng thái của thiết bị
 * @param {UnifiedDeviceDto} device 
 */
function updateDeviceStatus(device) {
    console.log(device.power); // Gợi ý tự động các thuộc tính 'ON' | 'OFF'
}
```

### 2. Định nghĩa kiểu cho dữ liệu nhận về từ API
```javascript
/** @type {ApiResponse<AutomationDto>} */
const response = await fetch('/api/automations/1').then(r => r.json());
if (response.status === 200) {
    console.log(response.data.cronExpression);
}
```
