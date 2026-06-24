# Thay đổi về API Alert & Các biến Template cho Rule Engine

Tài liệu này tóm tắt các thay đổi và bổ sung đối với hệ thống API Alert, bao gồm nâng cấp các bộ lọc tìm kiếm (Criteria DTOs), bổ sung API đếm số lượng (Count API), và các biến truyền vào `messageTemplate` của Alert Config kích hoạt bởi **Rule Engine** (`namespace: RULE`).

---

## 1. Thay đổi API Alert (Nâng cấp Criteria & Endpoint Count)

Nhằm tối ưu hóa hiệu năng và thống nhất trải nghiệm cho phía client, chúng tôi đã tái cấu trúc và bổ sung các DTO tiêu chí lọc chung cho các API Danh sách và API Đếm (Count).

### 1.1 Quản lý Cấu hình Cảnh báo (Alert Config)
Sử dụng chung tiêu chí lọc `AlertConfigFilterDto`:
*   `namespace` (AlertNamespace)
*   `alertCode` (String)
*   `sourceId` (String)
*   `page` (int, mặc định `0`)
*   `size` (int, mặc định `10`)

**Các Endpoint cập nhật/mới:**
*   `GET /v1/alerts`: Lấy danh sách cấu hình (hỗ trợ lọc phân trang đầy đủ theo `AlertConfigFilterDto`).
*   `GET /v1/alerts/count`: *(Mới)* Đếm số lượng cấu hình dựa trên tiêu chí lọc `AlertConfigFilterDto` (bỏ qua page/size).

### 1.2 Lấy Danh sách Sự kiện Cảnh báo theo Config (Alert Instance)
Sử dụng chung tiêu chí lọc `AlertInstanceSubFilterDto`:
*   `status` (AlertStatus)
*   `severity` (Severity)
*   `from` (Instant - định dạng ISO Date-time)
*   `to` (Instant - định dạng ISO Date-time)
*   `page` (int, mặc định `0`)
*   `size` (int, mặc định `10`)

**Các Endpoint cập nhật/mới:**
*   `GET /v1/alerts/{alertConfigId}/instances`: Lấy danh sách các sự kiện thuộc một cấu hình (hỗ trợ lọc phân trang theo `AlertInstanceSubFilterDto`).
*   `GET /v1/alerts/{alertConfigId}/instances/count`: *(Mới)* Đếm số lượng sự kiện thuộc cấu hình đó dựa trên `AlertInstanceSubFilterDto` (bỏ qua page/size).

### 1.3 Lịch sử Logs của Sự kiện (Alert Instance Logs)
Sử dụng chung tiêu chí lọc `AlertInstanceLogFilterDto`:
*   `actionType` (AlertActionType)
*   `actorType` (AlertActorType)
*   `page` (int, mặc định `0`)
*   `size` (int, mặc định `10`)

**Các Endpoint cập nhật/mới:**
*   `GET /v1/alerts/{alertConfigId}/instances/{instanceId}/logs`: Lấy lịch sử log của một sự kiện dưới dạng **phân trang** (thay vì list thô như cũ, sử dụng `AlertInstanceLogFilterDto`).
*   `GET /v1/alerts/{alertConfigId}/instances/{instanceId}/logs/count`: *(Mới)* Đếm tổng số log dựa trên tiêu chí lọc `AlertInstanceLogFilterDto` (bỏ qua page/size).

---

## 2. Các biến bổ sung trong `messageTemplate`

Khi một quy tắc (Rule) thỏa mãn điều kiện và kích hoạt cảnh báo, ngoài các biến cũ, hệ thống hỗ trợ thêm các biến ngữ cảnh tự động sau:

| Tên biến | Kiểu dữ liệu | Mô tả |
| :--- | :--- | :--- |
| `total_conditions` | Integer | **[Mới]** Tổng số điều kiện (conditions) được cấu hình trong quy tắc |
| `cond{sortOrder}_operator` | String | **[Mới]** Toán tử so sánh thực tế của điều kiện tương ứng (ví dụ: `>`, `<`, `=`, `>=`, `<=`, `!=`) |

---

## 3. Chi tiết ví dụ sử dụng Template

Giả sử chúng ta có một quy tắc (Rule) với thông tin như sau:
*   **ID**: `10`
*   **Tên**: `AUTORULE_TEMP`
*   **Danh sách điều kiện** (gồm 2 điều kiện):
    1.  Điều kiện 0 (`sortOrder = 0`): Nhiệt độ phòng (`ROOM_TEMP`) **>** `30`
    2.  Điều kiện 1 (`sortOrder = 1`): Trạng thái điều hòa (`AC_POWER`) **==** `OFF`

