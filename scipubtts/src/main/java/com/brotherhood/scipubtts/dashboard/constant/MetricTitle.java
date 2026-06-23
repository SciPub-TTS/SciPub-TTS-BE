package com.brotherhood.scipubtts.dashboard.constant;

import lombok.Getter;

@Getter
public enum MetricTitle {

  TOTAL_PAPERS("TOTAL PAPERS"),
  ACTIVE_TRENDING_TOPICS("ACTIVE TRENDING TOPICS"),
  RISING_KEYWORDS("RISING KEYWORDS"),
  AVERAGE_GROWTH_RATE("AVERAGE GROWTH RATE"),
  CITATION_IMPACT("CITATION IMPACT"),
  TOP_FIELD("TOP FIELD"),
  NEW_PAPERS_THIS_WEEK("NEW PAPERS THIS WEEK"),
  LAST_SYNC("LAST SYNC");

  private final String title;

  MetricTitle(String title) {
    this.title = title;
  }

    public static MetricTitle fromTitle(String title) {
    for (MetricTitle metric : values()) {
      if (metric.title.equalsIgnoreCase(title)) {
        return metric;
      }
    }
    throw new IllegalArgumentException("Unknown metric title: " + title);
  }
}