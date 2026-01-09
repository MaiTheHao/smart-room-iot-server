package com.iviet.ivshs.controller.view;

import com.iviet.ivshs.constant.I18nMessageConstant;
import com.iviet.ivshs.dto.LoginViewModel;
import com.iviet.ivshs.service.I18nMessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class LoginViewController {

    private final I18nMessageService i18nMessageService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(name = "error", required = false) String error,
            HttpServletRequest request,
            Model model) {
        var loginPageData = buildLoginPageData(error, request);
        model.addAllAttributes(loginPageData.toModelAttributes());
        return "pages/login.html";
    }

    private LoginViewModel buildLoginPageData(String error, HttpServletRequest request) {
        Optional<String> errorMessage = Optional.empty();
        
        if (error != null) {
            Object exception = request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            String messageKey = I18nMessageConstant.LOGIN_ERROR_UNKNOWN;

            if (exception instanceof AuthenticationException authEx) {
                String msg = authEx.getMessage();
                if (msg != null) {
                    if (msg.contains("Bad credentials")) messageKey = I18nMessageConstant.LOGIN_ERROR_BAD_CREDENTIALS;
                    else if (msg.contains("disabled")) messageKey = I18nMessageConstant.LOGIN_ERROR_USER_DISABLED;
                    else if (msg.contains("expired")) messageKey = I18nMessageConstant.LOGIN_ERROR_ACCOUNT_EXPIRED;
                    else if (msg.contains("locked")) messageKey = I18nMessageConstant.LOGIN_ERROR_ACCOUNT_LOCKED;
                    else if (msg.contains("not found")) messageKey = I18nMessageConstant.LOGIN_ERROR_USER_NOT_FOUND;
                    else if (msg.contains("Client type is not USER")) messageKey = I18nMessageConstant.LOGIN_ERROR_INVALID_CLIENT_TYPE;
                }
            }
            errorMessage = Optional.of(i18nMessageService.getMessage(messageKey));
        }
        
        return LoginViewModel.builder()
			.errorMessage(errorMessage)
			.build();
    }
}
