package com.brotherhood.scipubtts.bookmark.service.impl;

import com.brotherhood.scipubtts.bookmark.dto.request.CreateBookmarkCollectionRequest;
import com.brotherhood.scipubtts.bookmark.dto.request.CreateBookmarkRequest;
import com.brotherhood.scipubtts.bookmark.dto.request.UpdateBookmarkCollectionItemsRequest;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkCollectionMembershipRow;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkCollectionResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkCollectionSummaryResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkPageResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatsResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatusResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.FilterOptionsResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.TrendingPaperResponse;
import com.brotherhood.scipubtts.bookmark.entity.BookmarkCollection;
import com.brotherhood.scipubtts.bookmark.entity.CollectionBookmark;
import com.brotherhood.scipubtts.bookmark.entity.UserBookmark;
import com.brotherhood.scipubtts.bookmark.repository.BookmarkCollectionRepository;
import com.brotherhood.scipubtts.bookmark.repository.CollectionBookmarkRepository;
import com.brotherhood.scipubtts.bookmark.repository.UserBookmarkRepository;
import com.brotherhood.scipubtts.bookmark.service.BookmarkService;
import com.brotherhood.scipubtts.bookmark.support.BookmarkSnapshotSupport;
import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.search.service.OpenAlexMapReader;
import com.brotherhood.scipubtts.search.service.SearchQuerySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 12;
    private static final int MAX_SIZE = 50;
    private static final String OPENALEX_WORK_TYPE_SELECT_FIELDS = "id,type";

    private final UserBookmarkRepository userBookmarkRepository;
    private final BookmarkCollectionRepository bookmarkCollectionRepository;
    private final CollectionBookmarkRepository collectionBookmarkRepository;
    private final BookmarkSnapshotSupport snapshotSupport;
    private final OpenAlexClient openAlexClient;
    private final OpenAlexMapReader openAlexMapReader;
    private final SearchQuerySupport searchQuerySupport;

    @Override
    @Transactional
    public BookmarkResponse addBookmark(UUID userId, CreateBookmarkRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.REQUEST_BODY_REQUIRED);
        }

        String openAlexId = normalizeOpenAlexId(request.openAlexId());

        return userBookmarkRepository.findByUserIdAndOpenAlexId(userId, openAlexId)
                .map(existing -> loadSingleBookmarkResponse(userId, existing))
                .orElseGet(() -> createNewBookmark(userId, openAlexId, request));
    }

    @Override
    @Transactional
    public BookmarkPageResponse getMyBookmarks(
            UUID userId,
            int page,
            int size,
            UUID collectionId,
            String title,
            String keyword,
            String topic,
            String source,
            String author,
            Integer year,
            String sort
    ) {
        int safePage = Math.max(page, DEFAULT_PAGE);
        int safeSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(safePage, safeSize, buildSort(sort));

        Page<UserBookmark> bookmarkPage = userBookmarkRepository.searchMyBookmarks(
                userId,
                collectionId,
                normalizeText(title),
                normalizeText(keyword),
                normalizeText(topic),
                normalizeText(source),
                normalizeText(author),
                year,
                pageable
        );

        List<UserBookmark> bookmarks = bookmarkPage.getContent();
        Map<String, String> resolvedWorkTypes = backfillMissingWorkTypeSnapshots(bookmarks);
        Map<UUID, List<BookmarkCollectionSummaryResponse>> collectionsByBookmarkId =
                loadCollectionsByBookmarkId(userId, bookmarks);

        List<BookmarkResponse> items = bookmarks.stream()
                .map(bookmark -> toResponse(bookmark, collectionsByBookmarkId, resolvedWorkTypes))
                .toList();

        return new BookmarkPageResponse(
                items,
                bookmarkPage.getNumber(),
                bookmarkPage.getSize(),
                bookmarkPage.getTotalElements(),
                bookmarkPage.getTotalPages(),
                bookmarkPage.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BookmarkStatusResponse getStatus(UUID userId, String openAlexId) {
        String normalizedOpenAlexId = normalizeOpenAlexId(openAlexId);

        if (!StringUtils.hasText(normalizedOpenAlexId)) {
            return new BookmarkStatusResponse(false, null, openAlexId, List.of());
        }

        return userBookmarkRepository.findByUserIdAndOpenAlexId(userId, normalizedOpenAlexId)
                .map(bookmark -> {
                    List<BookmarkCollectionSummaryResponse> collections =
                            loadCollectionsByBookmarkId(userId, List.of(bookmark))
                                    .getOrDefault(bookmark.getId(), List.of());

                    return new BookmarkStatusResponse(
                            true,
                            bookmark.getId(),
                            bookmark.getOpenAlexId(),
                            collections
                    );
                })
                .orElseGet(() -> new BookmarkStatusResponse(
                        false,
                        null,
                        normalizedOpenAlexId,
                        List.of()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public BookmarkStatsResponse getStats(UUID userId) {
        long totalPapers = userBookmarkRepository.countByUserId(userId);
        long totalTopics = userBookmarkRepository.countDistinctTopicsByUserId(userId);
        long totalAuthors = userBookmarkRepository.countDistinctAuthorsByUserId(userId);

        return new BookmarkStatsResponse(
                safeLongToInt(totalPapers),
                safeLongToInt(totalTopics),
                safeLongToInt(totalAuthors)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FilterOptionsResponse getFilterOptions(UUID userId) {
        return new FilterOptionsResponse(
                userBookmarkRepository.findDistinctTopicsByUserId(userId),
                userBookmarkRepository.findDistinctYearsByUserId(userId),
                userBookmarkRepository.findDistinctAuthorsByUserId(userId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkCollectionResponse> getCollections(UUID userId) {
        return bookmarkCollectionRepository.findCollectionResponsesByUserId(userId);
    }

    @Override
    @Transactional
    public BookmarkCollectionResponse createCollection(
            UUID userId,
            CreateBookmarkCollectionRequest request
    ) {
        if (request == null) {
            throw new BusinessException(ErrorCode.REQUEST_BODY_REQUIRED);
        }

        String name = normalizeText(request.name());

        if (!StringUtils.hasText(name)) {
            throw new BusinessException(ErrorCode.BOOKMARK_COLLECTION_NAME_REQUIRED);
        }

        if (bookmarkCollectionRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new BusinessException(ErrorCode.BOOKMARK_COLLECTION_ALREADY_EXISTS);
        }

        try {
            BookmarkCollection collection = bookmarkCollectionRepository.save(
                    BookmarkCollection.builder()
                            .userId(userId)
                            .name(name)
                            .createdAt(OffsetDateTime.now())
                            .build()
            );

            return new BookmarkCollectionResponse(
                    collection.getId(),
                    collection.getName(),
                    0,
                    collection.getCreatedAt()
            );
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.BOOKMARK_COLLECTION_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional
    public void deleteCollection(UUID userId, UUID collectionId) {
        requireCollection(userId, collectionId);
        int deletedRows = bookmarkCollectionRepository.deleteOwnedCollectionById(collectionId, userId);

        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.BOOKMARK_COLLECTION_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void addBookmarksToCollection(
            UUID userId,
            UUID collectionId,
            UpdateBookmarkCollectionItemsRequest request
    ) {
        requireCollection(userId, collectionId);

        Set<UUID> bookmarkIds = extractBookmarkIds(request);
        validateBookmarkOwnership(userId, bookmarkIds);

        List<CollectionBookmark> newRelations = buildMissingCollectionRelations(
                collectionId,
                bookmarkIds
        );

        if (!newRelations.isEmpty()) {
            collectionBookmarkRepository.saveAll(newRelations);
        }
    }

    @Override
    @Transactional
    public void removeBookmarkFromCollection(UUID userId, UUID collectionId, UUID bookmarkId) {
        requireCollection(userId, collectionId);
        collectionBookmarkRepository.deleteByCollectionIdAndBookmarkId(collectionId, bookmarkId);
    }

    @Override
    @Transactional
    public void deleteBookmark(UUID userId, UUID bookmarkId) {
        int deletedRows = userBookmarkRepository.deleteByIdAndUserId(bookmarkId, userId);

        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void deleteByOpenAlexId(UUID userId, String openAlexId) {
        userBookmarkRepository.deleteByUserIdAndOpenAlexId(
                userId,
                normalizeOpenAlexId(openAlexId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrendingPaperResponse> getTop6TrendingPapers() {
        OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusDays(7);
        return userBookmarkRepository.findTrendingPaperThisWeek(oneWeekAgo, PageRequest.of(0, 6));
    }

    private BookmarkResponse createNewBookmark(
            UUID userId,
            String openAlexId,
            CreateBookmarkRequest request
    ) {
        try {
            String workTypeSnapshot = resolveRequestedWorkTypeSnapshot(
                    openAlexId,
                    request.workTypeSnapshot()
            );

            UserBookmark saved = userBookmarkRepository.save(
                    UserBookmark.builder()
                            .userId(userId)
                            .openAlexId(openAlexId)
                            .entityType("WORK")
                            .titleSnapshot(normalizeText(request.titleSnapshot()))
                            .authorsSnapshot(normalizeText(request.authorsSnapshot()))
                            .authorOpenAlexIdsSnapshot(
                                    snapshotSupport.serializeEntityIds(
                                            request.authorOpenAlexIdsSnapshot()
                                    )
                            )
                            .workTypeSnapshot(workTypeSnapshot)
                            .sourceSnapshot(normalizeText(request.sourceSnapshot()))
                            .topicSnapshot(normalizeText(request.topicSnapshot()))
                            .topicOpenAlexIdSnapshot(
                                    snapshotSupport.normalizeEntityId(
                                            request.topicOpenAlexIdSnapshot()
                                    )
                            )
                            .publicationYear(request.publicationYear())
                            .citationSnapshot(request.citationSnapshot())
                            .createdAt(OffsetDateTime.now())
                            .build()
            );

            return toResponse(saved, List.of());
        } catch (DataIntegrityViolationException ex) {
            UserBookmark existing = userBookmarkRepository
                    .findByUserIdAndOpenAlexId(userId, openAlexId)
                    .orElseThrow(() -> ex);

            return loadSingleBookmarkResponse(userId, existing);
        }
    }

    private BookmarkResponse toResponse(
            UserBookmark bookmark,
            List<BookmarkCollectionSummaryResponse> collections
    ) {
        return toResponse(bookmark, collections, Map.of());
    }

    private BookmarkResponse toResponse(
            UserBookmark bookmark,
            List<BookmarkCollectionSummaryResponse> collections,
            Map<String, String> resolvedWorkTypes
    ) {
        return new BookmarkResponse(
                bookmark.getId(),
                bookmark.getOpenAlexId(),
                bookmark.getTitleSnapshot(),
                bookmark.getAuthorsSnapshot(),
                resolveWorkTypeLabel(bookmark, resolvedWorkTypes),
                bookmark.getSourceSnapshot(),
                bookmark.getTopicSnapshot(),
                bookmark.getPublicationYear(),
                bookmark.getCitationSnapshot(),
                collections,
                bookmark.getCreatedAt()
        );
    }

    private BookmarkResponse toResponse(
            UserBookmark bookmark,
            Map<UUID, List<BookmarkCollectionSummaryResponse>> collectionsByBookmarkId
    ) {
        return toResponse(bookmark, collectionsByBookmarkId, Map.of());
    }

    private BookmarkResponse toResponse(
            UserBookmark bookmark,
            Map<UUID, List<BookmarkCollectionSummaryResponse>> collectionsByBookmarkId,
            Map<String, String> resolvedWorkTypes
    ) {
        return toResponse(
                bookmark,
                collectionsByBookmarkId.getOrDefault(bookmark.getId(), List.of()),
                resolvedWorkTypes
        );
    }

    private BookmarkResponse loadSingleBookmarkResponse(UUID userId, UserBookmark bookmark) {
        return toResponse(
                bookmark,
                loadCollectionsByBookmarkId(userId, List.of(bookmark)),
                backfillMissingWorkTypeSnapshots(List.of(bookmark))
        );
    }

    private Map<UUID, List<BookmarkCollectionSummaryResponse>> loadCollectionsByBookmarkId(
            UUID userId,
            List<UserBookmark> bookmarks
    ) {
        if (bookmarks.isEmpty()) {
            return Map.of();
        }

        List<UUID> bookmarkIds = bookmarks.stream()
                .map(UserBookmark::getId)
                .toList();

        List<BookmarkCollectionMembershipRow> rows =
                collectionBookmarkRepository.findMembershipRowsByUserIdAndBookmarkIds(
                        userId,
                        bookmarkIds
                );

        Map<UUID, List<BookmarkCollectionSummaryResponse>> collectionsByBookmarkId =
                new LinkedHashMap<>();

        for (BookmarkCollectionMembershipRow row : rows) {
            collectionsByBookmarkId
                    .computeIfAbsent(row.bookmarkId(), ignored -> new ArrayList<>())
                    .add(new BookmarkCollectionSummaryResponse(
                            row.collectionId(),
                            row.collectionName()
                    ));
        }

        return collectionsByBookmarkId;
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_SIZE);
    }

    private Sort buildSort(String sort) {
        String normalizedSort = normalizeText(sort);

        if (normalizedSort == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return switch (normalizedSort.toUpperCase()) {
            case "OLDEST" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "YEAR_DESC" -> Sort.by(Sort.Direction.DESC, "publicationYear")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "YEAR_ASC" -> Sort.by(Sort.Direction.ASC, "publicationYear")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "CITATION_DESC" -> Sort.by(Sort.Direction.DESC, "citationSnapshot")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "CITATION_ASC" -> Sort.by(Sort.Direction.ASC, "citationSnapshot")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "TITLE_ASC" -> Sort.by(Sort.Direction.ASC, "titleSnapshot");
            case "TITLE_DESC" -> Sort.by(Sort.Direction.DESC, "titleSnapshot");
            case "RECENT" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private int safeLongToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) value;
    }

    private BookmarkCollection requireCollection(UUID userId, UUID collectionId) {
        return bookmarkCollectionRepository.findByIdAndUserId(collectionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKMARK_COLLECTION_NOT_FOUND));
    }

    private Set<UUID> extractBookmarkIds(UpdateBookmarkCollectionItemsRequest request) {
        if (request == null || request.bookmarkIds() == null || request.bookmarkIds().isEmpty()) {
            throw new BusinessException(ErrorCode.BOOKMARK_REQUIRED);
        }

        Set<UUID> bookmarkIds = new LinkedHashSet<>();

        for (UUID bookmarkId : request.bookmarkIds()) {
            if (bookmarkId != null) {
                bookmarkIds.add(bookmarkId);
            }
        }

        if (bookmarkIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BOOKMARK_REQUIRED);
        }

        return bookmarkIds;
    }

    private void validateBookmarkOwnership(UUID userId, Set<UUID> bookmarkIds) {
        long ownedCount = userBookmarkRepository.countByUserIdAndIdIn(userId, bookmarkIds);

        if (ownedCount != bookmarkIds.size()) {
            throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
        }
    }

    private List<CollectionBookmark> buildMissingCollectionRelations(
            UUID collectionId,
            Set<UUID> bookmarkIds
    ) {
        Set<UUID> existingBookmarkIds =
                collectionBookmarkRepository.findBookmarkIdsByCollectionIdAndBookmarkIdIn(
                        collectionId,
                        bookmarkIds
                );

        List<CollectionBookmark> newRelations = new ArrayList<>();

        for (UUID bookmarkId : bookmarkIds) {
            if (existingBookmarkIds.contains(bookmarkId)) {
                continue;
            }

            newRelations.add(CollectionBookmark.builder()
                    .collectionId(collectionId)
                    .bookmarkId(bookmarkId)
                    .createdAt(OffsetDateTime.now())
                    .build());
        }

        return newRelations;
    }

    private String normalizeText(String value) {
        return snapshotSupport.normalizeText(value);
    }

    private String normalizeOpenAlexId(String value) {
        return snapshotSupport.normalizeEntityId(value);
    }

    private String resolveRequestedWorkTypeSnapshot(
            String openAlexId,
            String requestedWorkTypeSnapshot
    ) {
        String normalizedRequestedWorkType = formatWorkTypeLabel(requestedWorkTypeSnapshot);

        if (StringUtils.hasText(normalizedRequestedWorkType)) {
            return normalizedRequestedWorkType;
        }

        return fetchWorkTypeLabels(List.of(openAlexId)).get(openAlexId);
    }

    private Map<String, String> backfillMissingWorkTypeSnapshots(List<UserBookmark> bookmarks) {
        if (bookmarks.isEmpty()) {
            return Map.of();
        }

        List<UserBookmark> bookmarksMissingWorkType = bookmarks.stream()
                .filter(this::shouldFetchWorkTypeSnapshot)
                .toList();

        if (bookmarksMissingWorkType.isEmpty()) {
            return Map.of();
        }

        Map<String, String> resolvedWorkTypes = fetchWorkTypeLabels(
                bookmarksMissingWorkType.stream()
                        .map(UserBookmark::getOpenAlexId)
                        .toList()
        );

        if (resolvedWorkTypes.isEmpty()) {
            return Map.of();
        }

        List<UserBookmark> bookmarksToUpdate = new ArrayList<>();

        for (UserBookmark bookmark : bookmarksMissingWorkType) {
            String normalizedOpenAlexId = normalizeOpenAlexId(bookmark.getOpenAlexId());
            String resolvedWorkType = resolvedWorkTypes.get(normalizedOpenAlexId);

            if (!StringUtils.hasText(resolvedWorkType)) {
                continue;
            }

            bookmark.setWorkTypeSnapshot(resolvedWorkType);
            bookmarksToUpdate.add(bookmark);
        }

        if (!bookmarksToUpdate.isEmpty()) {
            userBookmarkRepository.saveAll(bookmarksToUpdate);
        }

        return resolvedWorkTypes;
    }

    private boolean shouldFetchWorkTypeSnapshot(UserBookmark bookmark) {
        if (!StringUtils.hasText(normalizeOpenAlexId(bookmark.getOpenAlexId()))) {
            return false;
        }

        if (!"WORK".equalsIgnoreCase(normalizeText(bookmark.getEntityType()))) {
            return false;
        }

        return !StringUtils.hasText(normalizeText(bookmark.getWorkTypeSnapshot()));
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

    private String formatWorkTypeLabel(String value) {
        String normalizedValue = normalizeText(value);

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

    private String resolveWorkTypeLabel(
            UserBookmark bookmark,
            Map<String, String> resolvedWorkTypes
    ) {
        String workTypeSnapshot = normalizeText(bookmark.getWorkTypeSnapshot());

        if (StringUtils.hasText(workTypeSnapshot)) {
            return workTypeSnapshot;
        }

        String resolvedWorkType = resolvedWorkTypes.get(
                normalizeOpenAlexId(bookmark.getOpenAlexId())
        );

        if (StringUtils.hasText(resolvedWorkType)) {
            return resolvedWorkType;
        }

        String entityType = normalizeText(bookmark.getEntityType());

        if (!StringUtils.hasText(entityType)) {
            return "Work";
        }

        String normalizedEntityType = entityType.trim().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(normalizedEntityType.charAt(0)) + normalizedEntityType.substring(1);
    }
}
