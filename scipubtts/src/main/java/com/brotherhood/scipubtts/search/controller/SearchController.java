package com.brotherhood.scipubtts.search.controller;

import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.search.dto.SearchFilterOptionsResponse;
import com.brotherhood.scipubtts.search.dto.SearchFilterOptionListResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistoryItemResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistorySaveRequest;
import com.brotherhood.scipubtts.search.dto.SearchSummaryResponse;
import com.brotherhood.scipubtts.search.dto.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksResponse;
import com.brotherhood.scipubtts.search.service.SearchService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    // This controller only receives HTTP requests and forwards them to the service layer.
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get search summary")
    public ResponseEntity<ResponseObject> getSummary() {
        SearchSummaryResponse data = searchService.getSummary();

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded search summary", data)
        );
    }

    @GetMapping("/filters/options")
    public ResponseEntity<ResponseObject> getFilterOptions(
            @Parameter(example = "machine learning")
            @RequestParam(defaultValue = "") String keyword,
            @Parameter(example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(example = "1")
            @RequestParam(defaultValue = "1") int page
    ) {
        // Load option lists used by the frontend filter widgets.
        SearchFilterOptionsResponse data = searchService.getFilterOptions(keyword, limit, page);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded search filter options", data)
        );
    }

    @GetMapping("/filters/{filterKey}/options")
    public ResponseEntity<ResponseObject> getFilterOptionPage(
            @PathVariable String filterKey,
            @Parameter(example = "machine learning")
            @RequestParam(defaultValue = "") String keyword,
            @Parameter(example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(example = "1")
            @RequestParam(defaultValue = "1") int page
    ) {
        SearchFilterOptionListResponse data = searchService.getFilterOptionPage(filterKey, keyword, limit, page);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded search filter option page", data)
        );
    }

    @GetMapping("/works")
    @Operation(summary = "Search works")
    public ResponseEntity<ResponseObject> searchWorks(
            @ParameterObject @ModelAttribute SearchWorksQueryRequest request
    ) {
        // Search papers with the optional query, filters and sort values.
        SearchWorksResponse data = searchService.searchWorks(request);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Search works successfully", data)
        );
    }

    @GetMapping("/history/recent")
    public ResponseEntity<ResponseObject> getRecentSearches(
            @Parameter(hidden = true) @CurrentUserUUID(required = false) UUID userId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "7") int limit
    ) {
        List<SearchHistoryItemResponse> data = searchService.getRecentSearches(userId, keyword, limit);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded recent searches", data)
        );
    }

    @PostMapping("/history")
    public ResponseEntity<ResponseObject> saveSearchHistory(
            @Parameter(hidden = true) @CurrentUserUUID(required = false) UUID userId,
            @RequestBody SearchHistorySaveRequest request
    ) {
        searchService.saveSearchHistory(request.withUserId(userId));

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Saved search history", null)
        );
    }
}
