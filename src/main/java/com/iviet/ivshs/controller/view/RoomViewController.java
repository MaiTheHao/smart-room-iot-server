package com.iviet.ivshs.controller.view;

import com.iviet.ivshs.service.RoomViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class RoomViewController {

	private final RoomViewService roomViewService;

	@GetMapping("/room/{id}")
	public String roomDetail(
			@PathVariable(name = "id") Long roomId,
			@RequestParam(name = "startedAt", required = false) String startedAtStr,
			@RequestParam(name = "endedAt", required = false) String endedAtStr,
			Model model) {
		var roomDetailModel = roomViewService.getRoomDetailModel(roomId, startedAtStr, endedAtStr);
		model.addAllAttributes(roomDetailModel.toModelAttributes());
		return "pages/room_detail.html";
	}
}
