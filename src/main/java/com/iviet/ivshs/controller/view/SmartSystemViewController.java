package com.iviet.ivshs.controller.view;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.iviet.ivshs.service.automation.AutomationService;
import com.iviet.ivshs.service.rule.RuleService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/management/smart-system")
@RequiredArgsConstructor
public class SmartSystemViewController {

    private final AutomationService automationService;
    private final RuleService ruleService;

    @GetMapping("/automations")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public String automationsPage() {
        return "pages/smart_system/automation/index.html";
    }

    @GetMapping("/automations/{id}/actions")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_AUTOMATION')")
    public String automationActionsPage(@PathVariable("id")
    Long id, Model model) {
        model.addAttribute("automation", automationService.getById(id));
        return "pages/smart_system/automation/actions.html";
    }

    @GetMapping("/rules")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
    public String rulesPage() {
        return "pages/smart_system/rule/index.html";
    }

    @GetMapping("/rules/{id}/conditions")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
    public String ruleConditionsPage(@PathVariable("id")
    Long id, Model model) {
        model.addAttribute("rule", ruleService.getById(id));
        return "pages/smart_system/rule/conditions.html";
    }

    @GetMapping("/rules/{id}/actions")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
    public String ruleActionsPage(@PathVariable("id")
    Long id, Model model) {
        model.addAttribute("rule", ruleService.getById(id));
        return "pages/smart_system/rule/actions.html";
    }

    @GetMapping("/rules/{id}/alert")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_RULE')")
    public String ruleAlertPage(@PathVariable("id")
    Long id, Model model) {
        model.addAttribute("rule", ruleService.getById(id));
        return "pages/smart_system/rule/alert.html";
    }

    @GetMapping("/alerts")
    @PreAuthorize("isAuthenticated()")
    public String alertsPage() {
        return "pages/smart_system/alert/index.html";
    }
}

