# Thiết kế kỹ thuật: Tách rời các Device Components trong Room Detail Page

## 1. Giới thiệu & Mục tiêu
Trang Room Detail hiện tại có file `ui_renderer.js` khá lớn (~650 dòng), chứa nhiều logic trộn lẫn giữa hiển thị biểu đồ phòng (`RoomAnalytics`), hiển thị danh sách thiết bị (`DeviceRenderer`), và quản lý biểu đồ thiết bị (`DeviceChart`). 
Mục tiêu của đợt tái cấu trúc này là:
*   Loại bỏ cấu trúc phân tab (Tabs) ban đầu, chuyển sang giao diện cuộn dọc xổ thẳng toàn bộ thiết bị được chia theo các nhóm tiêu đề: Điều hòa (Air Conditioner), Quạt (Fan), và Đèn (Light).
*   Mỗi nhóm có nút toggle để người dùng có thể thu gọn (collapse) hoặc mở rộng (expand) nhóm đó.
*   Tách rời `DeviceRenderer` và `DeviceChart` ra khỏi `ui_renderer.js` vào thư mục `component/` với cấu trúc thư mục con cho từng component để tăng tính mô-đun và dễ bảo trì.
*   Tách nhỏ card thiết bị thành các module cụ thể: `ac_card.js`, `fan_card.js`, `light_card.js` và điều phối qua `device_card.js`.

---

## 2. Cấu trúc thư mục mới
Thư mục `component/` sẽ chứa các thư mục con sau:
```text
room_detail/component/
├── device_list/
│   └── device_list.js       # Quản lý danh sách nhóm thiết bị dạng accordion/collapse dọc
├── device_card/
│   ├── device_card.js       # Factory điều phối chung cho các loại card
│   ├── ac_card.js           # Giao diện điều khiển & cập nhật trạng thái Điều hòa (AC)
│   ├── fan_card.js          # Giao diện điều khiển & cập nhật trạng thái Quạt (FAN)
│   └── light_card.js        # Giao diện điều khiển & cập nhật trạng thái Đèn (LIGHT)
└── device_chart/
    └── device_chart.js      # Giao diện biểu đồ phân tích & Flatpickr cho thiết bị
```

---

## 3. Chi tiết thiết kế các Component

### 3.1. `component/device_list/device_list.js`
*   **Chức năng**:
    *   `init()`: Khởi tạo khung HTML hiển thị 3 nhóm thiết bị (AC, Fan, Light) với tiêu đề nhóm và nút collapse để thu gọn.
    *   `renderOrUpdateAll(newDevices)`: Lọc thiết bị theo loại, so sánh danh sách `id` để quyết định cập nhật động trạng thái của card hoặc render lại toàn bộ nhóm.
    *   `renderGroupFull(pane, items, emptyMsg)`: Render lại danh sách thiết bị của một nhóm, khôi phục lại trạng thái toggle đóng/mở của các card con và khởi chạy lại biểu đồ nếu tab analytics đang mở.
*   **Giao diện HTML mẫu cho nhóm**:
    ```html
    <div class="mb-4">
        <div class="d-flex justify-content-between align-items-center bg-light p-3 rounded-4 cursor-pointer" data-bs-toggle="collapse" data-bs-target="#group-ac-content">
            <h6 class="m-0 fw-bold text-dark d-flex align-items-center">
                <i data-lucide="wind" class="text-primary me-2"></i>
                <span>Điều hòa</span>
            </h6>
            <i data-lucide="chevron-down"></i>
        </div>
        <div class="collapse show mt-3" id="group-ac-content">
            <!-- Danh sách các card thiết bị được render ở đây -->
        </div>
    </div>
    ```

### 3.2. `component/device_card/device_card.js` (Dispatcher)
*   **Chức năng**:
    *   `render(device)`: Nhận diện `device.category` để gọi `render` tương ứng từ `ac_card`, `fan_card`, hay `light_card`.
    *   `update(device)`: Nhận diện `device.category` để gọi `update` tương ứng từ `ac_card`, `fan_card`, hay `light_card`.

### 3.3. Các Card thiết bị cụ thể (`ac_card.js`, `fan_card.js`, `light_card.js`)
*   **Chức năng**:
    *   `renderCard(device)`: Render cấu trúc card chung cho thiết bị.
    *   `renderControlPane(device, isActive)`: Render các slider, switch, button điều khiển đặc thù cho từng loại.
    *   `updateCardStatus(device)`: Cập nhật trực tiếp các slider/nút bấm trên giao diện mà không cần render lại toàn bộ card.

### 3.4. `component/device_chart/device_chart.js`
*   **Chức năng**:
    *   `init(naturalId)`: Khởi tạo date range picker `flatpickr` và đối tượng `ApexCharts` để vẽ biểu đồ công suất/chỉ số điện năng tiêu thụ.
    *   `refreshData(naturalId)`, `updateChart(naturalId)`: Tải lại và cập nhật dữ liệu.
    *   `switchType(naturalId, newType)`: Chuyển đổi dữ liệu hiển thị (W, V, A, kWh).

---

## 4. Kế hoạch xác minh (Verification Plan)
1.  **Xác minh giao diện**:
    *   Kiểm tra trang room detail hiển thị dọc 3 nhóm thiết bị: Điều hòa, Quạt, Đèn.
    *   Kiểm tra nút toggle thu gọn/mở rộng từng nhóm hoạt động đúng.
    *   Kiểm tra giao diện card thiết bị, nút bật/tắt chính và phần điều khiển/biểu đồ bên trong.
2.  **Xác minh chức năng**:
    *   Bật/tắt thiết bị và điều khiển (thay đổi nhiệt độ AC, tốc độ quạt, độ sáng đèn) hoạt động bình thường qua API.
    *   Biểu đồ của từng thiết bị vẫn được vẽ chính xác khi chuyển sang tab "Analytics" và đổi loại chỉ số (W, V, A, kWh).
    *   Tự động cập nhật trạng thái thiết bị (polling mỗi 5 giây) không bị gián đoạn hay lỗi giao diện.
