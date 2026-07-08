package com.brotherhood.scipubtts.feed.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record FeedItemResponse(
        String id,
        String title,
        String abstractText,
        String doi,
        Integer publicationYear,
        Integer citedByCount,
        Boolean openAccess,
        Boolean hasPdf,
        String pdfUrl,
        Boolean hasOrcid,
        String type,
        String topic,
        String subFieldName,
        String sourceId,
        String sourceName,
        List<String> authors,
        List<FeedEntityRefResponse> authorRefs,
        List<String> keywords,
        FeedEntityRefResponse topicRef,
        Boolean matchesTrendingKeyword,
        Boolean matchesTrendingTopic,
        Double trendingScore,
        Integer relevance,
        String reason,
        List<String> tabMatches
) {}
