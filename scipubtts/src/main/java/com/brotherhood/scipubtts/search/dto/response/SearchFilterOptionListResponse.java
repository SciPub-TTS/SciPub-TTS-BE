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
public class SearchFilterOptionListResponse {
    private String filterKey;

    private List<OptionItem> options;


    public String filterKey() {
        return filterKey;
    }

    public List<OptionItem> options() {
        return options;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionItem {
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
}

