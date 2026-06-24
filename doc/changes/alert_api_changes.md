# Thay đổi về API Alert & Các biến Template cho Rule Engine

Tài liệu này tóm tắt các thay đổi và bổ sung đối với hệ thống biến truyền vào `messageTemplate` của Alert Config kích hoạt bởi **Rule Engine** (`namespace: RULE`).

---

## 1. Các biến bổ sung trong `messageTemplate`

Khi một quy tắc (Rule) thỏa mãn điều kiện và kích hoạt cảnh báo, ngoài các biến cũ, hệ thống hiện tại đã hỗ trợ thêm các biến sau:

| Tên biến | Kiểu dữ liệu | Mô tả |
| :--- | :--- | :--- |
| `total_conditions` | Integer | **[Mới]** Tổng số điều kiện (conditions) được cấu hình trong quy tắc |
| `cond{sortOrder}_operator` | String | **[Mới]** Toán tử so sánh của điều kiện tương ứng (ví dụ: `>`, `<`, `=`, `>=`, `<=`, `!=`) |

---

## 2. Chi tiết ví dụ sử dụng

Giả sử chúng ta có một quy tắc (Rule) với thông tin như sau:
* **ID**: `10`
* **Tên**: `AUTORULE_TEMP`
* **Danh sách điều kiện** (gồm 2 điều kiện):
  1. Điều kiện 0 (`sortOrder = 0`): Nhiệt độ phòng (`ROOM_TEMP`) **>** `30`
  2. Điều kiện 1 (`sortOrder = 1`): Trạng thái điều hòa (`AC_POWER`) **==** `OFF`

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

## 3. Các file thay đổi liên quan
* Logic xử lý: [RuleProcessor.java](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/java/com/iviet/ivshs/scheduler/dynamic/rule/RuleProcessor.java)
* Tài liệu API chính thức: [alert.md](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/doc/api/alert.md)
