package com.brotherhood.scipubtts.feed.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record FeedItemResponse(
    Long paperId,
    String title,
    String abstractText,
    String sourceName,
    List<String> authors,
    List<String> topics,
    Double score,
    List<String> matchedReasons) {
}
