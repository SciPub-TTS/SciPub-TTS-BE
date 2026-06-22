package com.brotherhood.scipubtts.search.dto;

import java.time.LocalDate;
import java.util.List;

public record HotTopicResponse(
        LocalDate snapshotDate,
        List<HotTopicItemResponse> topics
) {
}
