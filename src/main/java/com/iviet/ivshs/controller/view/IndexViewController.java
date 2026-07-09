package com.iviet.ivshs.controller.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.iviet.ivshs.dto.IndexViewModel;
import com.iviet.ivshs.service.IndexViewService;

@Controller
@RequiredArgsConstructor
public class IndexViewController {

	private final IndexViewService indexViewService;

	@GetMapping({
			"",
			"/",
			"/index"
	})
	public String index(Model model) {
		IndexViewModel _model = indexViewService.getModel();
		model.addAllAttributes(_model.toModelAttributes());
		return "pages/index.html";
	}

	@GetMapping("/js/pages/index.js")
	public String getIndexJs() {
		return "pages/index.js";
	}
}
