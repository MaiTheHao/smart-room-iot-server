package com.iviet.ivshs.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iviet.ivshs.dto.RuleDto;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.PermissionService;
import com.iviet.ivshs.service.RuleService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/rule")
@RequiredArgsConstructor
public class RuleViewController {

	private final PermissionService permissionService;
	private final RuleService ruleService;

	@GetMapping
	public String ruleIndex() {
		return "redirect:/rule/rules";
	}

	@GetMapping("/rules")
	public String ruleList(Model model) {
		permissionService.requireManageAutomation();
		model.addAttribute("pageTitle", "Rule Management");
		model.addAttribute("activeTab", "rules");
		return "pages/rule/rules.html";
	}

	@GetMapping("/rules/{id}/conditions")
	public String ruleConditions(@PathVariable(name = "id") Long id, Model model) {
		if (id == null) throw new BadRequestException("Rule ID is required");

		permissionService.requireManageAutomation();
		RuleDto rule = ruleService.getById(id);
		model.addAttribute("rule", rule);
		model.addAttribute("pageTitle", "Rule Conditions");
		model.addAttribute("activeTab", "rules");
		return "pages/rule/conditions.html";
	}
}
