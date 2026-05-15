package com.iviet.ivshs.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/management")
public class ManagementViewController {

    @GetMapping("/clients")
    public String clientsPage() {
        return "pages/management/clients.html";
    }

    @GetMapping("/floors")
    public String floorsPage() {
        return "pages/management/floors.html";
    }

    @GetMapping("/rooms")
    public String roomsPage() {
        return "pages/management/rooms.html";
    }

    @GetMapping("/functions")
    public String functionsPage() {
        return "pages/management/functions.html";
    }

    @GetMapping("/roles")
    public String rolesPage() {
        return "pages/management/roles.html";
    }
}
