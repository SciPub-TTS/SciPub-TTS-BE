package com.brotherhood.scipubtts.dashboard.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.brotherhood.scipubtts.dashboard.entity.Topic;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopicHotFilterResponse {
    private List<Topic> topicIdList;

    public List<Topic> topicIdList() {
        return topicIdList;
    }
}
