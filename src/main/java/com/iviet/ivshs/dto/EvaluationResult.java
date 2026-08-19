package com.iviet.ivshs.dto;

import java.util.Map;
import lombok.Builder;

@Builder
public record EvaluationResult(boolean isMatched, Map<String, Object> templateData) {

  public static EvaluationResult empty() {
    return new EvaluationResult(false, Map.of());
  }

  public static EvaluationResult of(boolean isMatched, Map<String, Object> templateData) {
    return new EvaluationResult(isMatched, templateData != null ? templateData : Map.of());
  }
}
