package com.iviet.ivshs.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@UtilityClass
public class LocalContextUtil {

    public final String LANG_VI = "vi";
    public final String LANG_EN = "en";
    
    public final Locale DEFAULT_LOCALE = Locale.of("vi", "VN");
    public final String DEFAULT_LANG_CODE = LANG_VI;

    @NonNull
    public Locale getCurrentLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        return (locale != null) ? locale : DEFAULT_LOCALE;
    }

    public String getCurrentLangCode() {
        return Optional.of(getCurrentLocale().getLanguage())
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .orElse(DEFAULT_LANG_CODE);
    }

    public String getCurrentLangCodeFromRequest() {
        HttpServletRequest req = RequestContextUtil.getCurrentRequest();
        if (req != null) {
            String langParam = req.getParameter("lang");
            if (StringUtils.hasText(langParam)) {
                return langParam.trim().toLowerCase();
            }
        }
        log.warn("Could not determine lang code from request, falling back to default.");
        return DEFAULT_LANG_CODE;
    }

    public String resolveLangCode(String langCode) {
        return StringUtils.hasText(langCode) 
                ? langCode.trim().toLowerCase() 
                : getCurrentLangCode();
    }

    @NonNull
    public Locale createLocaleFromLangCode(String langCode) {
        String resolvedLang = resolveLangCode(langCode);
        String country = mapLangToCountry(resolvedLang);
        Locale locale = Locale.of(resolvedLang, country);

        if (locale == null) locale = DEFAULT_LOCALE;

        return locale;
    }

    private String mapLangToCountry(String langCode) {
        return switch (langCode) {
            case LANG_VI -> "VN";
            case LANG_EN -> "US";
            case "ja" -> "JP";
            case "zh" -> "CN";
            case "fr" -> "FR";
            case "de" -> "DE";
            case "es" -> "ES";
            default -> langCode.toUpperCase();
        };
    }

    public void setLocaleFromRequest(@NonNull HttpServletRequest request, @NonNull HttpSession session, Object localeResolver) {
        String langParam = request.getParameter("lang");
        
        if (StringUtils.hasText(langParam)) {
            String resolvedLang = resolveLangCode(langParam);
            Locale locale = createLocaleFromLangCode(resolvedLang);
            
            if (localeResolver instanceof org.springframework.web.servlet.LocaleResolver) {
                ((org.springframework.web.servlet.LocaleResolver) localeResolver).setLocale(request, null, locale);
            }
            LocaleContextHolder.setLocale(locale);
            session.setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
        } else {
            Locale locale = DEFAULT_LOCALE;
            Locale sessionLocale = (Locale) session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
            if (sessionLocale != null) {
                locale = sessionLocale;
            }
            LocaleContextHolder.setLocale(locale);
        }
    }
}