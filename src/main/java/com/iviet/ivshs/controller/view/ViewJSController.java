package com.iviet.ivshs.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewJSController {

    @GetMapping("/js/pages/layout.js")
    public String getLayoutJs() {
        return "pages/layout.js";
    }
}
