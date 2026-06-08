package com.brotherhood.scipubtts.bookmark.controller;

import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkPageResponse;
import com.brotherhood.scipubtts.bookmark.dto.request.CreateBookmarkRequest;
import com.brotherhood.scipubtts.bookmark.dto.request.UpdateBookmarkNoteRequest;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatsResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatusResponse;
import com.brotherhood.scipubtts.bookmark.dto.response.FilterOptionsResponse;
import com.brotherhood.scipubtts.bookmark.service.BookmarkService;
import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // ==========================================
    // 1. ADD BOOKMARK
    // ==========================================
    @PostMapping
    public ResponseEntity<ResponseObject> addBookmark(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @Valid @RequestBody CreateBookmarkRequest request) {

        BookmarkResponse data = bookmarkService.addBookmark(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject(201, "Bookmark saved successfully", data));
    }

    // ==========================================
    // 2. GET BOOKMARK LIST (LAZY PAGING)
    // ==========================================
    @GetMapping
    public ResponseEntity<ResponseObject> getMyBookmarks(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "RECENT") String sort) {

        BookmarkPageResponse data = bookmarkService.getMyBookmarks(
                userId, page, size, keyword, topic, source, year, sort
        );

        return ResponseEntity.ok(
                new ResponseObject(200, "Bookmarks fetched successfully", data)
        );
    }

    // ==========================================
    // 3. CHECK BOOKMARK STATUS
    // ==========================================
    @GetMapping("/status")
    public ResponseEntity<ResponseObject> getBookmarkStatus(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestParam @NotBlank(message = "OpenAlex ID query parameter is required") String openAlexId) {

        BookmarkStatusResponse data = bookmarkService.getStatus(userId, openAlexId);

        return ResponseEntity.ok(
                new ResponseObject(200, "Bookmark status checked successfully", data)
        );
    }

    // ==========================================
    // 4. GET BOOKMARK STATS
    // ==========================================
    @GetMapping("/stats")
    public ResponseEntity<ResponseObject> getStats(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId) {

        BookmarkStatsResponse data = bookmarkService.getStats(userId);

        return ResponseEntity.ok(
                new ResponseObject(200, "Bookmark statistics fetched successfully", data)
        );
    }

    // ==========================================
    // 5. GET FILTER OPTIONS
    // ==========================================
    @GetMapping("/filter-options")
    public ResponseEntity<ResponseObject> getFilterOptions(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId) {

        FilterOptionsResponse data = bookmarkService.getFilterOptions(userId);

        return ResponseEntity.ok(
                new ResponseObject(200, "Bookmark filter options fetched successfully", data)
        );
    }

    // ==========================================
    // 6. ADD OR UPDATE NOTE
    // ==========================================
    @PatchMapping("/{bookmarkId}/note")
    public ResponseEntity<ResponseObject> updateNote(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @PathVariable UUID bookmarkId,
            @RequestBody UpdateBookmarkNoteRequest request) {

        BookmarkResponse data = bookmarkService.updateNote(userId, bookmarkId, request);

        return ResponseEntity.ok(
                new ResponseObject(200, "Bookmark note updated successfully", data)
        );
    }

    // ==========================================
    // 7. DELETE BOOKMARK BY ID
    // ==========================================
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<ResponseObject> deleteBookmark(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @PathVariable UUID bookmarkId) {

        bookmarkService.deleteBookmark(userId, bookmarkId);

        return ResponseEntity.ok(
                new ResponseObject(200, "Bookmark deleted successfully", null)
        );
    }

    // ==========================================
    // 8. DELETE BOOKMARK BY OPENALEX ID
    // ==========================================
    @DeleteMapping("/by-openalex/{openAlexId}")
    public ResponseEntity<ResponseObject> deleteByOpenAlexId(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @PathVariable @NotBlank(message = "OpenAlex ID query parameter is required") String openAlexId) {

        bookmarkService.deleteByOpenAlexId(userId, openAlexId);

        return ResponseEntity.ok(
                new ResponseObject(200, "Bookmark removed successfully", null)
        );
    }
}