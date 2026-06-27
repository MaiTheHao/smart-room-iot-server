# Báo Cáo Đối Chiếu Phân Quyền Alert Sau Khi Fix

Bản báo cáo này đối chiếu chi tiết luồng nghiệp vụ và bảo mật của hệ thống Smart Room IoT trước và sau khi sửa lỗi **Phân quyền chéo (Cross-group Privilege Escalation)** được mô tả trong [problem.md](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/problem.md).

---

## 1. So Sánh Luồng Kiểm Tra Quyền (Xem/Xử lý Alert)

Dưới đây là sự khác biệt trong luồng kiểm tra quyền xử lý khi người dùng tương tác với Alert.

```mermaid
graph TD
    subgraph Trước Khi Sửa (Mô Hình Toàn Cục)
        A1[Yêu cầu xử lý Alert của Phòng A] --> B1{"User có quyền F_HANDLE_ALERT toàn cục không?"}
        B1 -- Có (Tích lũy từ Phòng B) --> C1{"User có thuộc Phòng A không?"}
        C1 -- Có (User thuộc Phòng A) --> D1[Hợp Lệ - Cho phép xử lý]
        D1 --> E1["Hậu quả: Leo thang đặc quyền! (Phòng A chỉ có quyền Read)"]
    end

    subgraph Sau Khi Sửa (Mô Hình Hybrid)
        A2[Yêu cầu xử lý Alert của Phòng A] --> B2["Lấy danh sách các Nhóm gán với Alert (Phòng A)"]
        B2 --> C2{"User có thuộc Phòng A không?"}
        C2 -- Không --> D2[Bị Từ Chối - ForbiddenException]
        C2 -- Có --> E2{"Trong nhóm Phòng A, User có quyền F_HANDLE_ALERT?"}
        E2 -- Có --> F2[Hợp Lệ - Cho phép xử lý]
        E2 -- Không --> D2
    end
```

### Chi Tiết Đối Chiếu:

| Đặc tính kiểm tra | Trước khi sửa (problem.md) | Sau khi sửa (Hiện tại) |
| :--- | :--- | :--- |
| **Cơ chế lưu trữ quyền** | Gộp toàn bộ Authority thành chuỗi phẳng trong Security Context. | Lưu trữ dạng cấu trúc bản đồ bất biến `Map<Long, Set<String>>` (`Group -> Permissions`) trong `CustomUserDetails`. |
| **Kiểm tra quyền Admin** | Hệ thống kiểm tra quyền manage toàn cục và bypass hoàn toàn kiểm tra nhóm. | **Không bypass**. Admin cũng là một người dùng thông thường đối với Alert, chỉ có quyền xem/xử lý các Alert thuộc nhóm mình tham gia, và nhóm đó phải được phân quyền `F_ACCESS_ALERT`/`F_HANDLE_ALERT` tường minh từ DB. |
| **Logic xác thực tầng Service** | Tách rời: Kiểm tra quyền toàn cục `F_HANDLE_ALERT` độc lập, sau đó chỉ kiểm tra sự tồn tại trong nhóm. | Kết hợp chặt chẽ: Xác thực quyền chức năng **ngay trên phạm vi nhóm cụ thể** được gán cho Alert. |
| **Rủi ro leo thang quyền** | **RẤT CAO** (Chỉ cần có 1 nhóm có quyền Write và thuộc nhóm Read-Only của Alert khác là có thể ghi đè). | **ĐÃ KHẮC PHỤC** (Quyền xử lý bị cô lập hoàn toàn trong phạm vi nhóm nhận cảnh báo). |

---

## 2. So Sánh Luồng Truy Vấn Danh Sách Alert (Search/Retrieve)

### Trước khi sửa:
- **Cơ chế:** Tầng Service truyền `clientId` xuống DAO.
- **DAO query:** Thực hiện JOIN phức tạp từ `alert_instance` -> `alert_instance_group` -> `sys_group` -> `client_group` -> `client`.
- **Hạn chế:** DAO bị phụ thuộc vào thực thể `Client` và logic xác thực người dùng.

### Sau khi sửa:
1. **Service xử lý trước:** Tầng Service lấy danh sách các nhóm được phép xem thông qua `SecurityContextUtil.getAllowedGroups(F_ACCESS_ALERT)`.
2. **Truy vấn tối giản:** Service truyền tập hợp `groupIds` trực tiếp xuống DAO.
3. **DAO query:** Thực hiện lọc đơn giản bằng `WHERE arg.group.id IN (:groupIds)`.
4. **Lợi ích:** Tách biệt hoàn toàn tầng DAO khỏi nghiệp vụ phân quyền. Không cần join bảng `client`, cải thiện hiệu năng truy vấn SQL đáng kể.

---

## 3. Khắc Phục Lỗi N+1 Query

* **Vấn đề cũ:** Trong tầng Service, mỗi lần xác thực quyền truy cập của một Alert đơn lẻ, hệ thống phải gọi xuống DB để truy vấn danh sách nhóm nhận tin (`findGroupsByAlertId`). Khi duyệt danh sách lớn, số lượng query tăng tuyến tính (N+1 query).
* **Giải pháp mới:** 
  - Ánh xạ trực tiếp `@OneToMany` trường `alertInstanceGroups` trong thực thể `AlertInstance`.
  - Tận dụng cơ chế lazy loading và Hibernate session cache. Khi truy vấn danh sách, ta có thể `JOIN FETCH` trường này để nạp toàn bộ thông tin chỉ với **1 câu query duy nhất**, loại bỏ hoàn toàn các truy vấn DB dư thừa khi kiểm tra quyền.

---

## 4. Tổng Kết Trạng Thái Hệ Thống

| Vấn đề (problem.md) | Trạng thái hiện tại | Giải pháp khắc phục |
| :--- | :--- | :--- |
| Leo thang đặc quyền chéo | **ĐÃ GIẢI QUYẾT** | Tích hợp ánh xạ nhóm-quyền bất biến trong CustomUserDetails. |
| N+1 query khi check quyền | **ĐÃ GIẢI QUYẾT** | Ánh xạ trường liên kết `@OneToMany` trực tiếp trong thực thể. |
| DAO bị phụ thuộc vào Client/Role | **ĐÃ GIẢI QUYẾT** | Chuyển giao việc phân giải nhóm cho Service và lọc bằng IDs ở DAO. |