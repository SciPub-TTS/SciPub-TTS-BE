//package com.brotherhood.scipubtts.bookmark.controller;
//
//import com.brotherhood.scipubtts.auth.security.UserPrincipal;
//import com.brotherhood.scipubtts.bookmark.dto.request.BookmarkPageResponse;
//import com.brotherhood.scipubtts.bookmark.dto.request.CreateBookmarkRequest;
//import com.brotherhood.scipubtts.bookmark.dto.request.UpdateBookmarkNoteRequest;
//import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkResponse;
//import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatsResponse;
//import com.brotherhood.scipubtts.bookmark.dto.response.BookmarkStatusResponse;
//import com.brotherhood.scipubtts.bookmark.dto.response.FilterOptionsResponse;
//import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
//import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
//import io.swagger.v3.oas.annotations.Parameter;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/bookmarks")
//@RequiredArgsConstructor
//public class BookmarkController {
//
//    private final BookmarkService bookmarkService;
//
//    // ==========================================
//    // 1. ADD BOOKMARK
//    // ==========================================
//    @PostMapping
//    public ResponseEntity<ResponseObject> addBookmark(
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
//            @RequestBody CreateBookmarkRequest request) {
//
//        UUID userId = userPrincipal.getId();
//        BookmarkResponse data = bookmarkService.addBookmark(userId, request);
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(new ResponseObject(201, "Lưu bookmark thành công", data));
//    }
//
//    // ==========================================
//    // 2. GET BOOKMARK LIST (LAZY PAGING)
//    // ==========================================
//    @GetMapping
//    public ResponseEntity<ResponseObject> getMyBookmarks(
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "12") int size,
//            @RequestParam(required = false) String keyword,
//            @RequestParam(required = false) String topic,
//            @RequestParam(required = false) String source,
//            @RequestParam(required = false) Integer year,
//            @RequestParam(defaultValue = "RECENT") String sort) {
//
//        UUID userId = userPrincipal.getId();
//        BookmarkPageResponse data = bookmarkService.getMyBookmarks(
//                userId, page, size, keyword, topic, source, year, sort
//        );
//
//        return ResponseEntity.ok(
//                new ResponseObject(200, "Lấy danh sách bookmark thành công", data)
//        );
//    }
//
//    // ==========================================
//    // 3. CHECK BOOKMARK STATUS
//    // ==========================================
//    @GetMapping("/status")
//    public ResponseEntity<ResponseObject> getBookmarkStatus(
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
//            @RequestParam String openAlexId) {
//
//        UUID userId = userPrincipal.getId();
//        BookmarkStatusResponse data = bookmarkService.getStatus(userId, openAlexId);
//
//        return ResponseEntity.ok(
//                new ResponseObject(200, "Kiểm tra bookmark thành công", data)
//        );
//    }
//
//    // ==========================================
//    // 4. GET BOOKMARK STATS
//    // ==========================================
//    @GetMapping("/stats")
//    public ResponseEntity<ResponseObject> getStats(
//            @AuthenticationPrincipal UserPrincipal userPrincipal) {
//
//        UUID userId = userPrincipal.getId();
//        BookmarkStatsResponse data = bookmarkService.getStats(userId);
//
//        return ResponseEntity.ok(
//                new ResponseObject(200, "Lấy thống kê bookmark thành công", data)
//        );
//    }
//
//    // ==========================================
//    // 5. GET FILTER OPTIONS
//    // ==========================================
//    @GetMapping("/filter-options")
//    public ResponseEntity<ResponseObject> getFilterOptions(
//            @AuthenticationPrincipal UserPrincipal userPrincipal) {
//
//        UUID userId = userPrincipal.getId();
//        FilterOptionsResponse data = bookmarkService.getFilterOptions(userId);
//
//        return ResponseEntity.ok(
//                new ResponseObject(200, "Lấy bộ lọc bookmark thành công", data)
//        );
//    }
//
//    // ==========================================
//    // 6. ADD OR UPDATE NOTE
//    // ==========================================
//    @PatchMapping("/{bookmarkId}/note")
//    public ResponseEntity<ResponseObject> updateNote(
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
//            @PathVariable UUID bookmarkId,
//            @RequestBody UpdateBookmarkNoteRequest request) {
//
//        UUID userId = userPrincipal.getId();
//        BookmarkResponse data = bookmarkService.updateNote(userId, bookmarkId, request);
//
//        return ResponseEntity.ok(
//                new ResponseObject(200, "Cập nhật ghi chú bookmark thành công", data)
//        );
//    }
//
//    // ==========================================
//    // 7. DELETE BOOKMARK BY ID
//    // ==========================================
//    @DeleteMapping("/{bookmarkId}")
//    public ResponseEntity<ResponseObject> deleteBookmark(
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
//            @PathVariable UUID bookmarkId) {
//
//        UUID userId = userPrincipal.getId();
//        bookmarkService.deleteBookmark(userId, bookmarkId);
//
//        return ResponseEntity.ok(
//                new ResponseObject(200, "Xóa bookmark thành công", null)
//        );
//    }
//
//    // ==========================================
//    // 8. DELETE BOOKMARK BY OPENALEX ID
//    // ==========================================
//    @DeleteMapping("/by-openalex/{openAlexId}")
//    public ResponseEntity<ResponseObject> deleteByOpenAlexId(
//            @AuthenticationPrincipal UserPrincipal userPrincipal,
//            @PathVariable String openAlexId) {
//
//        UUID userId = userPrincipal.getId();
//        bookmarkService.deleteByOpenAlexId(userId, openAlexId);
//
//        // Idempotent: Dù có tìm thấy để xóa hay không, vẫn trả về 200 để FE yên tâm
//        return ResponseEntity.ok(
//                new ResponseObject(200, "Bỏ lưu bookmark thành công", null)
//        );
//    }
//}
