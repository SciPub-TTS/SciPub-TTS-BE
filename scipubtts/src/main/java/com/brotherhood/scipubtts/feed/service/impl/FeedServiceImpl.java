package com.brotherhood.scipubtts.feed.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final UserFollowRepository userFollowRepository;
    private final ResearchFeedJpaRepository researchFeedJpaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FeedResponse getFeed(UUID userId, FeedTab feedTab, int page, int pageSize) {
        // Sort keys must refer to database column names when executing native queries
        Sort sort = switch (feedTab) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "publication_date", "generated_at");
            case TRENDING -> Sort.by(Sort.Direction.DESC, "citation_snapshot");
            case RELEVANT -> Sort.by(Sort.Direction.DESC, "relevance_score");
            default -> Sort.by(Sort.Direction.DESC, "generated_at");
        };

        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<ResearchFeed> feedPage = researchFeedJpaRepository.findUserFeed(userId, feedTab.name(), pageable);

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
                        .status("Stable")
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
                        .field("Researcher")
                        .build())
                .toList();
    }

    @Override
    public List<SuggestedTopicResponse> getSuggestedTopics(UUID userId) {
        return List.of(
                SuggestedTopicResponse.builder().id("T11116").name("Academic Publishing and Open Access").build(),
                SuggestedTopicResponse.builder().id("T11224").name("AI Policy in Higher Education").build(),
                SuggestedTopicResponse.builder().id("T10123").name("Large Language Models").build());
    }

    private FeedItemResponse mapToFeedItemResponse(ResearchFeed item) {
        String doiUrl = item.getWorkOpenAlexId() != null ? item.getWorkOpenAlexId() : "";
        String doiLabel = doiUrl.replace("https://doi.org/", "");

        Double score = item.getRelevanceScore() != null ? item.getRelevanceScore() : 0.85;
        int relevance = (int) (score > 1.0 ? score : score * 100);

        JsonNode reasonNode = parseReasonJson(item.getReasonJson());

        return FeedItemResponse.builder()
                .id(item.getId().toString())
                .relevance(relevance)
                .badges(extractBadges(reasonNode, item.getSourceSnapshot()))
                .year(item.getPublicationYear() != null ? item.getPublicationYear() : 2025)
                .title(item.getTitleSnapshot())
                .authors(parseAuthors(item.getAuthorsSnapshot()))
                .extraAuthors(0)
                .venue(item.getSourceSnapshot() != null ? item.getSourceSnapshot() : "Unknown Publisher")
                .citations(item.getCitationSnapshot() != null ? item.getCitationSnapshot() : 0)
                .articleAbstract(
                        "Publication metadata references and full-text resources are accessible through the DOI publisher link.")
                .reason(extractReason(reasonNode))
                .tabMatches(extractTabMatches(reasonNode))
                .tags(extractTags(reasonNode))
                .doiUrl(doiUrl)
                .doiLabel(doiLabel)
                .build();
    }

    private List<FeedAuthorResponse> parseAuthors(String authorsSnapshot) {
        if (authorsSnapshot == null || authorsSnapshot.isBlank()) {
            return List.of();
        }
        try {
            if (authorsSnapshot.trim().startsWith("[")) {
                return objectMapper.readValue(authorsSnapshot, new TypeReference<List<FeedAuthorResponse>>() {
                });
            }
        } catch (Exception ignored) {
        }

        return Arrays.stream(authorsSnapshot.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> FeedAuthorResponse.builder().name(name).following(false).build())
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

    private List<String> extractTags(JsonNode reasonNode) {

        JsonNode reasons = reasonNode.path("reasons");
        List<String> tags = new ArrayList<>();

        if (reasons.isArray()) {
            for (JsonNode reason : reasons) {
                String display = reason.path("displayName").asText();
                if (!display.isBlank()) {
                    tags.add(display);
                }
            }
        }

        return tags;
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
        if (matchesTopic && matchesAuthor) tabMatches.add("matched-both");

        tabMatches.add("latest");
        tabMatches.add("trending");
        tabMatches.add("most-relevant");
        return tabMatches;
    }

    private void tabKeyMatch(List<String> list, String val) {
        if (!list.contains(val))
            list.add(val);
    }

    private List<FeedBadgeResponse> extractBadges(JsonNode reasonNode, String source) {
        List<FeedBadgeResponse> badges = new ArrayList<>();

        if (source != null && !source.isBlank()) {
            badges.add(FeedBadgeResponse.builder().label(source).tone("topic").build());
        }

        JsonNode reasons = reasonNode.path("reasons");
        if (reasons.isArray()) {
            for (JsonNode reason : reasons) {
                String type = reason.path("type").asText();

                if ("AUTHOR".equals(type)) {
                    badges.add(FeedBadgeResponse.builder().label("Matched Author").tone("author").build());
                } else if ("TOPIC".equals(type)) {
                    badges.add(FeedBadgeResponse.builder().label("Matched Topic").tone("topic").build());
                }
            }
        }

        if (badges.isEmpty()) {
            badges.add(FeedBadgeResponse.builder().label("Relevant Option").tone("match").build());
        }

        return badges;
    }
}
