# Luồng UI/UX Tạo Rule cho Hệ thống IoT

Dưới đây là mô tả chi tiết luồng UI/UX để người dùng tạo Rule. Vui lòng đối chiếu với [Rule API Documentation](../api/rule.md) để đảm bảo các tên field chính xác.

## Phần 1: Thiết lập Target Device & Action

### 1.1 Chọn Danh mục Thiết bị (Target Device Category)
- User chọn `targetDeviceCategory` (ví dụ: `LIGHT`, `AIR_CONDITION`, `FAN`)
- Chỉ tập trung triển khai: **Light**, **Fan**, **Air Condition** (PowerConsumption và Temperature tạm thời chưa triển khai)

### 1.2 Chọn Thiết bị Cụ thể (Target Device)
- UI hiển thị danh sách các thiết bị thuộc category đã chọn **trong phòng được chọn** (`roomId`)
- User click chọn thiết bị → lưu `deviceId` vào field `targetDeviceId`

### 1.3 Thiết lập Hành động (Action Parameters)
- **Quan trọng**: `actionParams` phải là **JSON string hợp lệ** và khớp 100% với schema của API `/control` của thiết bị tương ứng
- UI render các option điều khiển tương ứng từng category:
  - **AIR_CONDITION**: `power`, `temp`, `mode`, `fan_speed`, `swing`
  - **LIGHT**: `power`, `level`
  - **FAN**: `power`, `mode`, `speed`, `swing`, `light`
- User điền các tham số, hệ thống tạo JSON string hợp lệ và lưu vào `actionParams`

---

## Phần 2: Thiết lập Condition (Điều kiện kích hoạt)

### 2.1 Thêm Điều kiện
- UI cần nút **"Thêm Condition"** để người dùng có thể thêm múi điều kiện
- Mỗi lần thêm, `sortOrder` mặc định = `max(sortOrder_hiện_tại) + 1`

### 2.2 Thay đổi Thứ tự Condition
- UI cho phép user **thay đổi `sortOrder`** của từng condition (tốt nhất là kéo thả)
- **Nếu dùng nhập số**: Khi user nhập giá trị `sortOrder` mới, hệ thống sẽ tự động điều chỉnh các condition khác theo quy tắc chèn:
  - **Ví dụ**: Danh sách hiện có sortOrder `1, 2, 3, 7`. Nếu user đổi condition có sortOrder `7` thành `2`:
    - Condition cũ sortOrder `2` được đẩy sang `3`
    - Condition cũ sortOrder `3` được đẩy sang `4`
    - Condition mới nhận sortOrder `2`

### 2.3 Chọn Data Source (phạm vi dữ liệu)
Khi user chọn `dataSource`, UI sẽ phân làm 2 nhóm:

#### **Nhóm A: ROOM và SYSTEM (Đơn giản)**
- UI chỉ cần hiển thị dropdown **các property** để user chọn
- **Không cần chọn ID cụ thể**
- Các property hỗ trợ (phải chuẩn 100% theo [Rule Doc](../api/rule.md)):

| DataSource | Property hỗ trợ                                  |
| :--------- | :----------------------------------------------- |
| SYSTEM     | `current_time`, `day_of_week`, `day_of_month`   |
| ROOM       | `avg_temperature` (nhiệt độ trung bình), `sum_watt` (điện năng tiêu thụ tổng) |

- **Ví dụ**: User chọn `ROOM` → chọn property `temperature` → điền operator & value → xong

#### **Nhóm B: DEVICE và SENSOR (Drill-down)**
- Cần UI **chọn theo cấp bậc** (không giới hạn trong cùng 1 phòng):
  1. **Category**: Chọn loại thiết bị/cảm biến (`AIR_CONDITION`, `LIGHT`, `FAN`, `TEMPERATURE`, `POWER_CONSUMPTION`)
  2. **Floor** → **Room**: Drill-down để tìm phòng chứa thiết bị
  3. **Device/Sensor ID**: Chọn ID cụ thể

- **Lưu ý**: User có thể chọn thiết bị từ **bất kỳ phòng nào** để làm dữ liệu input của condition

- Sau khi chọn xong, `resourceParam` phải là **JSON string hợp lệ** với cấu trúc:
  ```json
  {
    "category": "<loại_thiết_bị>",
    "deviceId": <id>,
    "sensorId": <id>,
    "property": "<tên_thuộc_tính>"
  }
  ```

### 2.4 Hoàn tất Condition
- Sau khi setup `dataSource` và `resourceParam`, user chọn:
  - **Toán tử** (`operator`): `=`, `!=`, `>`, `<`, `>=`, `<=`
  - **Giá trị so sánh** (`value`): Nhập giá trị (hệ thống tự parse sang số hoặc chuỗi)
  - **Logic kết nối** (`nextLogic`): `AND` (mặc định) hoặc `OR` → kết nối với điều kiện tiếp theo

### 2.5 Cập nhật Condition
- **Quan trọng**: Khi update Rule, toàn bộ danh sách condition sẽ được **ghi đè lại**
- FE phải **clone đủ tòa bộ danh sách condition hiện tại** trước khi gửi request update
- Mỗi condition gửi lên phải chứa đầy đủ các field: `id`, `sortOrder`, `dataSource`, `resourceParam` (JSON string), `operator`, `value`, `nextLogic`