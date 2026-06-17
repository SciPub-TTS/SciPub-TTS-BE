package com.brotherhood.scipubtts.dashboard.statistic;

public record TopicMetricStatistic(
        double velocityMin,
        double velocityMax,

        double accelerationMin,
        double accelerationMax,

        double citationMin,
        double citationMax,

        double newcomerMin,
        double newcomerMax,

        double institutionMin,
        double institutionMax
) {
  public static TopicMetricStatistic empty() {
    return new TopicMetricStatistic(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
  }
}