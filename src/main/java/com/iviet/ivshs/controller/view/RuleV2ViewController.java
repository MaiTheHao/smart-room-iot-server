package com.iviet.ivshs.controller.view;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/v2")
@PreAuthorize("hasAuthority('F_MANAGE_RULE') || hasAuthority('F_MANAGE_ALL')")
public class RuleV2ViewController {

	@GetMapping("/rules")
	public String ruleV2Page(Model model) {
		return "pages/rule_v2/rules.html";
	}

	@GetMapping("/rules/{id}/conditions")
	public String ruleV2ConditionsPage(@PathVariable("id") Long id, Model model) {
		model.addAttribute("ruleId", id);
		return "pages/rule_v2/conditions.html";
	}

	@GetMapping("/rules/{id}/actions")
	public String ruleV2ActionsPage(@PathVariable("id") Long id, Model model) {
		model.addAttribute("ruleId", id);
		return "pages/rule_v2/actions.html";
	}
}
