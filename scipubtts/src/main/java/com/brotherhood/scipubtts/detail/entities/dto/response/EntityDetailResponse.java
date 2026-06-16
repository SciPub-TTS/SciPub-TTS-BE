package com.brotherhood.scipubtts.detail.entities.dto.response;

import com.brotherhood.scipubtts.search.dto.response.SearchWorksResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntityDetailResponse {
    private String entityType;
    private String id;
    private String displayName;
    private String description;
    private String orcid;
    private String primaryInstitutionName;
    private String subfieldName;
    private String fieldName;
    private String domainName;
    private Long worksCount;
    private Long citedByCount;
    private Integer hIndex;
    private Integer i10Index;
    private List<String> observedNames;
    private List<String> observedInstitutions;
    private List<RelatedItem> topicHighlights;
    private List<RelatedItem> siblingTopics;
    private List<CountByYearItem> countsByYear;
    private List<BreakdownItem> typeBreakdown;
    private List<SearchWorksResponse.WorkItem> works;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedItem {
        private String id;
        private String displayName;
        private Long count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountByYearItem {
        private int year;
        private long worksCount;
        private long citedByCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreakdownItem {
        private String value;
        private String label;
        private long count;
    }
}
