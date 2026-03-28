package com.iviet.ivshs.controller.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class IndexController {
	
	@GetMapping({"", "/"})
	public String index() {
		return "redirect:/home";
	}
}
