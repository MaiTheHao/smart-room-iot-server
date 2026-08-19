package com.iviet.ivshs.service.strategy;

import com.iviet.ivshs.dto.EvaluationResult;
import com.iviet.ivshs.entities.Condition;
import java.util.List;

public interface ConditionEvaluationService {

  boolean evaluate(Condition condition, Long contextId);

  EvaluationResult evaluateAll(List<Condition> conditions, Long contextId);
}
