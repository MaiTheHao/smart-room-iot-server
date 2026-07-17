# Quy định Tính toán Chỉ số Môi trường & Điện năng cấp Phòng (Room-level)

Tài liệu kỹ thuật định nghĩa các phương pháp tổng hợp dữ liệu (aggregation methods) từ các cảm biến IoT để tính toán chỉ số đại diện cho cấp Phòng.

---

## 1. Bảng Tổng Hợp Phương Pháp

| Lĩnh vực (Domain) | Tham số (Metric) | Phương thức tổng hợp | Cơ chế/Nguồn dữ liệu |
| :--- | :--- | :--- | :--- |
| **Độ ẩm** | Humidity (`%`) | **Median** (Trung vị) | Tổng hợp từ toàn bộ cảm biến độ ẩm hoạt động trong phòng. |
| **Độ sáng** | Lux (`Lux`) | **Median** (Trung vị) | Tổng hợp từ toàn bộ cảm biến cường độ ánh sáng trong phòng. |
| **Nồng độ $CO_2$** | $CO_2$ (`ppm`) | **Max** & **Mean** | - **Max**: Dùng cho Automation (Điều khiển thiết bị ngoại vi).<br>- **Mean**: Dùng cho lưu trữ lịch sử và vẽ đồ thị. |
| **Nhiệt độ** | Temperature (`°C`) | **Mean** (Trung bình cộng) | Lấy giá trị trung bình cộng (`AVG`) từ tất cả cảm biến nhiệt độ trong phòng. |
| **Điện năng** | Voltage (`V`) <br> Current (`A`) <br> Power (`W`) <br> Frequency (`Hz`) <br> Power Factor (`PF`) <br> Energy (`kWh`) | **Mean** (Trung bình cộng) <br> **Mean** (Trung bình cộng) <br> **Mean** (Trung bình cộng) <br> **Mean** (Trung bình cộng) <br> **Mean** (Trung bình cộng) <br> **Max** (Giá trị lớn nhất) | Thu thập trực tiếp từ cảm biến đo tổng của phòng (`PowerConsumption`). |

---

## 2. Chi Tiết Kỹ Thuật

### Độ ẩm (Humidity)
* **Thuật toán:** Median
* **Mục đích:** Loại bỏ các giá trị dị biệt (outliers) hoặc dữ liệu nhiễu cục bộ từ cảm biến đơn lẻ.

### Độ sáng (Lux)
* **Thuật toán:** Median
* **Mục đích:** Giảm thiểu ảnh hưởng từ hiện tượng chói sáng cục bộ (ví dụ: nắng rọi trực tiếp vào cảm biến).

### Khí $CO_2$
* **Thuật toán:**
  * **Max (Lớn nhất):** Phục vụ logic tự động hóa (Automation). Kích hoạt quạt thông gió ngay khi phát hiện vùng ngột ngạt cục bộ vượt ngưỡng an toàn.
  * **Mean (Trung bình cộng):** Phục vụ ghi nhận lịch sử (historical logs) để đánh giá xu hướng chất lượng không khí chung của cả phòng.

### Nhiệt độ (Temperature)
* **Thuật toán:** Mean (Trung bình cộng)
* **Mục đích:** Phản ánh nhiệt độ chung của không gian phòng bằng cách lấy trung bình cộng tất cả các điểm đo nhiệt độ trong phòng.

### Điện năng (Electricity)
Dữ liệu được thu thập từ thiết bị đo tổng của phòng (`PowerConsumption`). Khi gom nhóm dữ liệu theo các khoảng thời gian (Divisor):
* **Công suất & Thông số trạng thái điện lưới (Voltage, Current, Power, Frequency, Power Factor):** Áp dụng **Mean (AVG)** để lấy giá trị trung bình ổn định trong khoảng thời gian gom nhóm.
* **Điện tích lũy (Energy):** Áp dụng **Max** do đây là chỉ số tích lũy lũy kế (cumulative), giá trị lớn nhất thể hiện tổng lượng điện tiêu thụ tính đến cuối chu kỳ.


