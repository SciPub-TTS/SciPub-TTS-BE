package com.brotherhood.scipubtts.dashboard.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TopicStatus {
  BREAKOUT(100),
  HOT(50),
  RISING(Double.NEGATIVE_INFINITY);

  private final double threshold;

  TopicStatus(double threshold) {
    this.threshold = threshold;
  }

  public static TopicStatus fromChange(double change) {
    for (TopicStatus trend : values()) {
      if (change >= trend.threshold) {
        return trend;
      }
    }
    return RISING;
  }

  @JsonValue
  public String toJson() {
    return name().toLowerCase();
  }
}