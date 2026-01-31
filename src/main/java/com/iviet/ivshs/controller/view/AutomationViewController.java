package com.iviet.ivshs.controller.view;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iviet.ivshs.dto.AutomationActionDto;
import com.iviet.ivshs.dto.AutomationDto;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.AutomationService;
import com.iviet.ivshs.service.PermissionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/automation")
@RequiredArgsConstructor
public class AutomationViewController {

	private final PermissionService permissionService;
	private final AutomationService automationService;

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
	
	@GetMapping("/jobs/{id}/equipments")
	public String automationJobEquipments(@PathVariable(name = "id") Long id, Model model) {
		if (id == null) throw new BadRequestException("Automation ID is required");

		permissionService.requireManageAutomation();
		AutomationDto automation = automationService.getById(id);
		List<AutomationActionDto> actions = automationService.getActions(id);
		model.addAttribute("automation", automation);
		model.addAttribute("actions", actions);
		model.addAttribute("pageTitle", "Automation Equipment Actions");
		model.addAttribute("activeTab", "jobs");
		return "pages/automation/equipment.html";	
	}
}
