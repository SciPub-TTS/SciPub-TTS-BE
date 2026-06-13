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
}