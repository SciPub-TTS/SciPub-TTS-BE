package com.brotherhood.scipubtts.feed.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.brotherhood.scipubtts.dashboard.dto.request.TopicDataRequest;
import com.brotherhood.scipubtts.dashboard.service.TopicService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.brotherhood.scipubtts.feed.controller.FeedTab;
import com.brotherhood.scipubtts.feed.dto.response.*;
import com.brotherhood.scipubtts.feed.entity.ResearchFeed;
import com.brotherhood.scipubtts.feed.repository.ResearchFeedJpaRepository;
import com.brotherhood.scipubtts.feed.service.FeedService;
import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import com.brotherhood.scipubtts.follow.entity.UserFollow;
import com.brotherhood.scipubtts.follow.repository.UserFollowRepository;
import com.brotherhood.scipubtts.feed.dto.response.FeedEntityRefResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final UserFollowRepository userFollowRepository;
    private final ResearchFeedJpaRepository researchFeedJpaRepository;
    private final TopicService topicService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FeedResponse getFeed(
            UUID userId,
            FeedTab feedTab,
            int page,
            int pageSize,
            String exactMatchType,
            String exactMatchId,
            String exactMatchName
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "generated_at");
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<ResearchFeed> feedPage = researchFeedJpaRepository.findUserFeed(
                userId,
                feedTab.name(),
                normalizeExactMatchType(exactMatchType),
                normalizeOpenAlexId(exactMatchId),
                normalizeExactMatchName(exactMatchName),
                pageable
        );

        List<FeedItemResponse> items = feedPage.getContent().stream()
                .map(this::mapToFeedItemResponse)
                .toList();

        return FeedResponse.builder()
                .items(items)
                .totalItems(feedPage.getTotalElements())
                .build();
    }

    @Override
    public List<FollowedTopicResponse> getFollowedTopics(UUID userId) {
        List<UserFollow> follows = userFollowRepository.findByUserIdAndTargetType(userId, FollowTargetType.TOPIC);
        return follows.stream()
                .map(follow -> FollowedTopicResponse.builder()
                        .id(follow.getTargetOpenAlexId())
                        .name(follow.getDisplayNameSnapshot() != null ? follow.getDisplayNameSnapshot()
                                : "Unknown Topic")
                        .build())
                .toList();
    }

    @Override
    public List<FollowedAuthorResponse> getFollowedAuthors(UUID userId) {
        List<UserFollow> follows = userFollowRepository.findByUserIdAndTargetType(userId, FollowTargetType.AUTHOR);
        return follows.stream()
                .map(follow -> FollowedAuthorResponse.builder()
                        .id(follow.getTargetOpenAlexId())
                        .name(follow.getDisplayNameSnapshot() != null ? follow.getDisplayNameSnapshot()
                                : "Unknown Author")
                        .build())
                .toList();
    }

    @Override
    public SuggestedTopicResponse getSuggestedTopics(TopicDataRequest request) {
        return topicService.getSuggestTopic(request);
    }

    private FeedItemResponse mapToFeedItemResponse(ResearchFeed item) {
        JsonNode reasonNode = parseReasonJson(item.getReasonJson());
        List<String> authorNames = parseAuthorNames(item.getAuthorsSnapshot());
        List<FeedEntityRefResponse> authorRefs = parseAuthorRefs(
                item.getAuthorsSnapshot(),
                item.getAuthorOpenAlexIdsSnapshot()
        );
        List<String> keywords = parseKeywords(item.getKeywordsJson());
        FeedEntityRefResponse topicRef = mapTopicRef(
                item.getTopicOpenAlexIdSnapshot(),
                item.getTopicSnapshot()
        );

        return FeedItemResponse.builder()
                .id(item.getWorkOpenAlexId())
                .title(item.getTitleSnapshot())
                .abstractText(item.getAbstractText())
                .doi(item.getDoi())
                .publicationYear(item.getPublicationYear())
                .citedByCount(item.getCitationSnapshot() != null ? item.getCitationSnapshot() : 0)
                .openAccess(null)
                .hasPdf(StringUtils.hasText(item.getPdfUrl()))
                .pdfUrl(item.getPdfUrl())
                .hasOrcid(null)
                .type(item.getWorkTypeSnapshot())
                .topic(item.getTopicSnapshot())
                .subFieldName(item.getSubfieldSnapshot())
                .sourceId(null)
                .sourceName(item.getSourceSnapshot())
                .authors(authorNames)
                .authorRefs(authorRefs)
                .keywords(keywords)
                .topicRef(topicRef)
                .matchesTrendingKeyword(false)
                .matchesTrendingTopic(false)
                .trendingScore(0.0)
                .relevance(extractRelevance(reasonNode))
                .reason(extractReason(reasonNode))
                .tabMatches(extractTabMatches(reasonNode))
                .build();
    }

    private String normalizeExactMatchType(String exactMatchType) {
        if (!StringUtils.hasText(exactMatchType)) {
            return null;
        }

        String normalizedType = exactMatchType.trim().toUpperCase();
        if (!"AUTHOR".equals(normalizedType) && !"TOPIC".equals(normalizedType)) {
            return null;
        }

        return normalizedType;
    }

    private String normalizeOpenAlexId(String openAlexId) {
        if (!StringUtils.hasText(openAlexId)) {
            return null;
        }

        String normalizedId = openAlexId.trim();
        int lastSlash = normalizedId.lastIndexOf("/");
        if (lastSlash >= 0) {
            normalizedId = normalizedId.substring(lastSlash + 1);
        }

        return StringUtils.hasText(normalizedId) ? normalizedId : null;
    }

    private String normalizeExactMatchName(String exactMatchName) {
        if (!StringUtils.hasText(exactMatchName)) {
            return null;
        }

        String normalizedName = exactMatchName.trim();
        return StringUtils.hasText(normalizedName) ? normalizedName : null;
    }

    private int extractRelevance(JsonNode reasonNode) {
        JsonNode reasons = reasonNode.path("reasons");

        if (!reasons.isArray() || reasons.isEmpty()) {
            return 75;
        }

        int reasonCount = reasons.size();
        return Math.min(75 + (reasonCount * 10), 100);
    }

    private List<String> parseAuthorNames(String authorsSnapshot) {
        return splitCommaSeparatedValues(authorsSnapshot);
    }

    private List<FeedEntityRefResponse> parseAuthorRefs(
            String authorsSnapshot,
            String authorOpenAlexIdsSnapshot
    ) {
        List<String> authorNames = splitCommaSeparatedValues(authorsSnapshot);
        List<String> authorIds = splitCommaSeparatedValues(authorOpenAlexIdsSnapshot);
        List<FeedEntityRefResponse> authorRefs = new ArrayList<>();

        for (int index = 0; index < authorNames.size(); index++) {
            String authorId = index < authorIds.size() ? authorIds.get(index) : null;
            authorRefs.add(FeedEntityRefResponse.builder()
                    .id(StringUtils.hasText(authorId) ? authorId : null)
                    .displayName(authorNames.get(index))
                    .build());
        }

        return authorRefs;
    }

    private List<String> parseKeywords(String keywordsJson) {
        if (!StringUtils.hasText(keywordsJson)) {
            return List.of();
        }

        try {
            return objectMapper.readValue(keywordsJson, new TypeReference<List<String>>() {
            });
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private FeedEntityRefResponse mapTopicRef(String topicId, String topicName) {
        if (!StringUtils.hasText(topicName)) {
            return null;
        }

        return FeedEntityRefResponse.builder()
                .id(StringUtils.hasText(topicId) ? topicId : null)
                .displayName(topicName)
                .build();
    }

    private List<String> splitCommaSeparatedValues(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return List.of();
        }

        return Arrays.stream(rawValue.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private JsonNode parseReasonJson(String reasonJson) {
        if (reasonJson == null || reasonJson.isBlank()) {
            return objectMapper.missingNode();
        }
        try {
            return objectMapper.readTree(reasonJson);
        } catch (Exception e) {
            return objectMapper.missingNode();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    private String extractReason(JsonNode reasonNode) {

        JsonNode reasons = reasonNode.path("reasons");

        List<String> names = new ArrayList<>();
        for (JsonNode reason : reasons) {
            String display = reason.path("displayName").asText();
            if (!display.isBlank()) {
                names.add(display);
            }
        }

        if (!names.isEmpty()) {
            return "Matched: " + String.join(", ", names);
        }

        return "Recommended based on your followed profile filters.";
    }

    private List<String> extractTabMatches(JsonNode reasonNode) {
        List<String> tabMatches = new ArrayList<>();
        tabMatches.add("all");

        JsonNode reasons = reasonNode.path("reasons");
        boolean matchesTopic = false;
        boolean matchesAuthor = false;

        if (reasons.isArray()) {
            for (JsonNode reason : reasons) {
                String type = reason.path("type").asText();
                if ("TOPIC".equals(type)) matchesTopic = true;
                if ("AUTHOR".equals(type)) matchesAuthor = true;
            }
        }

        if (matchesTopic) tabMatches.add("matched-topic");
        if (matchesAuthor) tabKeyMatch(tabMatches, "matched-author");
        return tabMatches;
    }

    private void tabKeyMatch(List<String> list, String val) {
        if (!list.contains(val))
            list.add(val);
    }
}
