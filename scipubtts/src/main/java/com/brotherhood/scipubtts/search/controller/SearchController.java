package com.brotherhood.scipubtts.search.controller;

import com.brotherhood.scipubtts.common.annotation.CurrentUserUUID;
import com.brotherhood.scipubtts.common.apiResponse.ResponseObject;
import com.brotherhood.scipubtts.search.dto.response.SearchFilterOptionsResponse;
import com.brotherhood.scipubtts.search.dto.response.SearchFilterOptionListResponse;
import com.brotherhood.scipubtts.search.dto.response.SearchEntitiesResponse;
import com.brotherhood.scipubtts.search.dto.request.SearchEntityQueryRequest;
import com.brotherhood.scipubtts.search.dto.SearchEntityType;
import com.brotherhood.scipubtts.search.dto.response.SearchHistoryItemResponse;
import com.brotherhood.scipubtts.search.dto.request.SearchHistorySaveRequest;
import com.brotherhood.scipubtts.search.dto.response.SearchSummaryResponse;
import com.brotherhood.scipubtts.search.dto.request.SearchWorksQueryRequest;
import com.brotherhood.scipubtts.search.dto.response.SearchWorksResponse;
import com.brotherhood.scipubtts.search.service.SearchService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(
            SearchService searchService
    ) {
        this.searchService = searchService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get search summary")
    public ResponseEntity<ResponseObject> getSummary(
            @Parameter(
                    schema = @Schema(allowableValues = {
                            "works",
                            "authors",
                            "topics"
                    })
            )
            @RequestParam(defaultValue = "works") String entityType
    ) {
        SearchSummaryResponse data = searchService.getSummary(
                SearchEntityType.fromParameter(entityType)
        );

        return ok("Loaded search summary", data);
    }

    @GetMapping("/filters/options")
    @Operation(summary = "Get search filter options")
    public ResponseEntity<ResponseObject> getFilterOptions(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "1") int page
    ) {
        SearchFilterOptionsResponse data = searchService.getFilterOptions(keyword, limit, page);

        return ok("Loaded search filter options", data);
    }

    @GetMapping("/filters/{filterKey}/options")
    @Operation(summary = "Get one filter option list")
    public ResponseEntity<ResponseObject> getFilterOptionPage(
            @Parameter(
                    schema = @Schema(allowableValues = {
                            "type",
                            "subField",
                            "field",
                            "country",
                            "author",
                            "institution",
                            "primaryTopic",
                            "source",
                            "award"
                    })
            )
            @PathVariable String filterKey,
            @Parameter(
                    schema = @Schema(allowableValues = {
                            "works",
                            "authors",
                            "topics"
                    })
            )
            @RequestParam(defaultValue = "works") String entityType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "1") int page
    ) {
        SearchFilterOptionListResponse data = searchService.getFilterOptionPage(
                filterKey,
                SearchEntityType.fromParameter(entityType),
                keyword,
                limit,
                page
        );

        return ok("Loaded search filter option page", data);
    }

    @GetMapping("/works")
    @Operation(summary = "Search works")
    public ResponseEntity<ResponseObject> searchWorks(
            @ParameterObject @ModelAttribute SearchWorksQueryRequest request
    ) {
        SearchWorksResponse data = searchService.searchWorks(request);

        return ok("Search works successfully", data);
    }

    @GetMapping("/entities")
    @Operation(summary = "Search entities")
    public ResponseEntity<ResponseObject> searchEntities(
            @Parameter(
                    schema = @Schema(allowableValues = {
                            "authors",
                            "topics"
                    })
            )
            @RequestParam(defaultValue = "authors") String entityType,
            @ParameterObject @ModelAttribute SearchEntityQueryRequest request
    ) {
        SearchEntitiesResponse data = searchService.searchEntities(
                SearchEntityType.fromParameter(entityType),
                request
        );

        return ok("Search entities successfully", data);
    }

    @GetMapping("/history/recent")
    @Operation(summary = "Get recent search suggestions")
    public ResponseEntity<ResponseObject> getRecentSearches(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "7") int limit
    ) {
        List<SearchHistoryItemResponse> data = searchService.getRecentSearches(userId, keyword, limit);

        return ok("Loaded recent searches", data);
    }

    @PostMapping("/history")
    @Operation(summary = "Save search history")
    public ResponseEntity<ResponseObject> saveSearchHistory(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestBody SearchHistorySaveRequest request
    ) {
        searchService.saveSearchHistory(request.withUserId(userId));

        return ok("Saved search history", null);
    }

    @DeleteMapping("/history")
    @Operation(summary = "Delete one search history item")
    public ResponseEntity<ResponseObject> deleteSearchHistory(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestParam String query
    ) {
        searchService.deleteSearchHistory(userId, query);

        return ok("Deleted search history item", null);
    }

    @DeleteMapping("/history/all")
    @Operation(summary = "Clear all search history")
    public ResponseEntity<ResponseObject> clearSearchHistory(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId
    ) {
        searchService.clearSearchHistory(userId);

        return ok("Cleared search history", null);
    }

    private ResponseEntity<ResponseObject> ok(String message, Object data) {
        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), message, data)
        );
    }
}
