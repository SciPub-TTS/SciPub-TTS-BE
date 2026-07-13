package com.brotherhood.scipubtts.detail.works.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;
import com.brotherhood.scipubtts.search.service.SearchQuerySupport;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PaperDetailOpenAlexQueryFactory {

    // These select lists keep the OpenAlex response smaller and more predictable.
    private static final String WORK_DETAIL_SELECT_FIELDS =
            "id,title,doi,publication_year,publication_date,language,type,open_access,primary_location,best_oa_location,authorships,topics,primary_topic,keywords,cited_by_count,citation_normalized_percentile,fwci,referenced_works_count,referenced_works,related_works,locations_count,locations,biblio,ids,apc_list,apc_paid,has_content,content_urls,indexed_in,counts_by_year,is_retracted,abstract_inverted_index";
    private static final String WORK_REFERENCE_SELECT_FIELDS = "id,display_name,title";

    private final SearchQuerySupport searchQuerySupport;

    public PaperDetailOpenAlexQueryFactory(SearchQuerySupport searchQuerySupport) {
        this.searchQuerySupport = searchQuerySupport;
    }

    public String buildWorkDetailPath(String workId) {
        // Accept both "W123" and full OpenAlex URLs, then normalize them to one id.
        String normalizedWorkId = normalizeWorkId(workId);
        return "/works/" + normalizedWorkId;
    }

    public Map<String, String> buildWorkDetailQueryParams() {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("select", WORK_DETAIL_SELECT_FIELDS);
        return queryParams;
    }

    public Map<String, String> buildWorkReferenceQueryParams(Iterable<String> workIds) {
        // Load titles for reference ids in one batch request.
        String joinedWorkIds = joinWorkIds(workIds);
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("filter", "openalex:" + joinedWorkIds);
        queryParams.put("per_page", String.valueOf(countWorkIds(joinedWorkIds)));
        queryParams.put("select", WORK_REFERENCE_SELECT_FIELDS);
        return queryParams;
    }

    private String normalizeWorkId(String workId) {
        String normalizedWorkId = searchQuerySupport.extractLastSegment(
                workId == null ? "" : workId.trim()
        );

        if (!StringUtils.hasText(normalizedWorkId)) {
            throw new BusinessException(ErrorCode.OPENALEX_ENTITY_NOT_FOUND);
        }

        return normalizedWorkId;
    }

    private String joinWorkIds(Iterable<String> workIds) {
        StringBuilder joinedWorkIds = new StringBuilder();

        for (String workId : workIds) {
            String normalizedWorkId = normalizeWorkId(workId);
            if (!joinedWorkIds.isEmpty()) {
                joinedWorkIds.append("|");
            }
            joinedWorkIds.append(normalizedWorkId);
        }

        if (joinedWorkIds.isEmpty()) {
            throw new BusinessException(ErrorCode.OPENALEX_ENTITY_NOT_FOUND);
        }

        return joinedWorkIds.toString();
    }

    private int countWorkIds(String joinedWorkIds) {
        int count = 1;

        for (int index = 0; index < joinedWorkIds.length(); index++) {
            if (joinedWorkIds.charAt(index) == '|') {
                count++;
            }
        }

        return count;
    }
}