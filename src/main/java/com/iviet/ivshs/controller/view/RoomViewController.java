package com.iviet.ivshs.controller.view;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.iviet.ivshs.service.RoomViewService;
import com.iviet.ivshs.service.RoomViewService.RoomDetailCriteria;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RoomViewController {

    private final RoomViewService roomViewService;

    @GetMapping("/rooms/{id}")
    @PreAuthorize("@permissionService.canAccessRoom(#id)")
    public String roomDetail(@PathVariable("id") Long id, Model model) {
        var _model = roomViewService.getRoomDetailModel(RoomDetailCriteria.builder().roomId(id).build());
        model.addAllAttributes(_model.toModelAttributes());
        return "pages/room.html";
    }

    @GetMapping("/js/pages/room_detail/index.js")
    public String getRoomJs() {
        return "pages/room_detail/index.js";
    }
}
