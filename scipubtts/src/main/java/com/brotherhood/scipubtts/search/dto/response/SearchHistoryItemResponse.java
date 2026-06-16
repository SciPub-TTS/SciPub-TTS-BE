package com.brotherhood.scipubtts.search.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryItemResponse {
    private String id;

    private String query;

    private String savedAt;

    public String id() {
        return id;
    }

    public String query() {
        return query;
    }

    public String savedAt() {
        return savedAt;
    }
}


