package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.service.AlertMessageTemplateService;
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
                if (val == null) {
                    matcher.appendReplacement(result, Matcher.quoteReplacement("[NULL: " + key + "]"));
                } else if (val.toString().isEmpty()) {
                    matcher.appendReplacement(result, Matcher.quoteReplacement("[EMPTY: " + key + "]"));
                } else {
                    try {
                        matcher.appendReplacement(result, Matcher.quoteReplacement(val.toString()));
                    } catch (Exception e) {
                        matcher.appendReplacement(result, Matcher.quoteReplacement("[ERROR: " + key + " - " + e.getMessage() + "]"));
                    }
                }
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement("[NOT_FOUND: " + key + "]"));
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