### Bộ dữ liệu ngữ cảnh (`templateData`) sinh ra tại Runtime:
```json
{
  "rule_id": 10,
  "rule_name": "AUTORULE_TEMP",
  "total_conditions": 2,
  
  "cond0_value": 35.5,
  "cond0_threshold": "30",
  "cond0_operator": ">",
  
  "cond1_value": "OFF",
  "cond1_threshold": "OFF",
  "cond1_operator": "="
}
```

### Ví dụ cấu hình `messageTemplate`:
```text
Quy tắc {{rule_name}} (gồm {{total_conditions}} điều kiện) đã kích hoạt! Điều kiện đầu tiên vi phạm khi giá trị đo được {{cond0_value}} {{cond0_operator}} ngưỡng {{cond0_threshold}}.
```

### Nội dung tin nhắn thực tế (`body` của Alert Instance):
```text
Quy tắc AUTORULE_TEMP (gồm 2 điều kiện) đã kích hoạt! Điều kiện đầu tiên vi phạm khi giá trị đo được 35.5 > ngưỡng 30.
```

---

## 4. Các file thay đổi liên quan
*   **Controller**: [AlertController.java](../../src/main/java/com/iviet/ivshs/controller/api/v1/AlertController.java)
*   **DTO Filters**:
    *   [AlertConfigFilterDto.java](../../src/main/java/com/iviet/ivshs/dto/alert/AlertConfigFilterDto.java)
    *   [AlertInstanceSubFilterDto.java](../../src/main/java/com/iviet/ivshs/dto/alert/AlertInstanceSubFilterDto.java)
    *   [AlertInstanceLogFilterDto.java](../../src/main/java/com/iviet/ivshs/dto/alert/AlertInstanceLogFilterDto.java)
*   **Services**:
    *   [AlertConfigService.java](../../src/main/java/com/iviet/ivshs/service/alert/AlertConfigService.java) / [AlertConfigServiceImpl.java](../../src/main/java/com/iviet/ivshs/service/alert/impl/AlertConfigServiceImpl.java)
    *   [AlertInstanceService.java](../../src/main/java/com/iviet/ivshs/service/alert/AlertInstanceService.java) / [AlertInstanceServiceImpl.java](../../src/main/java/com/iviet/ivshs/service/alert/impl/AlertInstanceServiceImpl.java)
    *   [AlertInstanceLogService.java](../../src/main/java/com/iviet/ivshs/service/alert/AlertInstanceLogService.java) / [AlertInstanceLogServiceImpl.java](../../src/main/java/com/iviet/ivshs/service/alert/impl/AlertInstanceLogServiceImpl.java)
*   **DAOs**:
    *   [AlertConfigDao.java](../../src/main/java/com/iviet/ivshs/dao/AlertConfigDao.java)
    *   [AlertInstanceDao.java](../../src/main/java/com/iviet/ivshs/dao/AlertInstanceDao.java)
    *   [AlertInstanceLogDao.java](../../src/main/java/com/iviet/ivshs/dao/AlertInstanceLogDao.java)
*   **Rule Engine**: [RuleProcessor.java](../../src/main/java/com/iviet/ivshs/scheduler/dynamic/rule/RuleProcessor.java)
*   **Tài liệu API chính thức**: [alert.md](../../doc/api/alert.md)

---

## 5. Điều chỉnh bắt buộc của tham số `channels` và `recipientGroupCodes` trong Alert Config

- **Mục tiêu**: Điều chỉnh để tham số `channels` (các kênh gửi tin nhắn cảnh báo) và `recipientGroupCodes` (nhóm nhận tin cảnh báo) không còn bắt buộc khi tạo hoặc cập nhật cấu hình cảnh báo. Cả hai tham số có thể không chọn (chấp nhận giá trị `null` hoặc danh sách rỗng).
- **Chi tiết thay đổi**:
  - Gỡ bỏ annotation `@NotEmpty` đối với các trường `channels` và `recipientGroupCodes` trong DTO tạo cấu hình cảnh báo: [CreateAlertConfigDto](../../src/main/java/com/iviet/ivshs/dto/alert/CreateAlertConfigDto.java).
  - Gỡ bỏ annotation `@NotEmpty` đối với các trường `channels` và `recipientGroupCodes` trong DTO cập nhật cấu hình cảnh báo: [UpdateAlertConfigDto](../../src/main/java/com/iviet/ivshs/dto/alert/UpdateAlertConfigDto.java).
  - Cho phép danh sách rỗng hoặc `null` cho nhóm người nhận trong logic nghiệp vụ bằng cách sửa phương thức `validateGroupCodes` trong [AlertConfigServiceImpl](../../src/main/java/com/iviet/ivshs/service/alert/impl/AlertConfigServiceImpl.java) để trả về sớm thay vì ném lỗi `BadRequestException`.
  - Cập nhật tài liệu API tại [alert.md](../../doc/api/alert.md) để đánh dấu các tham số này có tính chất bắt buộc là **Không** thay vì **Có**.

