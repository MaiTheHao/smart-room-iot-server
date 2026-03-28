package com.iviet.ivshs.enumeration;

public enum FanType {
  GPIO,
  IR;

  public static FanType fromString(String value) {
    if (value == null) {
      return null;
    }
    try {
      return FanType.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid fan type: " + value);
    }
  }
}
