package com.brotherhood.scipubtts.bookmark.service.impl;

import com.brotherhood.scipubtts.bookmark.dto.request.CreateBookmarkCollectionRequest;
import com.brotherhood.scipubtts.bookmark.dto.request.CreateBookmarkRequest;
import com.brotherhood.scipubtts.bookmark.dto.request.UpdateBookmarkCollectionItemsRequest;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkCollectionResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkPageResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatusResponse;
import com.brotherhood.scipubtts.bookmark.entity.BookmarkCollection;
import com.brotherhood.scipubtts.bookmark.entity.CollectionBookmark;
import com.brotherhood.scipubtts.bookmark.entity.UserBookmark;
import com.brotherhood.scipubtts.bookmark.repository.BookmarkCollectionRepository;
import com.brotherhood.scipubtts.bookmark.repository.BookmarkCollectionMembershipRow;
import com.brotherhood.scipubtts.bookmark.repository.CollectionBookmarkRepository;
import com.brotherhood.scipubtts.bookmark.repository.UserBookmarkRepository;
import com.brotherhood.scipubtts.bookmark.service.BookmarkService;
import com.brotherhood.scipubtts.bookmark.support.BookmarkSnapshotSupport;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
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
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserBookmarkRepository userBookmarkRepository;
    private final BookmarkCollectionRepository bookmarkCollectionRepository;
    private final CollectionBookmarkRepository collectionBookmarkRepository;
    private final BookmarkSnapshotSupport snapshotSupport;

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
            String keyword
    ) {
        int safePage = Math.max(page, DEFAULT_PAGE);
        int safeSize = normalizeSize(size);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.Direction.DESC, "createdAt");

        Page<UserBookmark> bookmarkPage = userBookmarkRepository.searchMyBookmarks(
                userId,
                collectionId,
                normalizeText(keyword),
                pageable
        );

        List<UserBookmark> bookmarks = bookmarkPage.getContent();
        Map<UUID, List<BookmarkCollectionResponse>> collectionsByBookmarkId =
                loadCollectionsByBookmarkId(userId, bookmarks);

        List<BookmarkResponse> items = bookmarks.stream()
            .map(bookmark -> toResponse(bookmark, collectionsByBookmarkId))
                .toList();

        return new BookmarkPageResponse(
                items,
                bookmarkPage.getNumber(),
                bookmarkPage.getTotalElements(),
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
                    List<BookmarkCollectionResponse> collections =
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

            return new BookmarkCollectionResponse(collection.getId(), collection.getName(), 0);
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

    private BookmarkResponse createNewBookmark(
            UUID userId,
            String openAlexId,
            CreateBookmarkRequest request
    ) {
        try {
            String workTypeSnapshot = formatWorkTypeLabel(request.workTypeSnapshot());

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
            List<BookmarkCollectionResponse> collections
    ) {
        return new BookmarkResponse(
                bookmark.getId(),
                bookmark.getOpenAlexId(),
                bookmark.getTitleSnapshot(),
                bookmark.getAuthorsSnapshot(),
                resolveWorkTypeLabel(bookmark),
                bookmark.getTopicSnapshot(),
                bookmark.getPublicationYear(),
                bookmark.getCitationSnapshot(),
                collections,
                formatDisplayDateTime(bookmark.getCreatedAt())
        );
    }

    private BookmarkResponse toResponse(
            UserBookmark bookmark,
            Map<UUID, List<BookmarkCollectionResponse>> collectionsByBookmarkId
    ) {
        return toResponse(bookmark, collectionsByBookmarkId.getOrDefault(bookmark.getId(), List.of()));
    }

    private BookmarkResponse loadSingleBookmarkResponse(UUID userId, UserBookmark bookmark) {
        return toResponse(
                bookmark,
                loadCollectionsByBookmarkId(userId, List.of(bookmark))
        );
    }

    private Map<UUID, List<BookmarkCollectionResponse>> loadCollectionsByBookmarkId(
            UUID userId,
            List<UserBookmark> bookmarks
    ) {
        if (bookmarks.isEmpty()) {
            return Map.of();
        }

        List<UUID> bookmarkIds = bookmarks.stream()
            .map(bookmark -> bookmark.getId())
                .toList();

        List<BookmarkCollectionMembershipRow> rows =
                collectionBookmarkRepository.findMembershipRowsByUserIdAndBookmarkIds(
                        userId,
                        bookmarkIds
                );

        Map<UUID, List<BookmarkCollectionResponse>> collectionsByBookmarkId =
                new LinkedHashMap<>();

        for (BookmarkCollectionMembershipRow row : rows) {
            collectionsByBookmarkId
                    .computeIfAbsent(row.bookmarkId(), ignored -> new ArrayList<>())
                    .add(new BookmarkCollectionResponse(
                            row.collectionId(),
                            row.collectionName(),
                            row.workCount()
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

    private String formatDisplayDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.format(DISPLAY_DATE_TIME_FORMATTER);
    }

    private void requireCollection(UUID userId, UUID collectionId) {
        bookmarkCollectionRepository.findByIdAndUserId(collectionId, userId)
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

            if (!formattedValue.isEmpty()) {
                formattedValue.append(' ');
            }

            formattedValue.append(Character.toUpperCase(segment.charAt(0)));
            formattedValue.append(segment.substring(1));
        }

        return formattedValue.isEmpty() ? null : formattedValue.toString();
    }

    private String resolveWorkTypeLabel(
            UserBookmark bookmark
    ) {
        String workTypeSnapshot = normalizeText(bookmark.getWorkTypeSnapshot());

        if (StringUtils.hasText(workTypeSnapshot)) {
            return workTypeSnapshot;
        }

        String entityType = normalizeText(bookmark.getEntityType());

        if (!StringUtils.hasText(entityType)) {
            return "Work";
        }

        String normalizedEntityType = entityType.trim().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(normalizedEntityType.charAt(0)) + normalizedEntityType.substring(1);
    }
}
