package com.iviet.ivshs.service;

import java.util.Map;
import java.util.Set;

public interface AlertMessageTemplateService {

  String buildMessage(String template, Map<String, Object> data);

  Set<String> extractVariables(String template);
}
