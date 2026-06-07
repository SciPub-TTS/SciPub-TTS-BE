package com.brotherhood.scipubtts.openalexentity.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.search.service.SearchQuerySupport;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OpenAlexEntityQueryFactory {

    private static final String TOP_WORKS_SELECT_FIELDS =
            "id,display_name,publication_year,cited_by_count,primary_location,best_oa_location";

    private final SearchQuerySupport searchQuerySupport;

    public OpenAlexEntityQueryFactory(SearchQuerySupport searchQuerySupport) {
        this.searchQuerySupport = searchQuerySupport;
    }

    public String buildEntityDetailPath(OpenAlexEntityType entityType, String entityId) {
        return entityType.buildEntityPath(normalizeEntityId(entityId));
    }

    public Map<String, String> buildTopWorksQueryParams(
            OpenAlexEntityType entityType,
            String entityId,
            Integer page,
            Integer perPage,
            String sort
    ) {
        String normalizedEntityId = normalizeEntityId(entityId);

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("filter", entityType.buildWorksFilter(normalizedEntityId));
        queryParams.put("sort", searchQuerySupport.resolveSort(sort, false));
        queryParams.put("page", String.valueOf(searchQuerySupport.normalizeWorksPage(page)));
        queryParams.put("per_page", String.valueOf(searchQuerySupport.normalizePerPage(perPage)));
        queryParams.put("select", TOP_WORKS_SELECT_FIELDS);

        return queryParams;
    }

    private String normalizeEntityId(String entityId) {
        String normalizedEntityId = searchQuerySupport.extractLastSegment(
                entityId == null ? "" : entityId.trim()
        );

        if (!StringUtils.hasText(normalizedEntityId)) {
            throw new BusinessException(ErrorCode.OPENALEX_ENTITY_NOT_FOUND);
        }

        return normalizedEntityId;
    }
}
