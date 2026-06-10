package com.brotherhood.scipubtts.search.service;

import com.brotherhood.scipubtts.search.dto.SearchWorksResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SearchWorksMapper {

    // OpenAlex returns dynamic JSON, so OpenAlexMapReader extracts data safely.
    private final OpenAlexMapReader openAlexMapReader;

    public SearchWorksMapper(OpenAlexMapReader openAlexMapReader) {
        this.openAlexMapReader = openAlexMapReader;
    }

    public SearchWorksResponse map(
            Map<String, Object> response,
            String appliedFilter,
            String appliedSort,
            int fallbackPage,
            int fallbackPerPage
    ) {
        // Read response metadata first so paging info is always available.
        Map<String, Object> meta = openAlexMapReader.getMap(response, "meta");
        long totalCount = openAlexMapReader.getLong(meta, "count", 0L);
        int page = openAlexMapReader.getInt(meta, "page", fallbackPage);
        int perPage = openAlexMapReader.getInt(meta, "per_page", fallbackPerPage);
        long dbResponseTimeMs = openAlexMapReader.getLong(meta, "db_response_time_ms", 0L);
        double costUsd = openAlexMapReader.getDouble(meta, "cost_usd", 0.0);

        List<SearchWorksResponse.WorkItem> items = new ArrayList<>();
        List<Map<String, Object>> results = openAlexMapReader.getMapList(response, "results");

        for (Map<String, Object> result : results) {
            items.add(mapWorkItem(result));
        }

        SearchWorksResponse.Meta responseMeta = new SearchWorksResponse.Meta(
                totalCount,
                page,
                perPage,
                dbResponseTimeMs,
                costUsd,
                appliedFilter,
                appliedSort
        );

        return new SearchWorksResponse(responseMeta, items);
    }

    private SearchWorksResponse.WorkItem mapWorkItem(Map<String, Object> result) {
        Map<String, Object> openAccess = openAlexMapReader.getMap(result, "open_access");
        Map<String, Object> hasContent = openAlexMapReader.getMap(result, "has_content");
        Map<String, Object> primaryTopic = openAlexMapReader.getMap(result, "primary_topic");
        Map<String, Object> subField = openAlexMapReader.getMap(primaryTopic, "subfield");
        Map<String, Object> primaryLocation = openAlexMapReader.getMap(result, "primary_location");
        Map<String, Object> source = openAlexMapReader.getMap(primaryLocation, "source");
        List<Map<String, Object>> authorships = openAlexMapReader.getMapList(result, "authorships");

        // Build one DTO that the frontend can consume directly.
        return new SearchWorksResponse.WorkItem(
                openAlexMapReader.getString(result, "id"),
                openAlexMapReader.sanitizeDisplayText(openAlexMapReader.getString(result, "display_name")),
                openAlexMapReader.deriveAbstractText(openAlexMapReader.getMap(result, "abstract_inverted_index")),
                openAlexMapReader.getString(result, "doi"),
                openAlexMapReader.getInteger(result, "publication_year"),
                openAlexMapReader.getInteger(result, "cited_by_count"),
                openAlexMapReader.getBoolean(openAccess, "is_oa"),
                openAlexMapReader.getBoolean(hasContent, "pdf"),
                openAlexMapReader.derivePdfUrl(result),
                openAlexMapReader.deriveHasOrcid(authorships),
                openAlexMapReader.sanitizeDisplayText(openAlexMapReader.getString(result, "type")),
                openAlexMapReader.sanitizeDisplayText(openAlexMapReader.getString(primaryTopic, "display_name")),
                openAlexMapReader.sanitizeDisplayText(openAlexMapReader.getString(subField, "display_name")),
                openAlexMapReader.getString(source, "id"),
                openAlexMapReader.sanitizeDisplayText(openAlexMapReader.getString(source, "display_name")),
                mapAuthorNames(authorships)
        );
    }

    private List<String> mapAuthorNames(List<Map<String, Object>> authorships) {
        // Collect author display names in their original order.
        List<String> names = new ArrayList<>();

        for (Map<String, Object> authorship : authorships) {
            Map<String, Object> author = openAlexMapReader.getMap(authorship, "author");
            String name = openAlexMapReader.sanitizeDisplayText(openAlexMapReader.getString(author, "display_name"));
            if (!name.isBlank()) {
                names.add(name);
            }
        }

        return names;
    }
}
