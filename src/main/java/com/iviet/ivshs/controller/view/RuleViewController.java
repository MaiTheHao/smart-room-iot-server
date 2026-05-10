package com.iviet.ivshs.controller.view;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/rules")
@PreAuthorize("hasAuthority('F_MANAGE_RULE') || hasAuthority('F_MANAGE_ALL')")
public class RuleViewController {

	@GetMapping("")
	public String rulePage(Model model) {
		return "pages/rule/rules.html";
	}

	@GetMapping("/{id}/conditions")
	public String ruleConditionsPage(@PathVariable("id") Long id, Model model) {
		model.addAttribute("ruleId", id);
		return "pages/rule/conditions.html";
	}

	@GetMapping("/{id}/actions")
	public String ruleActionsPage(@PathVariable("id") Long id, Model model) {
		model.addAttribute("ruleId", id);
		return "pages/rule/actions.html";
	}
}

