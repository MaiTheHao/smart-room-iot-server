package com.iviet.ivshs.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.iviet.ivshs.service.HomeViewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeViewController {
	
	private final HomeViewService homeViewService;

	@GetMapping("/home")
	public String home(Model model) {

		var dashboardData = homeViewService.getModel();
		model.addAllAttributes(dashboardData.toModelAttributes());
		
		return "pages/home.html";
	}
}
