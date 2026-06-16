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

    // This controller only receives HTTP requests and forwards them to the service layer.
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Get search summary",
            description = "Returns the total indexed count shown at the top of the search page for the selected entity type."
    )
    public ResponseEntity<ResponseObject> getSummary(
            @Parameter(
                    description = "Entity type to summarize.",
                    example = "works",
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

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded search summary", data)
        );
    }

    @GetMapping("/filters/options")
    @Operation(
            summary = "Get default search filter options",
            description = "Loads the search filter data used by the frontend, such as type, subfield, author, institution, country, source and award options."
    )
    public ResponseEntity<ResponseObject> getFilterOptions(
            @Parameter(
                    description = "Optional keyword used to narrow the returned option lists.",
                    example = "machine learning"
            )
            @RequestParam(defaultValue = "") String keyword,
            @Parameter(description = "Number of options returned per filter list page.", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Page number for the option lists.", example = "1")
            @RequestParam(defaultValue = "1") int page
    ) {
        // Load option lists used by the frontend filter widgets.
        SearchFilterOptionsResponse data = searchService.getFilterOptions(keyword, limit, page);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded search filter options", data)
        );
    }

    @GetMapping("/filters/{filterKey}/options")
    @Operation(
            summary = "Get one filter option list",
            description = "Loads options for one filter only. Valid filterKey values are: type, subField, country, author, institution, source, award."
    )
    public ResponseEntity<ResponseObject> getFilterOptionPage(
            @Parameter(
                    description = "Which filter option list to load.",
                    example = "author",
                    schema = @Schema(allowableValues = {
                            "type",
                            "subField",
                            "country",
                            "author",
                            "institution",
                            "source",
                            "award"
                    })
            )
            @PathVariable String filterKey,
            @Parameter(
                    description = "Optional keyword used to match option labels, for example author or source names.",
                    example = "machine learning"
            )
            @RequestParam(defaultValue = "") String keyword,
            @Parameter(description = "Number of options returned in one page.", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Page number for the selected filter option list.", example = "1")
            @RequestParam(defaultValue = "1") int page
    ) {
        SearchFilterOptionListResponse data = searchService.getFilterOptionPage(filterKey, keyword, limit, page);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded search filter option page", data)
        );
    }

    @GetMapping("/works")
    @Operation(
            summary = "Search works",
            description = "Searches works by keyword and filters. Use either yearFrom/yearTo or yearExact, and either citationMin/citationMax or citationExact. Sending both modes together returns HTTP 400."
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

    @GetMapping("/entities")
    @Operation(
            summary = "Search non-work entities",
            description = "Searches authors or topics by name."
    )
    public ResponseEntity<ResponseObject> searchEntities(
            @Parameter(
                    description = "Entity type to search.",
                    example = "authors",
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

        return ResponseEntity.ok(
                new ResponseObject(
                        HttpStatus.OK.value(),
                        "Search entities successfully",
                        data
                )
        );
    }

    @GetMapping("/history/recent")
    @Operation(
            summary = "Get recent search suggestions",
            description = "Returns up to the latest search keywords for the current user. This powers the search suggestion dialog under the search box."
    )
    public ResponseEntity<ResponseObject> getRecentSearches(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @Parameter(
                    description = "Optional prefix keyword used to filter recent searches.",
                    example = "AI"
            )
            @RequestParam(defaultValue = "") String keyword,
            @Parameter(description = "Maximum number of recent items to return.", example = "7")
            @RequestParam(defaultValue = "7") int limit
    ) {
        List<SearchHistoryItemResponse> data = searchService.getRecentSearches(userId, keyword, limit);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Loaded recent searches", data)
        );
    }

    @PostMapping("/history")
    @Operation(
            summary = "Save search history",
            description = "Saves the current search keyword for the logged-in user so it can be suggested later in the search dialog."
    )
    public ResponseEntity<ResponseObject> saveSearchHistory(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @RequestBody SearchHistorySaveRequest request
    ) {
        searchService.saveSearchHistory(request.withUserId(userId));

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Saved search history", null)
        );
    }

    @DeleteMapping("/history")
    @Operation(
            summary = "Delete one search history item",
            description = "Deletes one saved search keyword for the current user."
    )
    public ResponseEntity<ResponseObject> deleteSearchHistory(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId,
            @Parameter(
                    description = "Saved search keyword to delete.",
                    example = "AI in education"
            )
            @RequestParam String query
    ) {
        searchService.deleteSearchHistory(userId, query);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Deleted search history item", null)
        );
    }

    @DeleteMapping("/history/all")
    @Operation(
            summary = "Clear all search history",
            description = "Deletes every saved search keyword of the current user."
    )
    public ResponseEntity<ResponseObject> clearSearchHistory(
            @Parameter(hidden = true) @CurrentUserUUID UUID userId
    ) {
        searchService.clearSearchHistory(userId);

        return ResponseEntity.ok(
                new ResponseObject(HttpStatus.OK.value(), "Cleared search history", null)
        );
    }
}
