package com.brotherhood.scipubtts.detail.authors.service.impl;

import com.brotherhood.scipubtts.common.openalex.OpenAlexClient;
import com.brotherhood.scipubtts.detail.authors.dto.response.AuthorDetailResponse;
import com.brotherhood.scipubtts.detail.authors.service.AuthorDetailService;
import com.brotherhood.scipubtts.detail.works.dto.response.DetailWorkResponse;
import com.brotherhood.scipubtts.detail.works.service.DetailWorkResponseMapper;
import com.brotherhood.scipubtts.search.service.OpenAlexMapReader;
import com.brotherhood.scipubtts.search.service.SearchConstants;
import com.brotherhood.scipubtts.search.service.SearchQuerySupport;
import com.brotherhood.scipubtts.search.service.SearchWorksMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthorDetailServiceImpl implements AuthorDetailService {

    private static final int DETAIL_WORK_LIMIT = 8;
    private static final int RELATED_TOPIC_LIMIT = 8;

    private final DetailWorkResponseMapper detailWorkResponseMapper;
    private final OpenAlexClient openAlexClient;
    private final OpenAlexMapReader openAlexMapReader;
    private final SearchQuerySupport searchQuerySupport;
    private final SearchWorksMapper searchWorksMapper;

    public AuthorDetailServiceImpl(
            DetailWorkResponseMapper detailWorkResponseMapper,
            OpenAlexClient openAlexClient,
            OpenAlexMapReader openAlexMapReader,
            SearchQuerySupport searchQuerySupport,
            SearchWorksMapper searchWorksMapper
    ) {
        this.detailWorkResponseMapper = detailWorkResponseMapper;
        this.openAlexClient = openAlexClient;
        this.openAlexMapReader = openAlexMapReader;
        this.searchQuerySupport = searchQuerySupport;
        this.searchWorksMapper = searchWorksMapper;
    }

    @Override
    public AuthorDetailResponse getAuthorDetail(String rawAuthorId) {
        String authorId = normalizeEntityId(rawAuthorId);
        Map<String, Object> author = openAlexClient.get("/authors/" + authorId, Map.of());

        return buildAuthorDetailResponse(authorId, author);
    }

    private AuthorDetailResponse buildAuthorDetailResponse(
            String authorId,
            Map<String, Object> author
    ) {
        List<Map<String, Object>> institutions =
                openAlexMapReader.getMapListFromObject(author.get("last_known_institutions"));
        List<Map<String, Object>> topics =
                openAlexMapReader.getMapListFromObject(author.get("topics"));
        Map<String, Object> summaryStats = openAlexMapReader.getMap(author, "summary_stats");

        return new AuthorDetailResponse(
                "authors",
                authorId,
                normalizeText(openAlexMapReader.getString(author, "display_name")),
                openAlexMapReader.getLong(author, "works_count", 0L),
                openAlexMapReader.getLong(author, "cited_by_count", 0L),
                loadWorks("authorships.author.id:" + authorId),
                mapCountsByYear(author.get("counts_by_year")),
                openAlexMapReader.getInteger(summaryStats, "h_index"),
                openAlexMapReader.getInteger(summaryStats, "i10_index"),
                mapInstitutionNames(institutions),
                mapObservedNames(author.get("display_name_alternatives")),
                normalizeText(openAlexMapReader.getString(author, "orcid")),
                readFirstDisplayName(institutions),
                mapTopicHighlights(topics)
        );
    }

    private List<DetailWorkResponse> loadWorks(String filterValue) {
        Map<String, String> queryParams = buildWorkQueryParams(filterValue);
        Map<String, Object> response = openAlexClient.get("/works", queryParams);
        List<Map<String, Object>> results = openAlexMapReader.getMapList(response, "results");

        return detailWorkResponseMapper.mapWorkItems(searchWorksMapper.mapWorkItems(results));
    }

    private Map<String, String> buildWorkQueryParams(String filterValue) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("filter", filterValue);
        queryParams.put("sort", "publication_date:desc");
        queryParams.put("per_page", String.valueOf(DETAIL_WORK_LIMIT));
        queryParams.put("select", SearchConstants.WORKS_SELECT_FIELDS);

        return queryParams;
    }

    private List<AuthorDetailResponse.YearStat> mapCountsByYear(Object rawCountsByYear) {
        List<Map<String, Object>> yearMaps = openAlexMapReader.getMapListFromObject(rawCountsByYear);
        List<AuthorDetailResponse.YearStat> items = new ArrayList<>();

        for (Map<String, Object> yearMap : yearMaps) {
            int year = openAlexMapReader.getInt(yearMap, "year", 0);

            if (year <= 0) {
                continue;
            }

            items.add(new AuthorDetailResponse.YearStat(
                    year,
                    openAlexMapReader.getLong(yearMap, "works_count", 0L),
                    openAlexMapReader.getLong(yearMap, "cited_by_count", 0L)
            ));
        }

        items.sort(Comparator.comparingInt(AuthorDetailResponse.YearStat::year));
        return items;
    }

    private List<AuthorDetailResponse.RelatedItem> mapTopicHighlights(List<Map<String, Object>> topics) {
        List<AuthorDetailResponse.RelatedItem> relatedItems = new ArrayList<>();
        LinkedHashSet<String> seenIds = new LinkedHashSet<>();

        for (Map<String, Object> topic : topics) {
            String id = searchQuerySupport.normalizeEntityValue(
                    openAlexMapReader.getString(topic, "id")
            );
            String displayName = normalizeText(openAlexMapReader.getString(topic, "display_name"));

            if (!StringUtils.hasText(id) || !StringUtils.hasText(displayName) || seenIds.contains(id)) {
                continue;
            }

            relatedItems.add(new AuthorDetailResponse.RelatedItem(
                    id,
                    displayName,
                    openAlexMapReader.getLong(topic, "count", 0L)
            ));
            seenIds.add(id);

            if (relatedItems.size() == RELATED_TOPIC_LIMIT) {
                break;
            }
        }

        return relatedItems;
    }

    private List<String> mapObservedNames(Object rawObservedNames) {
        LinkedHashSet<String> observedNames = new LinkedHashSet<>();

        for (Object rawObservedName : openAlexMapReader.getObjectList(rawObservedNames)) {
            String observedName = normalizeText(String.valueOf(rawObservedName));

            if (StringUtils.hasText(observedName)) {
                observedNames.add(observedName);
            }
        }

        return new ArrayList<>(observedNames);
    }

    private List<String> mapInstitutionNames(List<Map<String, Object>> institutions) {
        LinkedHashSet<String> names = new LinkedHashSet<>();

        for (Map<String, Object> institution : institutions) {
            String institutionName = normalizeText(
                    openAlexMapReader.getString(institution, "display_name")
            );

            if (StringUtils.hasText(institutionName)) {
                names.add(institutionName);
            }
        }

        return new ArrayList<>(names);
    }

    private String readFirstDisplayName(List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            String displayName = normalizeText(
                    openAlexMapReader.getString(item, "display_name")
            );

            if (StringUtils.hasText(displayName)) {
                return displayName;
            }
        }

        return null;
    }

    private String normalizeEntityId(String rawEntityId) {
        String normalizedId = searchQuerySupport.extractLastSegment(
                rawEntityId == null ? "" : rawEntityId.trim()
        ).toUpperCase(Locale.ROOT);

        if (!StringUtils.hasText(normalizedId)) {
            return "";
        }

        return normalizedId;
    }

    private String normalizeText(String value) {
        String normalizedValue = openAlexMapReader.sanitizeDisplayText(value);

        if (!StringUtils.hasText(normalizedValue)) {
            return null;
        }

        return normalizedValue;
    }
}
