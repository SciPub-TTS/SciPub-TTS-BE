package com.brotherhood.scipubtts.feed.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import java.util.List;

@Builder
public record FeedItemResponse(
        String id,
        Integer relevance,
        List<FeedBadgeResponse> badges,
        Integer year,
        String title,
        List<FeedAuthorResponse> authors,
        Integer extraAuthors,
        String venue,
        Integer citations,
        @JsonProperty("abstract") String articleAbstract,
        String reason,
        List<String> tabMatches,
        List<String> tags,
        String doiUrl,
        String doiLabel
) {}
