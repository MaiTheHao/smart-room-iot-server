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

@RestController("ruleController")
@RequiredArgsConstructor
@RequestMapping("/v1/rules")
public class RuleController {

    private final RuleService ruleService;

    @PostMapping
    public ResponseEntity<ApiResponse<RuleDto>> create(@RequestBody @Valid CreateRuleDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(ruleService.create(request)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RuleDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getAllActive()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<RuleDto>>> getList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getAll(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RuleDto>> getById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RuleDto>> update(@PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateRuleDto request) {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable(name = "id") Long id) {
        ruleService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Rule deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateRuleStatusDto request) {
        ruleService.toggleIsActive(id, request.isActive());
        return ResponseEntity
                .ok(ApiResponse.success(HttpStatus.OK, null, "Rule status updated: " + request.isActive()));
    }

    @PostMapping("/reload")
    public ResponseEntity<ApiResponse<Void>> reloadAllRules() {
        ruleService.reloadAll();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, null, "All rules reloaded in Quartz"));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<ApiResponse<Void>> executeNow(@PathVariable(name = "id") Long id) {
        ruleService.triggerNow(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, null, "Rule execution triggered immediately"));
    }
}
