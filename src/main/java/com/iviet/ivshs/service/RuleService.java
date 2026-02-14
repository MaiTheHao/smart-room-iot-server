package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateRuleDto;
import com.iviet.ivshs.dto.RuleDto;
import com.iviet.ivshs.dto.UpdateRuleDto;

import java.util.List;

/**
 * Service interface quản lý các quy tắc điều khiển thiết bị.
 * <p>
 * Cung cấp các chức năng:
 * <ul>
 *   <li>Thực thi quét toàn cục các quy tắc đang hoạt động</li>
 *   <li>Tạo, cập nhật, xóa và truy vấn các quy tắc</li>
 *   <li>Tái tải cấu hình quy tắc</li>
 * </ul>
 */
public interface RuleService {

    /**
     * Thực thi quét toàn cục tất cả các quy tắc đang hoạt động.
     * <p>
     * - Nhóm các quy tắc theo thiết bị đích (category:id)
     * - Đánh giá điều kiện của từng quy tắc
     * - Chọn quy tắc có độ ưu tiên cao nhất (Winner-Takes-All)
     * - Thực thi hành động của quy tắc chiến thắng
     */
    void executeGlobalRuleScan();

    /**
     * Tái tải lại toàn bộ cấu hình quy tắc từ cơ sở dữ liệu.
     * Sử dụng mô hình Global Engine.
     */
    void reloadAllRules();

    /**
     * Tạo một quy tắc điều khiển mới.
     *
     * @param request DTO chứa thông tin quy tắc
     * @return Quy tắc đã được lưu với ID được gán
     * @throws BadRequestException nếu tên quy tắc đã tồn tại
     */
    RuleDto create(CreateRuleDto request);

    /**
     * Cập nhật thông tin của một quy tắc.
     *
     * @param id ID của quy tắc cần cập nhật
     * @param request Dữ liệu cập nhật
     * @return Quy tắc đã cập nhật
     * @throws NotFoundException nếu quy tắc không tồn tại
     */
    RuleDto update(Long id, UpdateRuleDto request);

    /**
     * Xóa một quy tắc khỏi hệ thống.
     *
     * @param id ID của quy tắc cần xóa
     * @throws NotFoundException nếu quy tắc không tồn tại
     */
    void delete(Long id);

    /**
     * Lấy thông tin một quy tắc theo ID.
     *
     * @param id ID của quy tắc cần lấy
     * @return Quy tắc tương ứng
     * @throws NotFoundException nếu quy tắc không tồn tại
     */
    RuleDto getById(Long id);

    /**
     * Lấy danh sách tất cả các quy tắc.
     *
     * @return Danh sách các quy tắc
     */
    List<RuleDto> getAll();

    /**
     * Thay đổi trạng thái hoạt động của quy tắc.
     *
     * @param id ID của quy tắc
     * @param isActive Trạng thái mới
     * @throws NotFoundException nếu quy tắc không tồn tại
     */
    void toggleIsActive(Long id, boolean isActive);
}
