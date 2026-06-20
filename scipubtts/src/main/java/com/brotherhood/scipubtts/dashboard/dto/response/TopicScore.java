package com.brotherhood.scipubtts.dashboard.dto.response;

import com.brotherhood.scipubtts.dashboard.entity.Topic;

public record TopicScore(
        Topic topic,
        Double score
) {}