# Lịch sử thay đổi API

## 2026-08-19 — Phân rã kiến trúc Rule & Tách độc lập Condition, Action API

Tái cấu trúc toàn diện module **Rule Engine**, xóa bỏ hoàn toàn các class dead-code cũ (`RuleCondition`, `RuleAction`, `RuleConditionDao`, `RuleActionDao`, `RuleConditionMapper`, `RuleActionMapper`...), chuyển sang kiến trúc **Entity-first Đa hình (Polymorphic)** và **Decoupled Standalone Services** cho `Condition` và `Action`. Hỗ trợ **Dual-Routing**: vừa có Standalone API độc lập cho Condition/Action (dùng chung cho `RULE`, `AUTOMATION`, `SYSTEM`, `ROOM_EVENT`), vừa hỗ trợ Helper Sub-Resource Endpoints trong `RuleController`.

### 1. Thay đổi trong Rule API (`/api/v1/rules`)

- **Cấu trúc DTO**:
  - Loại bỏ các mảng `conditions` và `actions` khỏi `CreateRuleDto`, `UpdateRuleDto`, `RuleDto`.
  - `CreateRuleDto` / `UpdateRuleDto` / `RuleDto` giờ đây chỉ quản lý các thuộc tính của chính Rule (`name`, `priority`, `intervalSeconds`, `isActive`, `createdAt`, `updatedAt`).
- **Hành vi Cascade Delete**:
  - Khi gọi `DELETE /api/v1/rules/{id}`, hệ thống gỡ bỏ Quartz Scheduler đồng thời tự động xóa sạch toàn bộ Condition (`ownerCategory=RULE, ownerId={id}`) và Action (`ownerCategory=RULE, ownerId={id}`) liên thuộc.
- **Bổ sung Sub-resource Helper Endpoints**:

| Method | URL | Trạng thái | Mô tả |
| :--- | :--- | :--- | :--- |
| GET | `/api/v1/rules/{id}/conditions` | **Mới** | Lấy danh sách Condition của Rule. |
| POST | `/api/v1/rules/{id}/conditions` | **Mới** | Thêm nhanh Condition cho Rule (tự động gán `ownerCategory=RULE`, `ownerId={id}`). |
| GET | `/api/v1/rules/{id}/actions` | **Mới** | Lấy danh sách Action của Rule. |
| POST | `/api/v1/rules/{id}/actions` | **Mới** | Thêm nhanh Action cho Rule (tự động gán `ownerCategory=RULE`, `ownerId={id}` và validate phần cứng). |

---

### 2. Bổ sung Standalone Condition API (`/api/v1/conditions`)

Quản lý toàn bộ vòng đời của Điều kiện đánh giá độc lập dùng chung cho toàn bộ hệ thống (`RULE`, `AUTOMATION`, `SYSTEM`...):

| Method | URL | Trạng thái | Mô tả |
| :--- | :--- | :--- | :--- |
| POST | `/api/v1/conditions` | **Mới** | Tạo mới Condition độc lập (`CreateConditionDto`). |
| GET | `/api/v1/conditions/{id}` | **Mới** | Lấy chi tiết Condition theo ID. |
| PATCH | `/api/v1/conditions/{id}` | **Mới** | Cập nhật Condition theo ID (`UpdateConditionDto`). |
| DELETE | `/api/v1/conditions/{id}` | **Mới** | Xóa Condition theo ID. |
| GET | `/api/v1/conditions?ownerCategory&ownerId` | **Mới** | Tra cứu Condition theo Owner hoặc theo Source target. |
| DELETE | `/api/v1/conditions/by-owner?ownerCategory&ownerId` | **Mới** | Xóa hàng loạt Condition theo Owner. |

---

### 3. Bổ sung Standalone Action API (`/api/v1/actions`)

Quản lý toàn bộ vòng đời của Hành động thực thi điều khiển thiết bị/bắn tín hiệu (`RULE`, `AUTOMATION`, `SYSTEM`, `ROOM_EVENT`):

| Method | URL | Trạng thái | Mô tả |
| :--- | :--- | :--- | :--- |
| POST | `/api/v1/actions` | **Mới** | Tạo mới Action độc lập (tích hợp Hardware Capability & DTO Validation). |
| GET | `/api/v1/actions/{id}` | **Mới** | Lấy chi tiết Action theo ID. |
| PATCH | `/api/v1/actions/{id}` | **Mới** | Cập nhật Action theo ID (`UpdateActionDto`). |
| DELETE | `/api/v1/actions/{id}` | **Mới** | Xóa Action theo ID. |
| GET | `/api/v1/actions?ownerCategory&ownerId` | **Mới** | Tra cứu Action theo Owner hoặc theo Target device. |
| DELETE | `/api/v1/actions/by-owner?ownerCategory&ownerId` | **Mới** | Xóa hàng loạt Action theo Owner. |

---

### 4. Dọn dẹp Clean Code (P0) & Tài liệu cập nhật

- **Xóa Dead Code**: Đã xóa vĩnh viễn 14 file legacy của `RuleCondition` và `RuleAction` (Entities, DAOs, DTOs, Mappers).
- `docs/api/rule.md`: Viết lại hoàn chỉnh theo cấu trúc decoupled Rule + Sub-resource endpoints (bỏ `isInterval` thừa trong response).
- `docs/api/condition.md`: Tạo mới tài liệu chi tiết cho Condition API (chuẩn hóa enum `ConditionOwnerCategory`).
- `docs/api/action.md`: Tạo mới tài liệu chi tiết cho Action API (chuẩn hóa schema `FanControlRequestBody` và `AirConditionControlRequestBody`).
- `docs/api/README.md`: Bổ sung liên kết tới `condition.md` và `action.md`.
- `docs/changes/api-changes-19-08-2026.md`: Ghi nhận toàn bộ thay đổi API ngày 19/08/2026 trung thực và chính xác 100%.
