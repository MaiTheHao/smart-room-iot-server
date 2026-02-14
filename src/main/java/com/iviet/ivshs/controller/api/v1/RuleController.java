package com.iviet.ivshs.controller.api.v1;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iviet.ivshs.dto.ApiResponse;
import com.iviet.ivshs.dto.CreateRuleDto;
import com.iviet.ivshs.dto.RuleDto;
import com.iviet.ivshs.dto.UpdateRuleDto;
import com.iviet.ivshs.service.RuleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rules")
public class RuleController {

	private final RuleService ruleService;

	@PostMapping
	public ResponseEntity<ApiResponse<RuleDto>> create(
			@RequestBody @Valid CreateRuleDto request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.created(ruleService.create(request)));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<RuleDto>>> getAll() {
		return ResponseEntity.ok(ApiResponse.ok(ruleService.getAll()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<RuleDto>> getById(
			@PathVariable(name = "id") Long id) {
		return ResponseEntity.ok(ApiResponse.ok(ruleService.getById(id)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<RuleDto>> update(
			@PathVariable(name = "id") Long id,
			@RequestBody @Valid UpdateRuleDto request) {
		return ResponseEntity.ok(ApiResponse.ok(ruleService.update(id, request)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(
			@PathVariable(name = "id") Long id) {
		ruleService.delete(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
				.body(ApiResponse.success(HttpStatus.NO_CONTENT, null, "Rule deleted successfully"));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<ApiResponse<Void>> toggleStatus(
			@PathVariable(name = "id") Long id,
			@RequestParam(name = "isActive") boolean isActive) {
		ruleService.toggleIsActive(id, isActive);
		return ResponseEntity.ok(
				ApiResponse.success(HttpStatus.OK, null, "Rule status updated: " + isActive));
	}

	@PostMapping("/scan")
	public ResponseEntity<ApiResponse<Void>> executeGlobalRuleScan() {
		ruleService.executeGlobalRuleScan();
		return ResponseEntity.ok(
				ApiResponse.success(HttpStatus.OK, null, "Global rule scan executed"));
	}

	@PostMapping("/reload")
	public ResponseEntity<ApiResponse<Void>> reloadAllRules() {
		ruleService.reloadAllRules();
		return ResponseEntity.ok(
				ApiResponse.success(HttpStatus.OK, null, "All rules reloaded"));
	}
}
