package com.brotherhood.scipubtts.search.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchSummaryResponse {
    private long totalCount;

    private String entityType;

    private boolean totalCountExact;

    public long totalCount() {
        return totalCount;
    }

    public String entityType() {
        return entityType;
    }

    public boolean totalCountExact() {
        return totalCountExact;
    }
}

