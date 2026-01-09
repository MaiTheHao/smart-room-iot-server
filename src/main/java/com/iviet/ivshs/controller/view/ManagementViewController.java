package com.iviet.ivshs.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iviet.ivshs.service.PermissionService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/management")
@RequiredArgsConstructor
public class ManagementViewController {

    private final PermissionService permissionService;

    @GetMapping
    public String managementIndex(Model model) {
        model.addAttribute("pageTitle", "Management");
        return "redirect:/management/users";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        permissionService.requireManageClient();
        model.addAttribute("pageTitle", "Manage Users");
        model.addAttribute("activeTab", "users");
        return "pages/management/users.html";
    }

    @GetMapping("/groups")
    public String manageGroups(Model model) {
        permissionService.requireManageGroup();
        model.addAttribute("pageTitle", "Manage Groups");
        model.addAttribute("activeTab", "groups");
        return "pages/management/groups.html";
    }

    @GetMapping("/functions")
    public String manageFunctions(Model model) {
        permissionService.requireManageFunction();
        model.addAttribute("pageTitle", "Manage Functions");
        model.addAttribute("activeTab", "functions");
        return "pages/management/functions.html";
    }
}
