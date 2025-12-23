package com.iviet.ivshs.util;

import lombok.experimental.UtilityClass;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;

@UtilityClass
public class LocalContextUtil {

    public final String LANG_VI = "vi";
    public final String LANG_EN = "en";
    public final String DEFAULT_LANG_CODE = LANG_VI;

    /**
     * Lấy mã ngôn ngữ hiện tại từ Context.
     */
    public String getCurrentLangCode() {
        return Optional.ofNullable(LocaleContextHolder.getLocale().getLanguage())
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .orElse(DEFAULT_LANG_CODE);
    }

    /**
     * Resolve mã ngôn ngữ từ input, fallback về context hiện tại.
     */
    public String resolveLangCode(String langCode) {
        return StringUtils.hasText(langCode) 
                ? langCode.trim().toLowerCase() 
                : getCurrentLangCode();
    }

    /**
     * Kiểm tra xem mã ngôn ngữ input có khớp với ngôn ngữ hiện tại không.
     */
    public boolean isCurrentLang(String langCode) {
        return StringUtils.hasText(langCode) 
                && langCode.trim().equalsIgnoreCase(getCurrentLangCode());
    }

    /**
     * Lấy Locale object hiện tại.
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
}