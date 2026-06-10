package com.brotherhood.scipubtts.search.controller;

import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.search.dto.SearchFilterOptionsResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistoryItemResponse;
import com.brotherhood.scipubtts.search.dto.SearchHistorySaveRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchWorksResponse;
import com.brotherhood.scipubtts.search.service.SearchService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    // This controller only receives HTTP requests and forwards them to the service layer.
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
        // Load option lists used by the frontend filter widgets.
        SearchFilterOptionsResponse data = searchService.getFilterOptions(keyword, limit, page);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded search filter options", data)
        );
    }

    @GetMapping("/works")
    @Operation(
            summary = "Search works",
            description = "All query fields are optional. In Swagger, leave unused fields empty instead of sending placeholder values such as string, 0, or [\"\"]."
    )
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
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        // Load the latest search history of the current user.
        List<SearchHistoryItemResponse> data = searchService.getRecentSearches(userId, limit);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded recent searches", data)
        );
    }

    @PostMapping("/history")
    public ResponseEntity<ResponseObject> saveSearchHistory(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestBody SearchHistorySaveRequest request
    ) {
        // Save one search term for the current user.
        searchService.saveSearchHistory(request.withUserId(userId));

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Saved search history", null)
        );
    }

    @DeleteMapping("/history")
    public ResponseEntity<ResponseObject> deleteSearchHistory(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestParam String query
    ) {
        // Remove one search history entry for the current user.
        searchService.deleteSearchHistory(userId, query);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Deleted search history", null)
        );
    }
}
