package com.brotherhood.scipubtts.search.controller;

import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.auth.security.UserPrincipal;
import com.brotherhood.scipubtts.search.dto.SearchFilterOptionsResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistoryItemResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistorySaveRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksResponse;
import com.brotherhood.scipubtts.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/search")

public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/filters/options")
    public ResponseEntity<ResponseObject> getFilterOptions(
            @Parameter(description = "Keyword for author/institution/award option lookup")
            @RequestParam(defaultValue = "") String keyword,
            @Parameter(description = "Number of options per filter group (1-100)")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Page number for filter options (>=1)")
            @RequestParam(defaultValue = "1") int page
    ) {
        SearchFilterOptionsResponse data = searchService.getFilterOptions(keyword, limit, page);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Loaded search filter options", data)
        );
    }

    @GetMapping("/works")
    public ResponseEntity<ResponseObject> searchWorks(@ModelAttribute SearchWorksQueryRequest request) {
        SearchWorksResponse data = searchService.searchWorks(request);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Search works successfully", data)
        );
    }

    @GetMapping("/history/recent")
    public ResponseEntity<ResponseObject> getRecentSearches(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "5") int limit
    ) {
        UUID userId = requireUserId(userPrincipal);
        List<SearchHistoryItemResponse> data = searchService.getRecentSearches(
                userId,
                limit
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Loaded recent searches", data)
        );
    }

    @PostMapping("/history")
    public ResponseEntity<ResponseObject> saveSearchHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody SearchHistorySaveRequest request
    ) {
        request.setUserId(requireUserId(userPrincipal));
        searchService.saveSearchHistory(request);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Saved search history", null)
        );
    }

    @DeleteMapping("/history")
    public ResponseEntity<ResponseObject> deleteSearchHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String query
    ) {
        searchService.deleteSearchHistory(requireUserId(userPrincipal), query);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject(200, "Deleted search history", null)
        );
    }

    private UUID requireUserId(UserPrincipal userPrincipal) {
        if (userPrincipal == null || userPrincipal.getId() == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        return userPrincipal.getId();
    }
}

/*
SEARCH_FILE_NOTE
Syntax su dung:
- @RestController, @RequestMapping, @GetMapping/@PostMapping/@DeleteMapping.
- @AuthenticationPrincipal de lay user dang login.
- ResponseEntity + ResponseObject de tra response thong nhat.
File nay lam gi:
- Nhan request HTTP cho search works, filter options, va search history.
Flow chay:
- FE goi API -> Controller nhan va lay user -> goi SearchService -> tra ket qua.
*/
