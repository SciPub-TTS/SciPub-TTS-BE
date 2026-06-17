package com.brotherhood.scipubtts.dashboard.dto.response;

import com.brotherhood.scipubtts.dashboard.entity.Topic;

import java.util.List;

public record TopicCalculateResponse(
        List<Topic> topicList
) {
}
