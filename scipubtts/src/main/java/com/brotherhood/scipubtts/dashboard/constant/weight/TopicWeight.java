package com.brotherhood.scipubtts.dashboard.constant.weight;

public record TopicWeight(
        double velocity,
        double acceleration,
        double citationDecay,
        double newcomerAuthor,
        double institution
) {
}