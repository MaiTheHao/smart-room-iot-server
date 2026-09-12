package com.iviet.ivshs.controller.view;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.iviet.ivshs.service.PermissionService;
import com.iviet.ivshs.service.RoomEventConfigService;
import com.iviet.ivshs.service.RoomViewService;
import com.iviet.ivshs.service.RoomViewService.RoomDetailCriteria;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RoomViewController {

    private final RoomViewService roomViewService;
    private final PermissionService permissionService;
    private final RoomEventConfigService roomEventConfigService;

    @GetMapping("/rooms/{id}")
    @PreAuthorize("@permissionService.canAccessRoom(#id)")
    public String roomDetail(@PathVariable("id")
    Long id, Model model) {
        var _model = roomViewService.getRoomDetailModel(RoomDetailCriteria.builder().roomId(id).build());
        model.addAllAttributes(_model.toModelAttributes());
        return "pages/room.html";
    }

    @GetMapping("/rooms/{id}/events")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ROOM')")
    public String roomEvents(@PathVariable("id") Long id, Model model) {
        permissionService.requireAccessRoom(id);
        var _model = roomViewService.getRoomDetailModel(RoomDetailCriteria.builder().roomId(id).build());
        model.addAllAttributes(_model.toModelAttributes());
        return "pages/room_event.html";
    }

    @GetMapping("/rooms/{id}/events/{configId}/conditions")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ROOM')")
    public String roomEventConditions(
            @PathVariable("id") Long id,
            @PathVariable("configId") Long configId,
            Model model) {
        populateRoomEventModel(id, configId, model);
        return "pages/room_event/conditions.html";
    }

    @GetMapping("/rooms/{id}/events/{configId}/actions")
    @PreAuthorize("hasAnyAuthority('F_MANAGE_ALL', 'F_MANAGE_ROOM')")
    public String roomEventActions(
            @PathVariable("id") Long id,
            @PathVariable("configId") Long configId,
            Model model) {
        populateRoomEventModel(id, configId, model);
        return "pages/room_event/actions.html";
    }

    private void populateRoomEventModel(Long roomId, Long configId, Model model) {
        permissionService.requireAccessRoom(roomId);
        var _model = roomViewService.getRoomDetailModel(RoomDetailCriteria.builder().roomId(roomId).build());
        model.addAllAttributes(_model.toModelAttributes());
        var eventConfig = roomEventConfigService.getById(roomId, configId);
        model.addAttribute("eventConfig", eventConfig);
    }

    @GetMapping("/js/pages/room_detail/index.js")
    public String getRoomJs() {
        return "pages/room_detail/index.js";
    }
}
