package com.iviet.ivshs.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iviet.ivshs.service.PermissionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/automation")
@RequiredArgsConstructor
public class AutomationViewController {

	private final PermissionService permissionService;

	@GetMapping
	public String automationIndex(Model model) {
		model.addAttribute("pageTitle", "Automation");
		return "redirect:/automation/jobs";
	}

	@GetMapping("/jobs")
	public String automationScenarios(Model model) {
		permissionService.requireManageAutomation();
		model.addAttribute("pageTitle", "Job Management");
		model.addAttribute("activeTab", "jobs");
		return "pages/automation/jobs.html";
	}

	@GetMapping("/equipments")
	public String automationEquipments(Model model) {
		permissionService.requireManageAutomation();
		model.addAttribute("pageTitle", "Automation Equipments");
		model.addAttribute("activeTab", "equipments");
		return "pages/automation/equipments.html";
	}
}
