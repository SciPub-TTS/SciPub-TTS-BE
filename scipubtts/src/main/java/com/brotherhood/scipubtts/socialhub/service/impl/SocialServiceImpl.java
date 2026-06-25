package com.brotherhood.scipubtts.socialhub.service.impl;

import com.brotherhood.scipubtts.bookmark.entity.UserBookmark;
import com.brotherhood.scipubtts.bookmark.repository.UserBookmarkRepository;
import com.brotherhood.scipubtts.bookmark.support.BookmarkSnapshotSupport;
import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.search.service.OpenAlexMapReader;
import com.brotherhood.scipubtts.search.service.SearchQuerySupport;
import com.brotherhood.scipubtts.socialhub.dto.request.CreateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.request.UpdateSocialPostRequest;
import com.brotherhood.scipubtts.socialhub.dto.response.LikeToggleResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostDetailResponse;
import com.brotherhood.scipubtts.socialhub.dto.response.SocialPostSummaryResponse;
import com.brotherhood.scipubtts.socialhub.entity.SocialPost;
import com.brotherhood.scipubtts.socialhub.entity.SocialPostLike;
import com.brotherhood.scipubtts.socialhub.entity.SocialPostReference;
import com.brotherhood.scipubtts.socialhub.repository.SocialPostLikeRepository;
import com.brotherhood.scipubtts.socialhub.repository.SocialPostReferenceRepository;
import com.brotherhood.scipubtts.socialhub.repository.SocialPostRepository;
import com.brotherhood.scipubtts.socialhub.service.SocialService;
import com.brotherhood.scipubtts.user.entity.User;
import com.brotherhood.scipubtts.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {

    private static final int MAX_REFERENCES = 3;
    private static final int BODY_PREVIEW_LENGTH = 200;
    private static final String OPENALEX_WORK_TYPE_SELECT_FIELDS = "id,type";

    private final SocialPostRepository postRepository;
    private final SocialPostLikeRepository likeRepository;
    private final SocialPostReferenceRepository referenceRepository;
    private final UserRepository userRepository;
    private final UserBookmarkRepository bookmarkRepository;
    private final EntityManager entityManager;
    private final BookmarkSnapshotSupport snapshotSupport;
    private final OpenAlexClient openAlexClient;
    private final OpenAlexMapReader openAlexMapReader;
    private final SearchQuerySupport searchQuerySupport;

    @Override
    @Transactional
    public SocialPostDetailResponse createPost(UUID authorId, CreateSocialPostRequest request) {
        if (!StringUtils.hasText(request.title()) || !StringUtils.hasText(request.body())) {
            throw new BusinessException(ErrorCode.SOCIAL_POST_TITLE_OR_BODY_BLANK);
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<UserBookmark> validBookmarks = extractBookmarksForReferences(authorId, request.references());
        Map<String, String> workTypeSnapshotsByOpenAlexId =
                resolveBookmarkWorkTypeSnapshots(validBookmarks);

        SocialPost post = SocialPost.builder()
                .author(author)
                .title(request.title().trim())
                .body(request.body().trim())
                .topicTag(snapshotSupport.normalizeText(request.topicTag()))
                .build();

        post.setReferences(validBookmarks.stream()
                .map(bookmark -> buildReferenceFromBookmark(
                        post,
                        bookmark,
                        workTypeSnapshotsByOpenAlexId.get(
                                normalizeOpenAlexId(bookmark.getOpenAlexId())
                        )
                ))
                .collect(Collectors.toList()));

        SocialPost saved = postRepository.save(post);
        return toDetailResponse(saved, false, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SocialPostSummaryResponse> getNewest(Pageable pageable, UUID viewerId) {
        return toSummaryPage(
                postRepository.findAllByOrderByCreatedAtDesc(pageable),
                viewerId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SocialPostSummaryResponse> getTop(Pageable pageable, UUID viewerId) {
        return toSummaryPage(
                postRepository.findAllByOrderByLikeCountDescCreatedAtDesc(pageable),
                viewerId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SocialPostDetailResponse getPostDetail(UUID postId, UUID viewerId) {
        SocialPost post = findActivePost(postId);
        boolean liked = viewerId != null && likeRepository.existsByPostIdAndUserId(postId, viewerId);
        return toDetailResponse(post, liked, false);
    }

    @Override
    @Transactional
    public SocialPostDetailResponse updatePost(UUID postId, UUID editorId, UpdateSocialPostRequest request) {
        SocialPost post = findActivePost(postId);
        assertIsAuthor(post, editorId);

        if (StringUtils.hasText(request.title())) {
            post.setTitle(request.title().trim());
        }

        if (StringUtils.hasText(request.body())) {
            post.setBody(request.body().trim());
        }

        if (request.topicTag() != null) {
            post.setTopicTag(snapshotSupport.normalizeText(request.topicTag()));
        }

        boolean likesReset = false;

        if (request.references() != null) {
            Set<String> previousReferenceIds = new HashSet<>(
                    referenceRepository.findOpenalexIdByPostId(postId)
            );
            Set<String> nextReferenceIds = request.references().stream()
                    .map(this::normalizeOpenAlexId)
                    .collect(Collectors.toSet());

            if (!previousReferenceIds.equals(nextReferenceIds)) {
                List<UserBookmark> validBookmarks = extractBookmarksForReferences(
                        editorId,
                        request.references()
                );
                Map<String, String> workTypeSnapshotsByOpenAlexId =
                        resolveBookmarkWorkTypeSnapshots(validBookmarks);

                likeRepository.deleteAllByPostId(postId);
                post.resetLikeCount();
                likesReset = true;

                post.getReferences().clear();
                post.getReferences().addAll(validBookmarks.stream()
                        .map(bookmark -> buildReferenceFromBookmark(
                                post,
                                bookmark,
                                workTypeSnapshotsByOpenAlexId.get(
                                        normalizeOpenAlexId(bookmark.getOpenAlexId())
                                )
                        ))
                        .toList());
            }
        }

        SocialPost updated = postRepository.save(post);
        boolean liked = !likesReset && likeRepository.existsByPostIdAndUserId(postId, editorId);

        return toDetailResponse(updated, liked, likesReset);
    }

    @Override
    @Transactional
    public void deletePost(UUID postId, UUID requesterId) {
        SocialPost post = findActivePost(postId);
        assertIsAuthor(post, requesterId);
        post.softDelete();
        postRepository.save(post);
    }

    @Override
    @Transactional
    public LikeToggleResponse toggleLike(UUID postId, UUID userId) {
        SocialPost post = findActivePost(postId);
        Optional<SocialPostLike> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            postRepository.decrementLikeCount(postId);
            entityManager.refresh(post);
            return new LikeToggleResponse(false, post.getLikeCount());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        likeRepository.save(SocialPostLike.builder()
                .post(post)
                .user(user)
                .build());
        postRepository.incrementLikeCount(postId);
        entityManager.refresh(post);

        return new LikeToggleResponse(true, post.getLikeCount());
    }

    private List<UserBookmark> extractBookmarksForReferences(UUID userId, List<String> openAlexIds) {
        if (openAlexIds == null || openAlexIds.isEmpty()) {
            return List.of();
        }

        if (openAlexIds.size() > MAX_REFERENCES) {
            throw new BusinessException(ErrorCode.SOCIAL_POST_EXCEEDS_REFERENCE_LIMIT);
        }

        List<String> distinctIds = openAlexIds.stream()
                .map(this::normalizeOpenAlexId)
                .distinct()
                .toList();

        boolean hasInvalidFormat = distinctIds.stream()
                .anyMatch(id -> id == null || !id.startsWith("W"));

        if (hasInvalidFormat) {
            throw new BusinessException(ErrorCode.SOCIAL_POST_REFERENCE_INVALID_FORMAT);
        }

        List<UserBookmark> validBookmarks = bookmarkRepository.findByUserIdAndOpenAlexIdIn(
                userId,
                distinctIds
        );

        if (validBookmarks.size() != distinctIds.size()) {
            throw new BusinessException(ErrorCode.SOCIAL_POST_REFERENCE_NOT_IN_BOOKMARK);
        }

        return validBookmarks;
    }

    private Page<SocialPostSummaryResponse> toSummaryPage(Page<SocialPost> page, UUID viewerId) {
        List<UUID> postIds = page.stream().map(SocialPost::getId).toList();
        Set<UUID> likedIds = (viewerId != null && !postIds.isEmpty())
                ? likeRepository.findLikedPostIds(viewerId, postIds)
                : Set.of();

        return page.map(post -> toSummaryResponse(post, likedIds.contains(post.getId())));
    }

    private SocialPostSummaryResponse toSummaryResponse(SocialPost post, boolean liked) {
        User author = post.getAuthor();
        List<ResolvedReferenceSnapshot> references = resolveReferenceSnapshots(
                author.getId(),
                post.getReferences()
        );

        return new SocialPostSummaryResponse(
                post.getId(),
                post.getTitle(),
                buildBodyPreview(post.getBody()),
                extractTopicTags(post.getTopicTag()),
                references.stream().map(this::toSummaryReferenceInfo).toList(),
                post.getLikeCount(),
                liked,
                new SocialPostSummaryResponse.AuthorInfo(author.getId(), buildAuthorName(author)),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private SocialPostDetailResponse toDetailResponse(
            SocialPost post,
            boolean liked,
            boolean likesReset
    ) {
        User author = post.getAuthor();
        List<ResolvedReferenceSnapshot> references = resolveReferenceSnapshots(
                author.getId(),
                post.getReferences()
        );

        return new SocialPostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                extractTopicTags(post.getTopicTag()),
                post.getLikeCount(),
                liked,
                new SocialPostDetailResponse.AuthorInfo(author.getId(), buildAuthorName(author)),
                references.stream().map(this::toDetailReferenceInfo).toList(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likesReset
        );
    }

    private String buildBodyPreview(String body) {
        if (body.length() <= BODY_PREVIEW_LENGTH) {
            return body;
        }

        return body.substring(0, BODY_PREVIEW_LENGTH) + "...";
    }

    private String buildAuthorName(User author) {
        return (author.getFirstName() + " " + author.getLastName()).trim();
    }

    private SocialPostReference buildReferenceFromBookmark(
            SocialPost post,
            UserBookmark bookmark,
            String resolvedWorkTypeSnapshot
    ) {
        return SocialPostReference.builder()
                .post(post)
                .openalexId(bookmark.getOpenAlexId())
                .titleSnapshot(bookmark.getTitleSnapshot())
                .authorsSnapshot(bookmark.getAuthorsSnapshot())
                .authorOpenAlexIdsSnapshot(bookmark.getAuthorOpenAlexIdsSnapshot())
                .workTypeSnapshot(snapshotSupport.firstNonBlank(
                        resolvedWorkTypeSnapshot,
                        bookmark.getWorkTypeSnapshot()
                ))
                .topicSnapshot(bookmark.getTopicSnapshot())
                .topicOpenAlexIdSnapshot(bookmark.getTopicOpenAlexIdSnapshot())
                .yearSnapshot(bookmark.getPublicationYear() != null
                        ? bookmark.getPublicationYear().shortValue()
                        : null)
                .build();
    }

    private List<String> extractTopicTags(String rawTopicTag) {
        if (!StringUtils.hasText(rawTopicTag)) {
            return List.of();
        }

        return Arrays.stream(rawTopicTag.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private List<ResolvedReferenceSnapshot> resolveReferenceSnapshots(
            UUID userId,
            List<SocialPostReference> references
    ) {
        if (references == null || references.isEmpty()) {
            return List.of();
        }

        Map<String, UserBookmark> bookmarksByOpenAlexId = loadBookmarksByOpenAlexId(
                userId,
                references
        );
        Map<String, String> resolvedWorkTypesByOpenAlexId = resolveReferenceWorkTypeSnapshots(
                references,
                bookmarksByOpenAlexId
        );

        return references.stream()
                .map(reference -> resolveReferenceSnapshot(
                        reference,
                        bookmarksByOpenAlexId.get(reference.getOpenalexId()),
                        resolvedWorkTypesByOpenAlexId
                ))
                .toList();
    }

    private Map<String, UserBookmark> loadBookmarksByOpenAlexId(
            UUID userId,
            List<SocialPostReference> references
    ) {
        if (references == null || references.isEmpty()) {
            return Map.of();
        }

        List<String> openAlexIds = references.stream()
                .map(SocialPostReference::getOpenalexId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        if (openAlexIds.isEmpty()) {
            return Map.of();
        }

        return bookmarkRepository.findByUserIdAndOpenAlexIdIn(userId, openAlexIds).stream()
                .collect(Collectors.toMap(
                        UserBookmark::getOpenAlexId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private ResolvedReferenceSnapshot resolveReferenceSnapshot(
            SocialPostReference reference,
            UserBookmark fallbackBookmark,
            Map<String, String> resolvedWorkTypesByOpenAlexId
    ) {
        String rawAuthorIds = snapshotSupport.firstNonBlank(
                reference.getAuthorOpenAlexIdsSnapshot(),
                fallbackBookmark != null ? fallbackBookmark.getAuthorOpenAlexIdsSnapshot() : null
        );

        Integer yearSnapshot = reference.getYearSnapshot() != null
                ? reference.getYearSnapshot().intValue()
                : fallbackBookmark != null
                ? fallbackBookmark.getPublicationYear()
                : null;
        String fallbackWorkTypeSnapshot = fallbackBookmark != null
                ? snapshotSupport.normalizeText(fallbackBookmark.getWorkTypeSnapshot())
                : null;

        if (!StringUtils.hasText(fallbackWorkTypeSnapshot)) {
            fallbackWorkTypeSnapshot = resolvedWorkTypesByOpenAlexId.get(
                    normalizeOpenAlexId(reference.getOpenalexId())
            );
        }

        return new ResolvedReferenceSnapshot(
                reference.getId(),
                reference.getOpenalexId(),
                snapshotSupport.firstNonBlank(
                        reference.getTitleSnapshot(),
                        fallbackBookmark != null ? fallbackBookmark.getTitleSnapshot() : null
                ),
                snapshotSupport.firstNonBlank(
                        reference.getAuthorsSnapshot(),
                        fallbackBookmark != null ? fallbackBookmark.getAuthorsSnapshot() : null
                ),
                snapshotSupport.deserializeEntityIds(rawAuthorIds),
                snapshotSupport.firstNonBlank(
                        reference.getWorkTypeSnapshot(),
                        fallbackWorkTypeSnapshot
                ),
                snapshotSupport.firstNonBlank(
                        reference.getTopicSnapshot(),
                        fallbackBookmark != null ? fallbackBookmark.getTopicSnapshot() : null
                ),
                snapshotSupport.normalizeEntityId(snapshotSupport.firstNonBlank(
                        reference.getTopicOpenAlexIdSnapshot(),
                        fallbackBookmark != null ? fallbackBookmark.getTopicOpenAlexIdSnapshot() : null
                )),
                yearSnapshot
        );
    }

    private SocialPostSummaryResponse.ReferenceInfo toSummaryReferenceInfo(
            ResolvedReferenceSnapshot reference
    ) {
        return new SocialPostSummaryResponse.ReferenceInfo(
                reference.id(),
                reference.openAlexId(),
                reference.titleSnapshot(),
                reference.authorsSnapshot(),
                reference.authorOpenAlexIdsSnapshot(),
                reference.workTypeSnapshot(),
                reference.topicSnapshot(),
                reference.topicOpenAlexIdSnapshot(),
                reference.yearSnapshot()
        );
    }

    private SocialPostDetailResponse.ReferenceInfo toDetailReferenceInfo(
            ResolvedReferenceSnapshot reference
    ) {
        return new SocialPostDetailResponse.ReferenceInfo(
                reference.id(),
                reference.openAlexId(),
                reference.titleSnapshot(),
                reference.authorsSnapshot(),
                reference.authorOpenAlexIdsSnapshot(),
                reference.workTypeSnapshot(),
                reference.topicSnapshot(),
                reference.topicOpenAlexIdSnapshot(),
                reference.yearSnapshot()
        );
    }

    private SocialPost findActivePost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOCIAL_POST_NOT_FOUND));
    }

    private void assertIsAuthor(SocialPost post, UUID userId) {
        if (!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private Map<String, String> resolveBookmarkWorkTypeSnapshots(List<UserBookmark> bookmarks) {
        if (bookmarks == null || bookmarks.isEmpty()) {
            return Map.of();
        }

        Map<String, String> resolvedWorkTypesByOpenAlexId = new LinkedHashMap<>();
        List<String> missingOpenAlexIds = new ArrayList<>();

        for (UserBookmark bookmark : bookmarks) {
            String normalizedOpenAlexId = normalizeOpenAlexId(bookmark.getOpenAlexId());

            if (!StringUtils.hasText(normalizedOpenAlexId)) {
                continue;
            }

            String workTypeSnapshot = snapshotSupport.normalizeText(
                    bookmark.getWorkTypeSnapshot()
            );

            if (StringUtils.hasText(workTypeSnapshot)) {
                resolvedWorkTypesByOpenAlexId.put(normalizedOpenAlexId, workTypeSnapshot);
                continue;
            }

            missingOpenAlexIds.add(normalizedOpenAlexId);
        }

        if (!missingOpenAlexIds.isEmpty()) {
            resolvedWorkTypesByOpenAlexId.putAll(fetchWorkTypeLabels(missingOpenAlexIds));
        }

        return resolvedWorkTypesByOpenAlexId;
    }

    private Map<String, String> resolveReferenceWorkTypeSnapshots(
            List<SocialPostReference> references,
            Map<String, UserBookmark> bookmarksByOpenAlexId
    ) {
        if (references == null || references.isEmpty()) {
            return Map.of();
        }

        Map<String, String> resolvedWorkTypesByOpenAlexId = new LinkedHashMap<>();
        List<String> missingOpenAlexIds = new ArrayList<>();

        for (SocialPostReference reference : references) {
            String normalizedOpenAlexId = normalizeOpenAlexId(reference.getOpenalexId());

            if (!StringUtils.hasText(normalizedOpenAlexId)) {
                continue;
            }

            String referenceWorkTypeSnapshot = snapshotSupport.normalizeText(
                    reference.getWorkTypeSnapshot()
            );

            if (StringUtils.hasText(referenceWorkTypeSnapshot)) {
                resolvedWorkTypesByOpenAlexId.put(
                        normalizedOpenAlexId,
                        referenceWorkTypeSnapshot
                );
                continue;
            }

            UserBookmark fallbackBookmark = bookmarksByOpenAlexId.get(reference.getOpenalexId());
            String bookmarkWorkTypeSnapshot = fallbackBookmark != null
                    ? snapshotSupport.normalizeText(fallbackBookmark.getWorkTypeSnapshot())
                    : null;

            if (StringUtils.hasText(bookmarkWorkTypeSnapshot)) {
                resolvedWorkTypesByOpenAlexId.put(
                        normalizedOpenAlexId,
                        bookmarkWorkTypeSnapshot
                );
                continue;
            }

            missingOpenAlexIds.add(normalizedOpenAlexId);
        }

        if (!missingOpenAlexIds.isEmpty()) {
            resolvedWorkTypesByOpenAlexId.putAll(fetchWorkTypeLabels(missingOpenAlexIds));
        }

        return resolvedWorkTypesByOpenAlexId;
    }

    private Map<String, String> fetchWorkTypeLabels(List<String> openAlexIds) {
        List<String> normalizedOpenAlexIds = openAlexIds.stream()
                .map(this::normalizeOpenAlexId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        if (normalizedOpenAlexIds.isEmpty()) {
            return Map.of();
        }

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("filter", "openalex:" + String.join("|", normalizedOpenAlexIds));
        queryParams.put("per_page", String.valueOf(normalizedOpenAlexIds.size()));
        queryParams.put("select", OPENALEX_WORK_TYPE_SELECT_FIELDS);

        try {
            Map<String, Object> response = openAlexClient.get("/works", queryParams);
            List<Map<String, Object>> results = openAlexMapReader.getMapList(response, "results");
            Map<String, String> resolvedWorkTypes = new LinkedHashMap<>();

            for (Map<String, Object> result : results) {
                String normalizedOpenAlexId = normalizeOpenAlexId(
                        searchQuerySupport.normalizeEntityValue(
                                openAlexMapReader.getString(result, "id")
                        )
                );
                String normalizedWorkType = formatWorkTypeLabel(
                        openAlexMapReader.getString(result, "type")
                );

                if (StringUtils.hasText(normalizedOpenAlexId)
                        && StringUtils.hasText(normalizedWorkType)) {
                    resolvedWorkTypes.put(normalizedOpenAlexId, normalizedWorkType);
                }
            }

            return resolvedWorkTypes;
        } catch (RuntimeException exception) {
            return Map.of();
        }
    }

    private String formatWorkTypeLabel(String rawValue) {
        String normalizedValue = snapshotSupport.normalizeText(rawValue);

        if (!StringUtils.hasText(normalizedValue)) {
            return null;
        }

        String[] segments = normalizedValue
                .trim()
                .toLowerCase()
                .replace('_', '-')
                .split("-");
        StringBuilder formattedValue = new StringBuilder();

        for (String segment : segments) {
            if (!StringUtils.hasText(segment)) {
                continue;
            }

            if (formattedValue.length() > 0) {
                formattedValue.append(' ');
            }

            formattedValue.append(Character.toUpperCase(segment.charAt(0)));
            formattedValue.append(segment.substring(1));
        }

        return formattedValue.length() == 0 ? null : formattedValue.toString();
    }

    private String normalizeOpenAlexId(String rawValue) {
        return snapshotSupport.normalizeEntityId(rawValue);
    }

    private record ResolvedReferenceSnapshot(
            UUID id,
            String openAlexId,
            String titleSnapshot,
            String authorsSnapshot,
            List<String> authorOpenAlexIdsSnapshot,
            String workTypeSnapshot,
            String topicSnapshot,
            String topicOpenAlexIdSnapshot,
            Integer yearSnapshot
    ) {
    }
}
