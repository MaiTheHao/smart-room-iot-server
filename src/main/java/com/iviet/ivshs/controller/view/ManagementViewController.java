package com.iviet.ivshs.controller.view;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/management")
public class ManagementViewController {

    @GetMapping("/clients")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_CLIENT')")
    public String clientsPage() {
        return "pages/management/clients.html";
    }

    @GetMapping("/floors")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_FLOOR')")
    public String floorsPage() {
        return "pages/management/floors.html";
    }

    @GetMapping("/rooms")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ROOM')")
    public String roomsPage() {
        return "pages/management/rooms.html";
    }

    @GetMapping("/functions")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_FUNCTION')")
    public String functionsPage() {
        return "pages/management/functions.html";
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ROLE')")
    public String rolesPage() {
        return "pages/management/roles.html";
    }

    @GetMapping("/groups")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_GROUP')")
    public String groupsPage() {
        return "pages/management/groups.html";
    }
}
