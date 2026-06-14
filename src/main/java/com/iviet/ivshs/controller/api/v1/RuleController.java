package com.iviet.ivshs.controller.api.v1;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.dto.rule.CreateRuleDto;
import com.iviet.ivshs.dto.rule.RuleDto;
import com.iviet.ivshs.dto.rule.UpdateRuleStatusDto;
import com.iviet.ivshs.service.rule.RuleService;
import com.iviet.ivshs.dto.rule.UpdateRuleDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller cho Rule Engine (Interval Automation) Định tuyến tại: /api/v1/rules
 */
@RestController("ruleController")
@RequiredArgsConstructor
@RequestMapping("/v1/rules")
public class RuleController {

    private final RuleService ruleService;

    /**
     * TẠO MỚI RULE
     * 
     * @apiNote API để tạo một Rule mới. Yêu cầu payload chuẩn xác gồm các thuộc tính cơ bản (name, priority, intervalSeconds) và đi kèm mảng conditions, actions.
     * @param request Payload DTO CreateRuleDto chứa thông tin khởi tạo rule.
     * @return RuleDto cấu trúc Rule vừa được tạo cùng với ID mới.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RuleDto>> create(@RequestBody
    @Valid
    CreateRuleDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ruleService.create(request)));
    }

    /**
     * LẤY DANH SÁCH TẤT CẢ RULE (CHỈ LẤY ACTIVE)
     * 
     * @apiNote Dùng cho hệ thống load toàn bộ Rule đang được đánh dấu isActive = true, không phân trang. Thường dùng khi frontend cần select/list hoặc trigger nạp lại.
     * @return List<RuleDto> Danh sách các Rule đang hoạt động.
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RuleDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getAllActive()));
    }

    /**
     * LẤY DANH SÁCH PHÂN TRANG RULE
     * 
     * @apiNote API phân trang lấy danh sách Rule (bao gồm cả active và inactive) phục vụ UI quản lý.
     * @param page Trang mong muốn (Mặc định 0).
     * @param size Số bản ghi mỗi trang (Mặc định 10).
     * @return PaginatedResponse<RuleDto> Bao gồm dữ liệu và tổng số trang/bản ghi.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<RuleDto>>> getList(@RequestParam(name = "page", defaultValue = "0")
    int page, @RequestParam(name = "size", defaultValue = "10")
    int size) {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getAll(page, size)));
    }

    /**
     * LẤY THÔNG TIN CHI TIẾT 1 RULE KÈM ĐIỀU KIỆN, HÀNH ĐỘNG
     * 
     * @apiNote Dựa vào id truyền trên URL, trả về thông tin root và mảng conditions/actions bên trong.
     * @param id ID của Rule cần truy xuất.
     * @return RuleDto Chi tiết rule.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RuleDto>> getById(@PathVariable(name = "id")
    Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getById(id)));
    }

    /**
     * CẬP NHẬT RULE (PARTIAL UPDATE / REPLACE ARRAYS)
     * 
     * @apiNote Cơ chế cập nhật: - Các trường Object Root (name, priority, intervalSeconds, isActive): Nếu null bị bỏ qua (GIỮ NGUYÊN). Nếu có giá trị sẽ GHI ĐÈ. - Các trường Mảng Array (conditions, actions): + Nếu KHÔNG TRUYỀN null -> Mặc kệ (GIỮ NGUYÊN mảng cũ trong DB). + Nếu TRUYỀN mảng (kể cả mảng
     *          RỖNG []) -> XÓA SẠCH mảng cũ và REPLACE bằng mảng mới truyền lên.
     * 
     * @param id ID của Rule cần update.
     * @param request Payload update.
     * @return RuleDto Dữ liệu Rule sau khi được update thành công.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RuleDto>> update(@PathVariable(name = "id")
    Long id, @RequestBody
    @Valid
    UpdateRuleDto request) {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.update(id, request)));
    }

    /**
     * XÓA RULE
     * 
     * @apiNote Gỡ hoàn toàn lập lịch trên Quartz và xóa cứng trong db.
     * @param id Định danh Rule.
     * @return Xóa thành công trả về HTTP NO_CONTENT (204).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable(name = "id")
    Long id) {
        ruleService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Rule deleted successfully"));
    }

    /**
     * BẬT/TẮT TRẠNG THÁI ACTIVE RULE
     * 
     * @apiNote Cập nhật nhanh cờ isActive. Khi tắt, job sẽ lập tức bị hủy khỏi RAM máy chủ Quartz. Khi bật, nó sẽ được lập lịch lại.
     * @param id Định danh Rule.
     * @param request Payload chứa trạng thái isActive (true/false).
     * @return Phản hồi trống (HTTP 200).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable(name = "id")
    Long id, @RequestBody
    @Valid
    UpdateRuleStatusDto request) {
        ruleService.toggleIsActive(id, request.isActive());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, null, "Rule status updated: " + request.isActive()));
    }

    /**
     * ĐỌC LẠI TOÀN BỘ LẬP LỊCH TỪ CƠ SỞ DỮ LIỆU
     * 
     * @apiNote Clear toàn bộ Job thuộc RULE_ENGINE_SYSTEM trong Quartz, đọc lại những Rule đang active=true từ DB và add lại lên Quartz RAM. Dùng làm nút Backup chửa cháy khi Quartz bị lỗi lệch pha.
     * @return Phản hồi thành công (HTTP 200).
     */
    @PostMapping("/reload")
    public ResponseEntity<ApiResponse<Void>> reloadAllRules() {
        ruleService.reloadAll();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, null, "All rules reloaded in Quartz"));
    }

    /**
     * THỰC THI RULE IMMEDIATELY (TRIGGER NOW)
     * 
     * @apiNote Cưỡng ép Quartz kích nổ (Fire) Job lập tức bỏ qua chu kỳ interval. (Lưu ý: Trigger tự do 1 lần chứ không thay đổi chu kỳ hiện tại).
     * @param id ID của Rule.
     * @return Phản hồi trống (HTTP 200).
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<ApiResponse<Void>> executeNow(@PathVariable(name = "id")
    Long id) {
        ruleService.triggerNow(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, null, "Rule execution triggered immediately"));
    }
}
