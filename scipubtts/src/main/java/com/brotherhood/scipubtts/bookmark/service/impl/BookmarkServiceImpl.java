package com.brotherhood.scipubtts.bookmark.service.impl;

import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkPageResponse;
import com.brotherhood.scipubtts.bookmark.dto.request.CreateBookmarkRequest;
import com.brotherhood.scipubtts.bookmark.dto.request.UpdateBookmarkNoteRequest;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatsResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatusResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.FilterOptionsResponse;
import com.brotherhood.scipubtts.bookmark.entity.UserBookmark;
import com.brotherhood.scipubtts.bookmark.repository.UserBookmarkRepository;
import com.brotherhood.scipubtts.bookmark.service.BookmarkService;
import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 12;
    private static final int MAX_SIZE = 50;

    private final UserBookmarkRepository userBookmarkRepository;

    public BookmarkResponse toResponse(UserBookmark bookmark) {
        if (bookmark == null) {
            return null;
        }

        return new BookmarkResponse(
                bookmark.getId(),
                bookmark.getOpenAlexId(),
                bookmark.getTitleSnapshot(),
                bookmark.getAuthorsSnapshot(),
                bookmark.getSourceSnapshot(),
                bookmark.getTopicSnapshot(),
                bookmark.getPublicationYear(),
                bookmark.getCitationSnapshot(),
                bookmark.getNote(),
                bookmark.getCreatedAt()
        );
    }


    // =========================================================
    // 1. ADD BOOKMARK
    // =========================================================
    @Override
    @Transactional
    public BookmarkResponse addBookmark(UUID userId, CreateBookmarkRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.REQUEST_BODY_REQUIRED);
        }

        String openAlexId = normalize(request.openAlexId());

        // Nếu đã bookmark rồi thì trả về bookmark hiện có.
        // Không nên throw lỗi vì FE có thể bấm nhiều lần hoặc reload state chưa kịp.
        return userBookmarkRepository.findByUserIdAndOpenAlexId(userId, openAlexId)
                .map(this::toResponse)
                .orElseGet(() -> createNewBookmark(userId, openAlexId, request));
    }

    private BookmarkResponse createNewBookmark(
            UUID userId,
            String openAlexId,
            CreateBookmarkRequest request
    ) {
        try {
            UserBookmark bookmark = UserBookmark.builder()
                    .userId(userId)
                    .openAlexId(openAlexId)
                    .titleSnapshot(normalizeNullable(request.titleSnapshot()))
                    .authorsSnapshot(normalizeNullable(request.authorsSnapshot()))
                    .sourceSnapshot(normalizeNullable(request.sourceSnapshot()))
                    .topicSnapshot(normalizeNullable(request.topicSnapshot()))
                    .publicationYear(request.publicationYear())
                    .citationSnapshot(request.citationSnapshot())
                    .note(normalizeNullable(request.note()))
                    .isPublic(false)
                    .createdAt(OffsetDateTime.now())
                    .build();

            UserBookmark saved = userBookmarkRepository.save(bookmark);
            return this.toResponse(saved);

        } catch (DataIntegrityViolationException ex) {
            // Trường hợp race condition:
            // 2 request cùng lúc add cùng userId + openAlexId.
            UserBookmark existing = userBookmarkRepository
                    .findByUserIdAndOpenAlexId(userId, openAlexId)
                    .orElseThrow(() -> ex);

            return this.toResponse(existing);
        }
    }

    // =========================================================
    // 2. GET BOOKMARK LIST - LAZY PAGING + FILTER + SORT
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public BookmarkPageResponse getMyBookmarks(
            UUID userId,
            int page,
            int size,
            String keyword,
            String topic,
            String source,
            String author,
            Integer year,
            String sort
    ) {

        int safePage = Math.max(page, DEFAULT_PAGE);
        int safeSize = normalizeSize(size);

        Sort springSort = buildSort(sort);
        Pageable pageable = PageRequest.of(safePage, safeSize, springSort);

        Page<UserBookmark> bookmarkPage = userBookmarkRepository.searchMyBookmarks(
                userId,
                normalizeNullable(keyword),
                normalizeNullable(topic),
                normalizeNullable(source),
                normalizeNullable(author),
                year,
                pageable
        );

        List<BookmarkResponse> items = bookmarkPage
                .getContent()
                .stream()
                .map(this::toResponse)
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

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }

        return Math.min(size, MAX_SIZE);
    }

    private Sort buildSort(String sort) {
        String normalizedSort = normalize(sort);

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

    // =========================================================
    // 3. CHECK BOOKMARK STATUS
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public BookmarkStatusResponse getStatus(UUID userId, String openAlexId) {

        String normalizedOpenAlexId = normalize(openAlexId);

        if (!StringUtils.hasText(normalizedOpenAlexId)) {
            return new BookmarkStatusResponse(false, null, openAlexId);
        }

        return userBookmarkRepository.findByUserIdAndOpenAlexId(userId, normalizedOpenAlexId)
                .map(bookmark -> new BookmarkStatusResponse(
                        true,
                        bookmark.getId(),
                        bookmark.getOpenAlexId()
                ))
                .orElseGet(() -> new BookmarkStatusResponse(
                        false,
                        null,
                        normalizedOpenAlexId
                ));
    }

    // =========================================================
    // 4. GET STATS
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public BookmarkStatsResponse getStats(UUID userId) {
        long totalPapers = userBookmarkRepository.countByUserId(userId);
        long totalTopics = userBookmarkRepository.countDistinctTopicsByUserId(userId);
        long totalSources = userBookmarkRepository.countDistinctSourcesByUserId(userId);
        long totalAuthors = userBookmarkRepository.countDistinctAuthorsByUserId(userId);

        return new BookmarkStatsResponse(
                safeLongToInt(totalPapers),
                safeLongToInt(totalTopics),
                safeLongToInt(totalSources),
                safeLongToInt(totalAuthors)
        );
    }

    private int safeLongToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) value;
    }

    // =========================================================
    // 5. GET FILTER OPTIONS
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public FilterOptionsResponse getFilterOptions(UUID userId) {

        List<String> topics = userBookmarkRepository.findDistinctTopicsByUserId(userId);
        List<Integer> years = userBookmarkRepository.findDistinctYearsByUserId(userId);
        List<String> sources = userBookmarkRepository.findDistinctSourcesByUserId(userId);
        List<String> authors = userBookmarkRepository.findDistinctAuthorsByUserId(userId);

        return new FilterOptionsResponse(
                topics,
                years,
                sources,
                authors
        );
    }

    // =========================================================
    // 6. ADD OR UPDATE NOTE
    // =========================================================
    @Override
    @Transactional
    public BookmarkResponse updateNote(
            UUID userId,
            UUID bookmarkId,
            UpdateBookmarkNoteRequest request
    ) {

        UserBookmark bookmark = userBookmarkRepository.findByIdAndUserId(bookmarkId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND));

        String note = request == null ? null : normalizeNullable(request.note());

        bookmark.setNote(note);

        UserBookmark saved = userBookmarkRepository.save(bookmark);
        return this.toResponse(saved);
    }

    // =========================================================
    // 7. DELETE BOOKMARK BY ID
    // =========================================================
    @Override
    @Transactional
    public void deleteBookmark(UUID userId, UUID bookmarkId) {
        int deletedRows = userBookmarkRepository.deleteByIdAndUserId(bookmarkId, userId);

        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
        }
    }

    // =========================================================
    // 8. DELETE BOOKMARK BY OPENALEX ID
    // =========================================================
    @Override
    @Transactional
    public void deleteByOpenAlexId(UUID userId, String openAlexId) {

        String normalizedOpenAlexId = normalize(openAlexId);

        // Idempotent:
        // Nếu chưa từng bookmark hoặc đã bị xóa trước đó, vẫn coi là thành công.
        userBookmarkRepository.deleteByUserIdAndOpenAlexId(userId, normalizedOpenAlexId);
    }

    // =========================================================
    // COMMON VALIDATION / NORMALIZATION
    // =========================================================
    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}
