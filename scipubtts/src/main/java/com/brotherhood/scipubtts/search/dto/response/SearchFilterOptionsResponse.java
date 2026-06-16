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
public class SearchFilterOptionsResponse {
    private long totalWorks;

    private YearRange year;

    private List<FacetOption> type;

    private ToggleFilter openAccess;

    private List<FacetOption> subField;

    private List<EntityOption> author;

    private List<EntityOption> institution;

    private ToggleFilter pdf;

    private CitationRange citation;

    private List<FacetOption> country;

    private List<EntityOption> source;

    private List<EntityOption> award;

    private EnumFilter indexedByOrcid;


    public long totalWorks() {
        return totalWorks;
    }

    public YearRange year() {
        return year;
    }

    public List<FacetOption> type() {
        return type;
    }

    public ToggleFilter openAccess() {
        return openAccess;
    }

    public List<FacetOption> subField() {
        return subField;
    }

    public List<EntityOption> author() {
        return author;
    }

    public List<EntityOption> institution() {
        return institution;
    }

    public ToggleFilter pdf() {
        return pdf;
    }

    public CitationRange citation() {
        return citation;
    }

    public List<FacetOption> country() {
        return country;
    }

    public List<EntityOption> source() {
        return source;
    }

    public List<EntityOption> award() {
        return award;
    }

    public EnumFilter indexedByOrcid() {
        return indexedByOrcid;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearRange {
        private int minimumYear;

        private int currentYear;

        public int minimumYear() {
            return minimumYear;
        }

        public int currentYear() {
            return currentYear;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CitationRange {
        private int minimumCitation;

        private int maximumCitation;

        public int minimumCitation() {
            return minimumCitation;
        }

        public int maximumCitation() {
            return maximumCitation;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacetOption {
        private String value;

        private String label;

        private long count;

        public String value() {
            return value;
        }

        public String label() {
            return label;
        }

        public long count() {
            return count;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityOption {
        private String value;

        private String label;

        private long count;

        public String value() {
            return value;
        }

        public String label() {
            return label;
        }

        public long count() {
            return count;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToggleFilter {
        private String openAlexFilterKey;

        private boolean defaultValue;

        public String openAlexFilterKey() {
            return openAlexFilterKey;
        }

        public boolean defaultValue() {
            return defaultValue;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnumFilter {
        private String openAlexFilterKey;

        private List<String> options;

        private String defaultValue;

        public String openAlexFilterKey() {
            return openAlexFilterKey;
        }

        public List<String> options() {
            return options;
        }

        public String defaultValue() {
            return defaultValue;
        }
    }
}


