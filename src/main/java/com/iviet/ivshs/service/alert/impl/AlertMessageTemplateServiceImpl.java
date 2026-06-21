package com.iviet.ivshs.service.alert.impl;

import com.iviet.ivshs.service.alert.AlertMessageTemplateService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AlertMessageTemplateServiceImpl implements AlertMessageTemplateService {

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    @Override
    public String buildMessage(String template, Map<String, Object> data) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        Map<String, Object> safeData = data != null ? data : Collections.emptyMap();
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            if (safeData.containsKey(key)) {
                Object val = safeData.get(key);
                matcher.appendReplacement(result, Matcher.quoteReplacement(val != null ? val.toString() : ""));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    @Override
    public Set<String> extractVariables(String template) {
        if (template == null || template.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> variables = new LinkedHashSet<>();
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        while (matcher.find()) {
            variables.add(matcher.group(1).trim());
        }
        return variables;
    }
}
