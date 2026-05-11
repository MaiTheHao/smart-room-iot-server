package com.iviet.ivshs.controller.view;

import com.iviet.ivshs.service.LoginViewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class LoginViewController {

    private final LoginViewService loginViewService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(name = "error", required = false) Boolean error,
            HttpServletRequest request,
            Model model) {
        var loginPageData = loginViewService.getModel(request, error);
        model.addAllAttributes(loginPageData.toModelAttributes());
        return "pages/login.html";
    }
}
