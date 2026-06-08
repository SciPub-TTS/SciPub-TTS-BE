package com.brotherhood.scipubtts.bookmark.service;

import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkPageResponse;
import com.brotherhood.scipubtts.bookmark.dto.request.CreateBookmarkRequest;
import com.brotherhood.scipubtts.bookmark.dto.request.UpdateBookmarkNoteRequest;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatsResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatusResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.FilterOptionsResponse;

import java.util.UUID;

public interface BookmarkService {

    BookmarkResponse addBookmark(UUID userId, CreateBookmarkRequest request);

    BookmarkPageResponse getMyBookmarks(
            UUID userId,
            int page,
            int size,
            String keyword,
            String topic,
            String source,
            Integer year,
            String sort
    );

    BookmarkStatusResponse getStatus(UUID userId, String openAlexId);

    BookmarkStatsResponse getStats(UUID userId);

    FilterOptionsResponse getFilterOptions(UUID userId);

    BookmarkResponse updateNote(UUID userId, UUID bookmarkId, UpdateBookmarkNoteRequest request);

    void deleteBookmark(UUID userId, UUID bookmarkId);

    void deleteByOpenAlexId(UUID userId, String openAlexId);
}
