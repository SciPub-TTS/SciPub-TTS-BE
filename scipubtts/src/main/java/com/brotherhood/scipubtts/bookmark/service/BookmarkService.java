package com.brotherhood.scipubtts.bookmark.service;

import com.brotherhood.scipubtts.bookmark.dto.request.CreateBookmarkCollectionRequest;
import com.brotherhood.scipubtts.bookmark.dto.request.CreateBookmarkRequest;
import com.brotherhood.scipubtts.bookmark.dto.request.UpdateBookmarkCollectionItemsRequest;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkCollectionResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkPageResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatusResponse;

import java.util.List;
import java.util.UUID;

public interface BookmarkService {

    BookmarkResponse addBookmark(UUID userId, CreateBookmarkRequest request);

    BookmarkPageResponse getMyBookmarks(
            UUID userId,
            int page,
            int size,
            UUID collectionId,
            String keyword
    );

    BookmarkStatusResponse getStatus(UUID userId, String openAlexId);

    List<BookmarkCollectionResponse> getCollections(UUID userId);

    BookmarkCollectionResponse createCollection(UUID userId, CreateBookmarkCollectionRequest request);

    void deleteCollection(UUID userId, UUID collectionId);

    void addBookmarksToCollection(UUID userId, UUID collectionId, UpdateBookmarkCollectionItemsRequest request);

    void removeBookmarkFromCollection(UUID userId, UUID collectionId, UUID bookmarkId);

    void deleteBookmark(UUID userId, UUID bookmarkId);

    void deleteByOpenAlexId(UUID userId, String openAlexId);
}
