package com.brotherhood.scipubtts.search.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchWorksResponse {
    private Meta meta;

    private List<WorkItem> results;


    public Meta meta() {
        return meta;
    }

    public List<WorkItem> results() {
        return results;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private long totalCount;

        private int page;

        private int perPage;

        private long dbResponseTimeMs;

        private double costUsd;

        private String appliedFilter;

        private String appliedSort;

        public long totalCount() {
            return totalCount;
        }

        public int page() {
            return page;
        }

        public int perPage() {
            return perPage;
        }

        public long dbResponseTimeMs() {
            return dbResponseTimeMs;
        }

        public double costUsd() {
            return costUsd;
        }

        public String appliedFilter() {
            return appliedFilter;
        }

        public String appliedSort() {
            return appliedSort;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityRef {
        private String id;

        private String displayName;

        public String id() {
            return id;
        }

        public String displayName() {
            return displayName;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkItem {
        private String id;

        private String title;

        private String abstractText;

        private String doi;

        private Integer publicationYear;

        private Integer citedByCount;

        private Boolean openAccess;

        private Boolean hasPdf;

        private String pdfUrl;

        private Boolean hasOrcid;

        private String type;

        private String topic;

        private String subFieldName;

        private String sourceId;

        private String sourceName;

        private List<String> authors;

        private List<EntityRef> authorRefs;

        private List<String> keywords;

        private EntityRef topicRef;

        private Boolean matchesTrendingKeyword;

        private Boolean matchesTrendingTopic;

        private Double trendingScore;

        public String id() {
            return id;
        }

        public String title() {
            return title;
        }

        public String abstractText() {
            return abstractText;
        }

        public String doi() {
            return doi;
        }

        public Integer publicationYear() {
            return publicationYear;
        }

        public Integer citedByCount() {
            return citedByCount;
        }

        public Boolean openAccess() {
            return openAccess;
        }

        public Boolean hasPdf() {
            return hasPdf;
        }

        public String pdfUrl() {
            return pdfUrl;
        }

        public Boolean hasOrcid() {
            return hasOrcid;
        }

        public String type() {
            return type;
        }

        public String topic() {
            return topic;
        }

        public String subFieldName() {
            return subFieldName;
        }

        public String sourceId() {
            return sourceId;
        }

        public String sourceName() {
            return sourceName;
        }

        public List<String> authors() {
            return authors;
        }

        public List<EntityRef> authorRefs() {
            return authorRefs;
        }

        public List<String> keywords() {
            return keywords;
        }

        public EntityRef topicRef() {
            return topicRef;
        }

        public Boolean matchesTrendingKeyword() {
            return matchesTrendingKeyword;
        }

        public Boolean matchesTrendingTopic() {
            return matchesTrendingTopic;
        }

        public Double trendingScore() {
            return trendingScore;
        }
    }
}


