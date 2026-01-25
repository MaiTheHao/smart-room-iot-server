package com.iviet.ivshs.util;

import org.springframework.context.MessageSource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @deprecated Use I18nMessageService instead.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Deprecated
public class I18nMessageUtil {

    private final MessageSource messageSource;

    @NonNull
    public String getMessage(@NonNull String key, Object... args) {
        String message = messageSource.getMessage(
                key, 
                args, 
                key,
                LocalContextUtil.getCurrentLocale()
        );
        return message != null ? message : key;
    }
}