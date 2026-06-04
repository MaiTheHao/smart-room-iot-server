package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.shared.constant.I18nMessageConstant;
import com.iviet.ivshs.dto.LoginViewModel;
import com.iviet.ivshs.service.I18nMessageService;
import com.iviet.ivshs.service.LoginViewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginViewServiceImpl implements LoginViewService {

    private final I18nMessageService i18nMessageService;

    @Override
    public LoginViewModel getModel(HttpServletRequest request, Boolean isError) {
        Optional<String> errorMessage = Optional.empty();

        if (isError != null && isError) {
            Object exception = request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
            String messageKey = I18nMessageConstant.LOGIN_ERROR_UNKNOWN;

            if (exception instanceof AuthenticationException authEx) {
                String msg = authEx.getMessage();
                if (msg != null) {
                    if (msg.contains("Bad credentials"))
                        messageKey = I18nMessageConstant.LOGIN_ERROR_BAD_CREDENTIALS;
                    else if (msg.contains("disabled"))
                        messageKey = I18nMessageConstant.LOGIN_ERROR_USER_DISABLED;
                    else if (msg.contains("expired"))
                        messageKey = I18nMessageConstant.LOGIN_ERROR_ACCOUNT_EXPIRED;
                    else if (msg.contains("locked"))
                        messageKey = I18nMessageConstant.LOGIN_ERROR_ACCOUNT_LOCKED;
                    else if (msg.contains("not found"))
                        messageKey = I18nMessageConstant.LOGIN_ERROR_USER_NOT_FOUND;
                    else if (msg.contains("Client type is not USER"))
                        messageKey = I18nMessageConstant.LOGIN_ERROR_INVALID_CLIENT_TYPE;
                }
            }
            errorMessage = Optional.of(i18nMessageService.getMessage(messageKey));
        }

        return LoginViewModel.builder()
                .errorMessage(errorMessage)
                .build();
    }
}
