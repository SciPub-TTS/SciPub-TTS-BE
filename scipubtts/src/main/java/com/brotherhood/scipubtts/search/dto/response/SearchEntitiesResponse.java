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
public class SearchEntitiesResponse {
    private Meta meta;

    private List<EntityItem> results;


    public Meta meta() {
        return meta;
    }

    public List<EntityItem> results() {
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

        private String entityType;

        private boolean hasMore;

        private boolean totalCountExact;

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

        public String entityType() {
            return entityType;
        }

        public boolean hasMore() {
            return hasMore;
        }

        public boolean totalCountExact() {
            return totalCountExact;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityItem {
        private String id;

        private String entityType;

        private String displayName;

        private String primaryInstitutionName;

        private String primaryTopicName;

        private String subfieldName;

        private String fieldName;

        private String domainName;

        private long worksCount;

        public String id() {
            return id;
        }

        public String entityType() {
            return entityType;
        }

        public String displayName() {
            return displayName;
        }

        public String primaryInstitutionName() {
            return primaryInstitutionName;
        }

        public String primaryTopicName() {
            return primaryTopicName;
        }

        public String subfieldName() {
            return subfieldName;
        }

        public String fieldName() {
            return fieldName;
        }

        public String domainName() {
            return domainName;
        }

        public long worksCount() {
            return worksCount;
        }
    }
}

