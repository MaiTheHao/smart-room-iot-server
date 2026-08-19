package com.iviet.ivshs.dto;

public sealed interface ConditionValue {

  record NumericValue(double value) implements ConditionValue {}

  record TextValue(String value) implements ConditionValue {}

  record MissingValue() implements ConditionValue {}

  static ConditionValue of(Object raw) {
    if (raw == null) {
      return new MissingValue();
    }
    if (raw instanceof Number n) {
      return new NumericValue(n.doubleValue());
    }
    if (raw instanceof Enum<?> e) {
      return new TextValue(e.name());
    }
    return new TextValue(raw.toString());
  }
}
