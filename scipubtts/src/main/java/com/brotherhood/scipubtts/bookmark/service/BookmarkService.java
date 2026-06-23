package com.brotherhood.scipubtts.bookmark.service;

import com.brotherhood.scipubtts.bookmark.dto.response.*;
import com.brotherhood.scipubtts.bookmark.dto.request.CreateBookmarkRequest;
import com.brotherhood.scipubtts.bookmark.dto.request.UpdateBookmarkNoteRequest;

import java.util.List;
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
            String author,
            Integer year,
            String sort
    );

    BookmarkStatusResponse getStatus(UUID userId, String openAlexId);

    BookmarkStatsResponse getStats(UUID userId);

    FilterOptionsResponse getFilterOptions(UUID userId);

    BookmarkResponse updateNote(UUID userId, UUID bookmarkId, UpdateBookmarkNoteRequest request);

    void deleteBookmark(UUID userId, UUID bookmarkId);

    void deleteByOpenAlexId(UUID userId, String openAlexId);

    public List<TrendingPaperResponse> getTop6TrendingPapers();
}
